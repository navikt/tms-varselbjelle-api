package no.nav.tms.varselbjelle.api

import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.withTestApplication
import io.mockk.mockk
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import no.nav.tms.varselbjelle.api.config.HttpClientBuilder
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

        val notifikasjonHttpClient: HttpClient = HttpClientBuilder.build(MockEngine {
            respond(
                Json.encodeToString(notifikasjoner),
                HttpStatusCode.OK,
                headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            )
        })

        val eventhandlerTokendings: EventhandlerTokendings = mockk(relaxed = true)
        val notifikasjonConsumer = NotifikasjonConsumer(
            client = notifikasjonHttpClient,
            eventhandlerTokendings = eventhandlerTokendings,
            eventHandlerBaseURL = "http://localhost"
        )

        val response = withTestApplication(
            mockVarselbjelleApi(
                httpClient = notifikasjonHttpClient,
                notifikasjonConsumer = notifikasjonConsumer
            )
        ) {
            autentisert(HttpMethod.Get, "tms-varselbjelle-api/rest/varsel/hentsiste")
        }.response

        response.status() shouldBe HttpStatusCode.OK

        val sammendragsVarselDto = Json.decodeFromString<VarselbjelleResponse>(response.content!!)
        sammendragsVarselDto.varsler.totaltAntallUleste shouldBe 1
        sammendragsVarselDto.varsler.nyesteVarsler shouldHaveSize 1
        sammendragsVarselDto.varsler.nyesteVarsler.first().varseltekst shouldBe "Du har 1 varsel"
    }

    @Test
    fun `gi 401 ved manglende cookie`() {
        val response = withTestApplication(
            mockVarselbjelleApi()
        ) {
            handleRequest(HttpMethod.Get, "tms-varselbjelle-api/rest/varsel/hentsiste")
        }.response

        response.status() shouldBe HttpStatusCode.Unauthorized
    }
}
