package no.nav.tms.varselbjelle.api

import io.ktor.application.Application
import io.ktor.client.HttpClient
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import io.mockk.mockk
import no.nav.tms.varselbjelle.api.notifikasjon.NotifikasjonConsumer

private const val testIssuer = "test-issuer"
private val jwtStub = JwtStub(testIssuer)
private val stubToken = jwtStub.createTokenFor("subject", "audience")

fun mockVarselbjelleApi(
    httpClient: HttpClient = mockk(relaxed = true),
    corsAllowedOrigins: String = "*.nav.no",
    corsAllowedSchemes: String = "https",
    corsAllowedHeaders: List<String> = listOf(""),
    notifikasjonConsumer: NotifikasjonConsumer = mockk(relaxed = true),
    varselsideUrl: String = "localhost"
): Application.() -> Unit {
    return fun Application.() {
        varselbjelleApi(
            jwkProvider = jwtStub.stubbedJwkProvider(),
            jwtIssuer = testIssuer,
            jwtAudience = "audience",
            httpClient = httpClient,
            corsAllowedOrigins = corsAllowedOrigins,
            corsAllowedSchemes = corsAllowedSchemes,
            corsAllowedHeaders = corsAllowedHeaders,
            notifikasjonConsumer = notifikasjonConsumer,
            varselsideUrl = varselsideUrl
        )
    }
}

fun TestApplicationEngine.autentisert(
    httpMethod: HttpMethod = HttpMethod.Get,
    endepunkt: String,
    token: String = stubToken,
    body: String? = null
) = handleRequest(httpMethod, endepunkt) {
    addHeader(HttpHeaders.ContentType, "application/json")
    addHeader(HttpHeaders.Cookie, "selvbetjening-idtoken=$token")
    body?.also { setBody(it) }
}