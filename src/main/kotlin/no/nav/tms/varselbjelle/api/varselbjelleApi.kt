package no.nav.tms.varselbjelle.api

import io.ktor.application.Application
import io.ktor.application.ApplicationStopping
import io.ktor.application.install
import io.ktor.client.HttpClient
import io.ktor.features.CORS
import io.ktor.features.ContentNegotiation
import io.ktor.features.DefaultHeaders
import io.ktor.http.HttpHeaders
import io.ktor.routing.routing
import io.ktor.serialization.json
import no.nav.tms.varselbjelle.api.config.jsonConfig
import no.nav.tms.varselbjelle.api.health.HealthService
import no.nav.tms.varselbjelle.api.health.healthApi
import no.nav.tms.varselbjelle.api.notifikasjon.NotifikasjonConsumer

fun Application.varselbjelleApi(
    healthService: HealthService,
    httpClient: HttpClient,
    corsAllowedOrigins: String,
    notifikasjonConsumer: NotifikasjonConsumer,
    varselsideUrl: String
) {

    install(DefaultHeaders)

    install(CORS) {
        host(corsAllowedOrigins)
        allowCredentials = true
        header(HttpHeaders.ContentType)
    }

    install(ContentNegotiation) {
        json(jsonConfig())
    }

    routing {
        healthApi(healthService)
        varsel(notifikasjonConsumer, varselsideUrl)
    }

    configureShutdownHook(httpClient)
}

private fun Application.configureShutdownHook(httpClient: HttpClient) {
    environment.monitor.subscribe(ApplicationStopping) {
        httpClient.close()
    }
}
