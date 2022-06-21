package no.nav.tms.varselbjelle.api.config

import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import no.nav.tms.varselbjelle.api.health.HealthService
import no.nav.tms.varselbjelle.api.notifikasjon.NotifikasjonConsumer
import no.nav.tms.varselbjelle.api.varselbjelleApi

fun main() {

    val httpClient = HttpClientBuilder.build()
    val healthService = HealthService()

    val environment = Environment()

    val notifikasjonConsumer = NotifikasjonConsumer(httpClient, environment.eventHandlerURL)

    embeddedServer(Netty, port = 8080) {
        varselbjelleApi(
            healthService = healthService,
            httpClient = httpClient,
            corsAllowedOrigins = environment.corsAllowedOrigins,
            notifikasjonConsumer = notifikasjonConsumer,
            environment.varselsideUrl
        )
    }.start(wait = true)

}
