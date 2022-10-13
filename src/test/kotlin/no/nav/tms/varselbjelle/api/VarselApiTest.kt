package no.nav.tms.varselbjelle.api

import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.request.*
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.*
import io.ktor.http.HttpMethod.Companion.Get
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.ktor.server.testing.testApplication
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import no.nav.tms.varselbjelle.api.config.jsonConfig
import no.nav.tms.varselbjelle.api.varsel.Varsel
import no.nav.tms.varselbjelle.api.varsel.EventHandlerConsumer
import no.nav.tms.varselbjelle.api.varsel.VarselType
import org.junit.jupiter.api.Test
import java.time.ZoneOffset.UTC
import java.time.ZonedDateTime

class VarselApiTest {
    private val eventhandlerTestUrl = "https://test.eventhandler.no"

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
            url("tms-varselbjelle-api/varsel/alle")
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
            url("tms-varselbjelle-api/varsel/alle")
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
            testVarsel(VarselType.BESKJED, sikkerhetsnivaa = 3),
        )

        val response = testApi(varslerFromExternalService = varsler) {
            url("tms-varselbjelle-api/varsel/alle")
            method = Get
            header("fodselsnummer", "12345678912")
            header("auth_level", "4")
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

            val eventHandlerHttpClient = createClient {
                install(io.ktor.client.plugins.contentnegotiation.ContentNegotiation) {
                    json(jsonConfig())
                }
                install(HttpTimeout)
            }
            val notifikasjonConsumer = EventHandlerConsumer(
                client = eventHandlerHttpClient,
                eventhandlerTokendings = mockk(relaxed = true),
                eventHandlerBaseURL = eventhandlerTestUrl
            )

            mockVarselbjelleApi(
                notifikasjonConsumer = notifikasjonConsumer
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
        tekst: String = "teekst",
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