package no.nav.tms.varselbjelle.api.config

import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import no.nav.tms.varselbjelle.api.health.HealthService

fun main() {

    val httpClient = HttpClientBuilder.build()
    val healthService = HealthService()

    val environment = Environment()

    embeddedServer(Netty, port = 8080) {
        varselbjelleApi(healthService, httpClient, environment.corsAllowedOrigins)
    }.start(wait = true)

}
