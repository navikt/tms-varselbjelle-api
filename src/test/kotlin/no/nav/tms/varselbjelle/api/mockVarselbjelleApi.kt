package no.nav.tms.varselbjelle.api


import io.ktor.client.HttpClient
import io.ktor.server.application.*
import io.ktor.server.testing.TestApplicationBuilder
import io.mockk.mockk
import no.nav.tms.token.support.azure.validation.mock.installAzureAuthMock
import no.nav.tms.varselbjelle.api.config.HttpClientBuilder
import no.nav.tms.varselbjelle.api.varsel.Varsel
import no.nav.tms.varselbjelle.api.varsel.VarselService
import no.nav.tms.varselbjelle.api.varsel.VarselType
import java.time.ZoneOffset
import java.time.ZonedDateTime


fun TestApplicationBuilder.mockVarselbjelleApi(
    httpClient: HttpClient = HttpClientBuilder.build(),
    corsAllowedOrigins: String = "*.nav.no",
    corsAllowedSchemes: String = "https",
    corsAllowedHeaders: List<String> = listOf(""),
    varselService: VarselService = mockk(relaxed = true),
    varselsideUrl: String = "localhost",
    authMockInstaller: Application.() -> Unit = installMock()
) {
    application {

        varselbjelleApi(
            httpClient = httpClient,
            corsAllowedOrigins = corsAllowedOrigins,
            corsAllowedSchemes = corsAllowedSchemes,
            corsAllowedHeaders = corsAllowedHeaders,
            varselService = varselService,
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

internal operator fun Varsel.times(size: Int): List<Varsel> = mutableListOf<Varsel>().also { list ->
    for (i in 1..size) {
        list.add(this)
    }
}

internal inline fun <T> T.assert(block: T.() -> Unit): T =
    apply {
        block()
    }

internal fun testVarsel(
    varselType: VarselType,
    eventId: String = "123",
    forstbehandlet: ZonedDateTime = ZonedDateTime.now(ZoneOffset.UTC),
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