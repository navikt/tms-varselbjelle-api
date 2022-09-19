package no.nav.tms.varselbjelle.api


import io.ktor.client.HttpClient
import io.ktor.client.request.cookie
import io.ktor.client.request.request
import io.ktor.client.request.url
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.contentType
import io.ktor.server.testing.TestApplicationBuilder
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import io.mockk.mockk
import no.nav.tms.varselbjelle.api.notifikasjon.NotifikasjonConsumer

private const val testIssuer = "test-issuer"
private val jwtStub = JwtStub(testIssuer)
internal val stubToken = jwtStub.createTokenFor("subject", "audience")

fun TestApplicationBuilder.mockVarselbjelleApi(
    httpClient: HttpClient = mockk(relaxed = true),
    corsAllowedOrigins: String = "*.nav.no",
    corsAllowedSchemes: String = "https",
    corsAllowedHeaders: List<String> = listOf(""),
    notifikasjonConsumer: NotifikasjonConsumer = mockk(relaxed = true),
    varselsideUrl: String = "localhost"
) {
    application {
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


internal suspend fun HttpClient.authenticatedGet(endpoint: String) =
    request {
        url(endpoint)
        method = HttpMethod.Get
        contentType(ContentType.Application.Json)
        cookie("selvbetjening-idtoken", stubToken)
    }

