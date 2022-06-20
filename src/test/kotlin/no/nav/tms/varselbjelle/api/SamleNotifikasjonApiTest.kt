package no.nav.tms.varselbjelle.api

import com.fasterxml.jackson.databind.ObjectMapper
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
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import no.nav.tms.varselbjelle.api.config.HttpClientBuilder
import no.nav.tms.varselbjelle.api.notifikasjon.EventType
import no.nav.tms.varselbjelle.api.notifikasjon.Notifikasjon
import no.nav.tms.varselbjelle.api.notifikasjon.NotifikasjonHttpClient
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.time.ZoneId
import java.time.ZonedDateTime

class SamleNotifikasjonApiTest {

    @Test
    @Disabled
    fun `returnerer samle-notifikasjon på varselbjelle-formmat`() {

        val response = withTestApplication(mockVarselbjelleApi()) {
            handleRequest(HttpMethod.Get, "rest/varsel/hentsiste") {}
        }.response

        response.status() shouldBe HttpStatusCode.OK

        val varselJson = ObjectMapper().readTree(response.content)

        varselJson["nyesteVarsler"].size() shouldBe 1
        varselJson["totaltAntallUleste"].asInt() shouldBe 1
    }

    @Test
    @Disabled
    fun `ende til ende`() {

        val notifikasjonResponse = listOf(
            Notifikasjon(
                grupperingsId = "123",
                eventId = "456",
                eventTidspunkt = ZonedDateTime.of(2020, 1, 1, 1, 1, 1, 1, ZoneId.of("Europe/Oslo")),
                forstBehandlet = ZonedDateTime.of(2020, 1, 1, 1, 1, 1, 1, ZoneId.of("Europe/Oslo")),
                produsent = "produsent",
                sikkerhetsnivaa = 4,
                sistOppdatert = ZonedDateTime.of(2020, 1, 1, 1, 1, 1, 1, ZoneId.of("Europe/Oslo")),
                tekst = "tekst",
                link = "link",
                aktiv = true,
                type = EventType.BESKJED
            )
        )

        val httpClient: HttpClient = HttpClientBuilder.build(MockEngine {
            respond(
                Json.encodeToString(notifikasjonResponse),
                HttpStatusCode.OK,
                headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            )
        })

        val notifikasjonHttpClient = NotifikasjonHttpClient(httpClient, "http://localhost")

        val response = withTestApplication(
            mockVarselbjelleApi(
                httpClient = httpClient,
                notifikasjonHttpClient = notifikasjonHttpClient
            )
        ) {
            handleRequest(HttpMethod.Get, "rest/varsel/hentsiste") {}
        }.response

        response.status() shouldBe HttpStatusCode.OK

        val varselJson = ObjectMapper().readTree(response.content)

        varselJson["totaltAntallUleste"].asInt() shouldBe 1
        varselJson["nyesteVarsler"].size() shouldBe 1
    }

}
