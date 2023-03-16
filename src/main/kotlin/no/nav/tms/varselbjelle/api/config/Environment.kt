package no.nav.tms.varselbjelle.api.config

import io.ktor.client.*
import io.ktor.client.engine.*
import io.ktor.client.engine.apache.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import no.nav.personbruker.dittnav.common.util.config.StringEnvVar
import no.nav.personbruker.dittnav.common.util.config.StringEnvVar.getEnvVar

data class Environment(
    val corsAllowedOrigins: String = getEnvVar("CORS_ALLOWED_ORIGINS"),
    val corsAllowedSchemes: String = getEnvVar("CORS_ALLOWED_SCHEMES","https"),
    val corsAllowedHeaders: List<String> = StringEnvVar.getEnvVarAsList("CORS_ALLOWED_HEADERS"),
    val eventHandlerURL: String = getEnvVar("EVENT_HANDLER_URL"),
    val eventAggregatorURL: String = getEnvVar("EVENT_AGGREGATOR_URL"),
    val eventhandlerClientId: String = getEnvVar("EVENTHANDLER_CLIENT_ID"),
    val eventaggregatorClientId: String = getEnvVar("EVENTAGGREGATOR_CLIENT_ID"),
    val varselsideUrl: String = getEnvVar("VARSELSIDE_URL")
)

object HttpClientBuilder {

    fun build(httpClientEngine: HttpClientEngine = Apache.create()): HttpClient {
        return HttpClient(httpClientEngine) {
            install(ContentNegotiation) {
                json(jsonConfig())
            }
            install(HttpTimeout)
        }
    }
}