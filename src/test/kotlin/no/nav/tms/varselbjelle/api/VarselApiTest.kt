package no.nav.tms.varselbjelle.api

import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.request.*
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.*
import io.ktor.http.HttpMethod.Companion.Get
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.request.header
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import no.nav.tms.varselbjelle.api.config.jsonConfig
import no.nav.tms.varselbjelle.api.varsel.Varsel
import no.nav.tms.varselbjelle.api.varsel.VarselService
import no.nav.tms.varselbjelle.api.varsel.VarselType
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.lang.IllegalArgumentException
import java.time.ZoneOffset.UTC
import java.time.ZonedDateTime


class VarselApiTest {
    private val eventhandlerTestUrl = "https://test.eventhandler.no"
    private val eventaggregatorTestUrl = "https://test.eventaggregator.no"
    private val acceptedFnr = "54235437876"

    @Test
    fun `ende til ende-test av notifikasjon til varselbjelle-varsel`() {
        val varsler = listOf(testVarsel(VarselType.BESKJED))

        val response = testApi(varslerFromExternalService = varsler) {
            url("tms-varselbjelle-api/varsel/sammendrag")
            method = Get
            header("fodselsnummer", "12345678912")
            header("auth_level", "4")
        }

        runBlocking {
            response.status shouldBe HttpStatusCode.OK
            val sammendragsVarselDto = Json.decodeFromString<VarselbjelleResponse>(response.bodyAsText())
            sammendragsVarselDto.varsler.totaltAntallUleste shouldBe 1
            sammendragsVarselDto.varsler.nyesteVarsler shouldHaveSize 1
            sammendragsVarselDto.varsler.nyesteVarsler.first().varseltekst shouldBe "Du har 1 varsel"
        }
    }

    @Test
    fun `Returner varsel med nødvendige felter`() {
        val varsel = testVarsel(
            varselType = VarselType.BESKJED,
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
        }
    }

    @Test
    fun `Returner liste av varsler`() {
        val varsler = listOf(
            testVarsel(VarselType.BESKJED),
            testVarsel(VarselType.OPPGAVE),
            testVarsel(VarselType.OPPGAVE),
            testVarsel(VarselType.INNBOKS),
            testVarsel(VarselType.INNBOKS),
            testVarsel(VarselType.INNBOKS),
        )

        val response = testApi(varslerFromExternalService = varsler) {
            url("tms-varselbjelle-api/varsel/aktive")
            method = Get
            header("fodselsnummer", "12345678912")
            header("auth_level", "4")
        }

        runBlocking {
            response.status shouldBe HttpStatusCode.OK
            val varslerGroupedByType = Json.decodeFromString<VarselbjelleVarslerByType>(response.bodyAsText())
            varslerGroupedByType.beskjeder.size shouldBe 1
            varslerGroupedByType.oppgaver.size shouldBe 2
            varslerGroupedByType.innbokser.size shouldBe 3
        }
    }

    @Test
    fun `maskerer varselinnhold ved for lavt innlogginsnivå`() {
        val varsler = listOf(
            testVarsel(VarselType.BESKJED, sikkerhetsnivaa = 4),
        )

        val response = testApi(varslerFromExternalService = varsler) {
            url("tms-varselbjelle-api/varsel/aktive")
            method = Get
            header("fodselsnummer", "12345678912")
            header("auth_level", "3")
        }

        runBlocking {
            response.status shouldBe HttpStatusCode.OK
            val varslerGroupedByType = Json.decodeFromString<VarselbjelleVarslerByType>(response.bodyAsText())

            val varsel = varslerGroupedByType.beskjeder.first()
            varsel.isMasked shouldBe true
            varsel.link shouldBe null
            varsel.tekst shouldBe null
        }
    }

    @Nested
    inner class DoneEndpoint {

        private val defaultBody="""{ "eventId": "doneeventid"}""".trimMargin()
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
                        post("beskjed/done") {
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

    private fun testVarsel(
        varselType: VarselType,
        eventId: String = "123",
        forstbehandlet: ZonedDateTime = ZonedDateTime.now(UTC),
        sikkerhetsnivaa: Int = 4,
        tekst: String = "teekstæøå",
        link: String = "liink"
    ): Varsel =
        Varsel(
            eventId = eventId,
            forstBehandlet = forstbehandlet,
            type = varselType,
            sikkerhetsnivaa = sikkerhetsnivaa,
            tekst = tekst,
            link = link
        )
}


private suspend fun ApplicationCall.eventId(): String =
    receive<String>().let {
        Json.parseToJsonElement(it).jsonObject["eventId"]?.jsonPrimitive?.content
            ?: throw IllegalArgumentException("eventId finnes ikke i body")
    }