package no.nav.tms.varselbjelle.api

import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import no.nav.tms.token.support.azure.exchange.AzureServiceBuilder
import no.nav.tms.varselbjelle.api.config.Environment
import no.nav.tms.varselbjelle.api.config.HttpClientBuilder
import no.nav.tms.varselbjelle.api.varsel.EventHandlerConsumer
import no.nav.tms.varselbjelle.api.azure.EventhandlerTokenFetcher

fun main() {
    val environment = Environment()

    val tokendingsService = AzureServiceBuilder.buildAzureService()
    val eventhandlerTokendings = EventhandlerTokenFetcher(tokendingsService, environment.eventhandlerClientId)

    val httpClient = HttpClientBuilder.build()
    val notifikasjonConsumer = EventHandlerConsumer(httpClient, eventhandlerTokendings, environment.eventHandlerURL)

    embeddedServer(Netty, port = 8080) {
        varselbjelleApi(
            httpClient = httpClient,
            corsAllowedOrigins = environment.corsAllowedOrigins,
            corsAllowedSchemes = environment.corsAllowedSchemes,
            corsAllowedHeaders = environment.corsAllowedHeaders,
            notifikasjonConsumer = notifikasjonConsumer,
            varselsideUrl = environment.varselsideUrl
        )
    }.start(wait = true)
}
