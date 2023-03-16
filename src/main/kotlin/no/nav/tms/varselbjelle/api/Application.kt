package no.nav.tms.varselbjelle.api

import io.ktor.client.*
import io.ktor.client.engine.*
import io.ktor.client.engine.apache.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import no.nav.tms.token.support.azure.exchange.AzureServiceBuilder
import no.nav.tms.varselbjelle.api.config.Environment
import no.nav.tms.varselbjelle.api.varsel.VarselService
import no.nav.tms.varselbjelle.api.azure.AzureTokenFetcher
import no.nav.tms.varselbjelle.api.config.HttpClientBuilder
import no.nav.tms.varselbjelle.api.config.jsonConfig

fun main() {
    val environment = Environment()

    val tokendingsService = AzureServiceBuilder.buildAzureService()
    val eventhandlerTokendings =
        AzureTokenFetcher(
            tokendingsService = tokendingsService,
            eventhandlerClientId = environment.eventhandlerClientId,
            eventaggregatorClientId = environment.eventaggregatorClientId
        )

    val httpClient = HttpClientBuilder.build()
    val varselService =
        VarselService(
            client = httpClient,
            azureTokenFetcher = eventhandlerTokendings,
            eventHandlerBaseURL = environment.eventHandlerURL,
            eventAggregatorBaseUrl = environment.eventAggregatorURL
        )

    embeddedServer(Netty, port = 8080) {
        varselbjelleApi(
            httpClient = httpClient,
            corsAllowedOrigins = environment.corsAllowedOrigins,
            corsAllowedSchemes = environment.corsAllowedSchemes,
            corsAllowedHeaders = environment.corsAllowedHeaders,
            varselService = varselService,
            varselsideUrl = environment.varselsideUrl
        )
    }.start(wait = true)
}

