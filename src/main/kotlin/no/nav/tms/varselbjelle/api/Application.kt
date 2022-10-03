package no.nav.tms.varselbjelle.api

import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import no.nav.tms.token.support.tokendings.exchange.TokendingsServiceBuilder
import no.nav.tms.varselbjelle.api.config.Environment
import no.nav.tms.varselbjelle.api.config.HttpClientBuilder
import no.nav.tms.varselbjelle.api.notifikasjon.NotifikasjonConsumer
import no.nav.tms.varselbjelle.api.tokenx.EventhandlerTokendings

fun main() {
    val environment = Environment()

    val tokendingsService = TokendingsServiceBuilder.buildTokendingsService()
    val eventhandlerTokendings = EventhandlerTokendings(tokendingsService, environment.eventhandlerClientId)

    val httpClient = HttpClientBuilder.build()
    val notifikasjonConsumer = NotifikasjonConsumer(httpClient, eventhandlerTokendings, environment.eventHandlerURL)

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
