package no.nav.tms.varselbjelle.api

import io.kotest.matchers.shouldBe
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.http.HttpMethod.Companion.Get
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import no.nav.tms.varselbjelle.api.config.jsonConfig
import no.nav.tms.varselbjelle.api.varsel.Varsel
import no.nav.tms.varselbjelle.api.varsel.VarselService
import no.nav.tms.varselbjelle.api.varsel.VarselType.BESKJED
import no.nav.tms.varselbjelle.api.varsel.VarselType.INNBOKS
import no.nav.tms.varselbjelle.api.varsel.VarselType.OPPGAVE
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.ZoneOffset.UTC
import java.time.ZonedDateTime


class VarselApiTest {
    private val eventhandlerTestUrl = "https://test.eventhandler.no"
    private val eventaggregatorTestUrl = "https://test.eventaggregator.no"
    private val acceptedFnr = "54235437876"

    @Test
    fun `Returner varsel med nødvendige felter`() {
        val varsel = testVarsel(
            varselType = BESKJED,
            forstbehandlet = ZonedDateTime.now(UTC),
            sikkerhetsnivaa = 4,
            tekst = "teekst",
            link = "liink"
        )


        val response = testApi(varslerFromExternalService = listOf(varsel)) {
            url("tms-varselbjelle-api/varsel/aktive")
            method = Get
            header("fodselsnummer", "12345678912")
            header("auth_level", "4")
        }

        runBlocking {
            response.status shouldBe HttpStatusCode.OK
            val varslerGroupedByType = Json.decodeFromString<VarselbjelleVarslerByType>(response.bodyAsText())
            val varselResponse = varslerGroupedByType.beskjeder.first()
            varselResponse.eventId shouldBe varsel.eventId
            varselResponse.isMasked shouldBe false
            varselResponse.tidspunkt shouldBe varsel.forstBehandlet
            varselResponse.link shouldBe varsel.link
            varselResponse.tekst shouldBe varsel.tekst
            varselResponse.eksternVarslingSendt shouldBe false
            varselResponse.eksternVarslingKanaler shouldBe emptyList()
        }
    }

    @Test
    fun `Returner liste av varsler`() {
        val varsler = listOf(
            testVarsel(varselType = BESKJED, eksternVarslingKanaler = listOf("SMS")),
            testVarsel(varselType = BESKJED, eksternVarslingKanaler = listOf("EPOST","SMS")),
            testVarsel(OPPGAVE, eksternVarslingKanaler = listOf("SMS")),
            testVarsel(OPPGAVE),
            testVarsel(INNBOKS, eksternVarslingKanaler = listOf("EPOST")),
            testVarsel(INNBOKS),
            testVarsel(INNBOKS),
        )

        val response = testApi(varslerFromExternalService = varsler) {
            url("tms-varselbjelle-api/varsel/aktive")
            method = Get
            header("fodselsnummer", "12345678912")
            header("auth_level", "4")
        }

        runBlocking {
            response.status shouldBe HttpStatusCode.OK
            Json.decodeFromString<VarselbjelleVarslerByType>(response.bodyAsText()).assert {
                beskjeder.size shouldBe 2
                oppgaver.size shouldBe 2
                innbokser.size shouldBe 3
                beskjeder.filter { it.eksternVarslingSendt && it.eksternVarslingKanaler==listOf("SMS")}.size shouldBe 1
                beskjeder.filter { it.eksternVarslingSendt && it.eksternVarslingKanaler==listOf("EPOST","SMS")}.size shouldBe 1
                innbokser.filter { it.eksternVarslingSendt && it.eksternVarslingKanaler == listOf("EPOST") }
            }

        }
    }

    @Test
    fun `Returner liste av varsler med innboks og beskjed i samme liste`() {
        val varsler =
            testVarsel(BESKJED, eksternVarslingKanaler = listOf("SMS")) * 3 + testVarsel(OPPGAVE) * 2 + testVarsel(INNBOKS) * 2


        val response = testApi(varslerFromExternalService = varsler) {
            url("tms-varselbjelle-api/varsel")
            method = Get
            header("fodselsnummer", "12345678912")
            header("auth_level", "4")
        }

        runBlocking {
            response.status shouldBe HttpStatusCode.OK
            val varslerGroupedByBeskjedAndOppgaver = Json.decodeFromString<VarselbjelleVarsler>(response.bodyAsText())
            varslerGroupedByBeskjedAndOppgaver.oppgaver.size shouldBe 2
            varslerGroupedByBeskjedAndOppgaver.beskjeder.size shouldBe 5

            val expected = varsler.first()
            val varselResponse = varslerGroupedByBeskjedAndOppgaver.beskjeder.first { it.eventId == expected.eventId }
            varselResponse.eventId shouldBe expected.eventId
            varselResponse.isMasked shouldBe false
            varselResponse.tidspunkt shouldBe expected.forstBehandlet
            varselResponse.link shouldBe expected.link
            varselResponse.tekst shouldBe expected.tekst
            varselResponse.type shouldBe expected.type.name
            varselResponse.eksternVarslingSendt shouldBe true
            varselResponse.eksternVarslingKanaler shouldBe  listOf("SMS")
        }
    }

