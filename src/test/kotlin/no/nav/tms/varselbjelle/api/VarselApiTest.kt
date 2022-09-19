package no.nav.tms.varselbjelle.api

import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.request.cookie
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.request
import io.ktor.client.request.url
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.config.MapApplicationConfig
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.TestApplicationBuilder
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.testApplication
import io.ktor.server.testing.withTestApplication
import io.mockk.mockk
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import no.nav.tms.varselbjelle.api.config.HttpClientBuilder
import no.nav.tms.varselbjelle.api.config.get
import no.nav.tms.varselbjelle.api.config.jsonConfig
import no.nav.tms.varselbjelle.api.notifikasjon.Notifikasjon
import no.nav.tms.varselbjelle.api.notifikasjon.NotifikasjonConsumer
import no.nav.tms.varselbjelle.api.tokenx.EventhandlerTokendings
import org.junit.jupiter.api.Test
import java.time.ZoneId
import java.time.ZonedDateTime

class VarselApiTest {

    @Test
    fun `ende til ende-test av notifikasjon til varselbjelle-varsel`() {

        val notifikasjoner = listOf(
            Notifikasjon(
                forstBehandlet = ZonedDateTime.of(2020, 1, 1, 1, 1, 1, 1, ZoneId.of("Europe/Oslo")),
            )
        )
        val eventhandlerTestUrl = "https://test.eventhandler.no"
        val notifikasjonHttpClient: HttpClient = HttpClientBuilder.build()

        val eventhandlerTokendings: EventhandlerTokendings = mockk(relaxed = true)

        testApplication {
            externalServices {
                hosts(eventhandlerTestUrl) {
                    install(ContentNegotiation) { json() }
                    routing {
                        get("fetch/event/aktive") {
                            call.respond(HttpStatusCode.OK, notifikasjoner)
                        }
                    }
                }
            }

            val notifikasjonConsumer = NotifikasjonConsumer(
                client = applicationHttpClient(),
                eventhandlerTokendings = eventhandlerTokendings,
                eventHandlerBaseURL = eventhandlerTestUrl
            )
            mockVarselbjelleApi(
                httpClient = notifikasjonHttpClient,
                notifikasjonConsumer = notifikasjonConsumer
            )
            val response = client.authenticatedGet("tms-varselbjelle-api/rest/varsel/hentsiste")

            response.status shouldBe HttpStatusCode.OK
            val sammendragsVarselDto = Json.decodeFromString<VarselbjelleResponse>(response.bodyAsText())
            sammendragsVarselDto.varsler.totaltAntallUleste shouldBe 1
            sammendragsVarselDto.varsler.nyesteVarsler shouldHaveSize 1
            sammendragsVarselDto.varsler.nyesteVarsler.first().varseltekst shouldBe "Du har 1 varsel"
        }
    }

    @Test
    fun `gi 401 ved manglende cookie`() {
        testApplication {
            mockVarselbjelleApi()
            client.get("tms-varselbjelle-api/rest/varsel/hentsiste").status shouldBe HttpStatusCode.Unauthorized
        }

    }
}

private fun ApplicationTestBuilder.applicationHttpClient() =
    createClient {
        install(io.ktor.client.plugins.contentnegotiation.ContentNegotiation) {
            json(jsonConfig())
        }
        install(HttpTimeout)
    }

