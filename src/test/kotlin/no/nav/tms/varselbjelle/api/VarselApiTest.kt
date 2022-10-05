package no.nav.tms.varselbjelle.api

import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.request.*
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
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import io.mockk.mockk
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import no.nav.tms.varselbjelle.api.config.HttpClientBuilder
import no.nav.tms.varselbjelle.api.config.jsonConfig
import no.nav.tms.varselbjelle.api.varsel.Varsel
import no.nav.tms.varselbjelle.api.varsel.EventHandlerConsumer
import no.nav.tms.varselbjelle.api.azure.EventhandlerTokenFetcher
import org.junit.jupiter.api.Test
import java.time.ZoneOffset.UTC
import java.time.ZonedDateTime

class VarselApiTest {

    @Test
    fun `ende til ende-test av notifikasjon til varselbjelle-varsel`() {

        val varsler = listOf(
            Varsel(
                forstBehandlet = ZonedDateTime.now(UTC),
            )
        )
        val eventhandlerTestUrl = "https://test.eventhandler.no"
        val notifikasjonHttpClient: HttpClient = HttpClientBuilder.build()

        val eventhandlerTokendings: EventhandlerTokenFetcher = mockk(relaxed = true)

        testApplication {
            externalServices {
                hosts(eventhandlerTestUrl) {
                    install(ContentNegotiation) { json() }
                    routing {
                        get("fetch/varsel/on-behalf-of/aktive") {
                            call.respond(HttpStatusCode.OK, varsler)
                        }
                    }
                }
            }

            val notifikasjonConsumer = EventHandlerConsumer(
                client = applicationHttpClient(),
                eventhandlerTokendings = eventhandlerTokendings,
                eventHandlerBaseURL = eventhandlerTestUrl
            )
            mockVarselbjelleApi(
                httpClient = notifikasjonHttpClient,
                notifikasjonConsumer = notifikasjonConsumer
            )
            val response = client.request {
                url("tms-varselbjelle-api/varsel/sammendrag")
                method = Get
                header("fodselsnummer", "12345678912")
                header("auth_level", "4")
            }

            response.status shouldBe HttpStatusCode.OK
            val sammendragsVarselDto = Json.decodeFromString<VarselbjelleResponse>(response.bodyAsText())
            sammendragsVarselDto.varsler.totaltAntallUleste shouldBe 1
            sammendragsVarselDto.varsler.nyesteVarsler shouldHaveSize 1
            sammendragsVarselDto.varsler.nyesteVarsler.first().varseltekst shouldBe "Du har 1 varsel"
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

