package no.nav.tms.varselbjelle.api


import io.ktor.client.HttpClient
import io.ktor.server.application.*
import io.ktor.server.testing.TestApplicationBuilder
import io.mockk.mockk
import no.nav.tms.token.support.azure.validation.mock.installAzureAuthMock
import no.nav.tms.varselbjelle.api.varsel.EventHandlerConsumer


fun TestApplicationBuilder.mockVarselbjelleApi(
    httpClient: HttpClient = mockk(relaxed = true),
    corsAllowedOrigins: String = "*.nav.no",
    corsAllowedSchemes: String = "https",
    corsAllowedHeaders: List<String> = listOf(""),
    notifikasjonConsumer: EventHandlerConsumer = mockk(relaxed = true),
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
    installAzureAuthMock {
        setAsDefault = true
        alwaysAuthenticated = true
    }
}
