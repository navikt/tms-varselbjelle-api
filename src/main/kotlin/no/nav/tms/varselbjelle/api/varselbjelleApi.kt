package no.nav.tms.varselbjelle.api

import io.ktor.client.HttpClient
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationStopping
import io.ktor.server.application.install
import io.ktor.server.auth.authenticate
import io.ktor.server.metrics.micrometer.MicrometerMetrics
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.plugins.defaultheaders.DefaultHeaders
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry
import mu.KotlinLogging
import no.nav.tms.token.support.azure.validation.installAzureAuth
import no.nav.tms.varselbjelle.api.config.jsonConfig
import no.nav.tms.varselbjelle.api.health.healthApi
import no.nav.tms.varselbjelle.api.varsel.VarselService

fun Application.varselbjelleApi(
    httpClient: HttpClient,
    corsAllowedOrigins: String,
    corsAllowedSchemes: String,
    corsAllowedHeaders: List<String>,
    varselService: VarselService,
    varselsideUrl: String,
    authInstaller: Application.() -> Unit = azureInstaller()
) {
    val collectorRegistry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)

    install(DefaultHeaders)

    authInstaller()

    install(StatusPages) {
        val logger = KotlinLogging.logger {}
        exception<Throwable> { call, cause ->
            when(cause){
                is IllegalArgumentException -> {
                    call.respond(HttpStatusCode.BadRequest, cause.message?:"")
                    logger.info("Bad request til varselbjelleApi: $cause, ${cause.message.toString()}")

                }
                else -> {
                    call.respond(HttpStatusCode.InternalServerError)
                    logger.error("Feil i varselbjelleApi: $cause, ${cause.message.toString()}")
                }
            }
        }
    }

    install(CORS) {
        allowCredentials = true
        allowHost(corsAllowedOrigins, schemes = listOf(corsAllowedSchemes))
        allowHeader(HttpHeaders.ContentType)
        corsAllowedHeaders.forEach { approvedHeader ->
            allowHeader(approvedHeader)
        }
    }

    install(ContentNegotiation) {
        json(jsonConfig())
    }

    install(MicrometerMetrics) {
        registry = collectorRegistry
    }

    routing {
        route("/tms-varselbjelle-api") {
            healthApi(collectorRegistry)

            authenticate {
                varsel(varselService, varselsideUrl)
            }
        }
    }

    configureShutdownHook(httpClient)
}

private fun Application.configureShutdownHook(httpClient: HttpClient) {
    environment.monitor.subscribe(ApplicationStopping) {
        httpClient.close()
    }
}

private fun azureInstaller(): Application.() -> Unit = {
    installAzureAuth {
        setAsDefault = true
    }
}
