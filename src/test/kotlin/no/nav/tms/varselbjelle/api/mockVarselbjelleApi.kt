package no.nav.tms.varselbjelle.api


import io.ktor.client.HttpClient
import io.ktor.client.request.request
import io.ktor.client.request.url
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.contentType
import io.ktor.server.application.*
import io.ktor.server.testing.TestApplicationBuilder
import io.mockk.mockk
import no.nav.tms.token.support.tokenx.validation.mock.SecurityLevel
import no.nav.tms.token.support.tokenx.validation.mock.installTokenXAuthMock
import no.nav.tms.varselbjelle.api.notifikasjon.NotifikasjonConsumer


fun TestApplicationBuilder.mockVarselbjelleApi(
    httpClient: HttpClient = mockk(relaxed = true),
    corsAllowedOrigins: String = "*.nav.no",
    corsAllowedSchemes: String = "https",
    corsAllowedHeaders: List<String> = listOf(""),
    notifikasjonConsumer: NotifikasjonConsumer = mockk(relaxed = true),
    varselsideUrl: String = "localhost",
    authMockInstaller: Application.() -> Unit = installMock()
) {
    application {

        varselbjelleApi(
            httpClient = httpClient,
            corsAllowedOrigins = corsAllowedOrigins,
            corsAllowedSchemes = corsAllowedSchemes,
            corsAllowedHeaders = corsAllowedHeaders,
            notifikasjonConsumer = notifikasjonConsumer,
            varselsideUrl = varselsideUrl,
            authInstaller = authMockInstaller
        )
    }
}

private fun installMock(): Application.() -> Unit = {
    installTokenXAuthMock {
        setAsDefault = true
        alwaysAuthenticated = true
        staticUserPid = "123"
        staticSecurityLevel = SecurityLevel.LEVEL_3
    }
}

internal suspend fun HttpClient.authenticatedGet(endpoint: String) =
    request {
        url(endpoint)
        method = HttpMethod.Get
        contentType(ContentType.Application.Json)
    }

