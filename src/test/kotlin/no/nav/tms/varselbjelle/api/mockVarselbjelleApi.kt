package no.nav.tms.varselbjelle.api

import io.ktor.application.Application
import io.ktor.client.HttpClient
import io.mockk.mockk
import no.nav.tms.varselbjelle.api.config.varselbjelleApi
import no.nav.tms.varselbjelle.api.health.HealthService
import no.nav.tms.varselbjelle.api.notifikasjon.NotifikasjonHttpClient

fun mockVarselbjelleApi(
    healthService: HealthService = mockk(relaxed = true),
    httpClient: HttpClient = mockk(relaxed = true),
    corsAllowedOrigins: String = "*.nav.no",
    notifikasjonHttpClient: NotifikasjonHttpClient = mockk(relaxed = true)
): Application.() -> Unit {
    return fun Application.() {
        varselbjelleApi(
            healthService = healthService,
            httpClient = httpClient,
            corsAllowedOrigins = corsAllowedOrigins,
            notifikasjonHttpClient = notifikasjonHttpClient
        )
    }
}