    @Test
    fun `maskerer varselinnhold ved for lavt innlogginsnivå`() {
        val varsler = listOf(
            testVarsel(BESKJED, sikkerhetsnivaa = 4),
        )

        testApi(varslerFromExternalService = varsler) {
            url("tms-varselbjelle-api/varsel/aktive")
            method = Get
            header("fodselsnummer", "12345678912")
            header("auth_level", "3")
        }.assert {
            runBlocking {
                status shouldBe HttpStatusCode.OK
                val varslerGroupedByType = Json.decodeFromString<VarselbjelleVarslerByType>(bodyAsText())

                val varsel = varslerGroupedByType.beskjeder.first()
                varsel.isMasked shouldBe true
                varsel.link shouldBe null
                varsel.tekst shouldBe null
            }
        }

        testApi(varslerFromExternalService = varsler) {
            url("tms-varselbjelle-api/varsel")
            method = Get
            header("fodselsnummer", "12345678912")
            header("auth_level", "3")
        }.assert {
            runBlocking {
                status shouldBe HttpStatusCode.OK
                val varslerGroupedByType = Json.decodeFromString<VarselbjelleVarsler>(bodyAsText())

                val varsel = varslerGroupedByType.beskjeder.first()
                varsel.isMasked shouldBe true
                varsel.link shouldBe null
                varsel.tekst shouldBe null
            }
        }


    }

    @Nested
    inner class DoneEndpoint {

        private val defaultBody = """{ "eventId": "doneeventid"}""".trimMargin()

        @Test
        fun `inaktiverer varsel med eventId`() = testApplication {
            mockDoneApi(HttpStatusCode.OK)
            client.postDone(body = defaultBody).status shouldBe HttpStatusCode.OK
        }

        @Test
        fun `400 hvis eventid mangler eller body er tom`() = testApplication {
            mockDoneApi(HttpStatusCode.OK)
            client.postDone(body = null).status shouldBe HttpStatusCode.BadRequest
            client.postDone(body = """{ "ikkeEventId": "ikkeDoneEventid"}""".trimMargin()).status shouldBe HttpStatusCode.BadRequest
        }

        @Test
        fun `Feilkoder hvis varselbjelleapi feiler mot eventaggragator`() {
            testApplication {
                mockDoneApi(HttpStatusCode.MethodNotAllowed)
                client.postDone(defaultBody).status shouldBe HttpStatusCode.InternalServerError
            }
            testApplication {
                mockDoneApi(HttpStatusCode.NotFound)
                client.postDone(defaultBody).status shouldBe HttpStatusCode.NotFound
            }

        }

        private suspend fun HttpClient.postDone(body: String?) = post {
            url("tms-varselbjelle-api/varsel/beskjed/done")
            header("fodselsnummer", acceptedFnr)
            header("auth_level", "4")
            if (body != null)
                setBody(body)
        }

        private fun ApplicationTestBuilder.mockDoneApi(respondWith: HttpStatusCode) {
            mockVarselbjelleApi(
                varselService = VarselService(
                    client = eventhandlerHttpClient(),
                    azureTokenFetcher = mockk(relaxed = true),
                    eventHandlerBaseURL = eventhandlerTestUrl,
                    eventAggregatorBaseUrl = eventaggregatorTestUrl
                )
            )

            externalServices {
                hosts(eventaggregatorTestUrl) {
                    routing {
                        post("on-behalf-of/beskjed/done") {
                            when {
                                call.request.header("fodselsnummer") == acceptedFnr && call.eventId() == "doneeventid" ->
                                    call.respond(respondWith)

                                else -> call.respond(HttpStatusCode.BadRequest)
                            }
                        }
                    }
                }
            }

        }
    }


    private fun ApplicationTestBuilder.eventhandlerHttpClient() = createClient {
        install(io.ktor.client.plugins.contentnegotiation.ContentNegotiation) {
            json(jsonConfig())
        }
        install(HttpTimeout)
    }

    private fun testApi(
        varslerFromExternalService: List<Varsel>,
        clientBuilder: HttpRequestBuilder.() -> Unit
    ): HttpResponse {
        lateinit var alleVarslerApiResponse: HttpResponse
        testApplication {
            externalServices {
                hosts(eventhandlerTestUrl) {
                    install(ContentNegotiation) { json() }
                    routing {
                        get("fetch/varsel/on-behalf-of/aktive") {
                            call.respond(HttpStatusCode.OK, varslerFromExternalService)
                        }
                    }
                }
            }

            val varselService = VarselService(
                client = eventhandlerHttpClient(),
                azureTokenFetcher = mockk(relaxed = true),
                eventHandlerBaseURL = eventhandlerTestUrl,
                eventAggregatorBaseUrl = eventaggregatorTestUrl
            )

            mockVarselbjelleApi(
                varselService = varselService
            )

            alleVarslerApiResponse = client.request { clientBuilder() }
        }
        return alleVarslerApiResponse
    }
}


private suspend fun ApplicationCall.eventId(): String =
    receive<String>().let {
        Json.parseToJsonElement(it).jsonObject["eventId"]?.jsonPrimitive?.content
            ?: throw IllegalArgumentException("eventId finnes ikke i body")
    }

