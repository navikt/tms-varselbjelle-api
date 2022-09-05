package no.nav.tms.varselbjelle.api

import com.auth0.jwk.JwkProvider
import com.auth0.jwt.interfaces.Claim
import io.ktor.application.Application
import io.ktor.application.ApplicationStopping
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.auth.Authentication
import io.ktor.auth.authenticate
import io.ktor.auth.jwt.JWTPrincipal
import io.ktor.auth.jwt.jwt
import io.ktor.client.HttpClient
import io.ktor.features.CORS
import io.ktor.features.ContentNegotiation
import io.ktor.features.DefaultHeaders
import io.ktor.features.StatusPages
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.auth.HttpAuthHeader
import io.ktor.metrics.micrometer.MicrometerMetrics
import io.ktor.request.path
import io.ktor.response.respond
import io.ktor.routing.route
import io.ktor.routing.routing
import io.ktor.serialization.json
import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry
import mu.KotlinLogging
import no.nav.tms.varselbjelle.api.config.jsonConfig
import no.nav.tms.varselbjelle.api.health.healthApi
import no.nav.tms.varselbjelle.api.notifikasjon.NotifikasjonConsumer

private val logger = KotlinLogging.logger { }

fun Application.varselbjelleApi(
    jwkProvider: JwkProvider,
    jwtIssuer: String,
    jwtAudience: String,
    httpClient: HttpClient,
    corsAllowedOrigins: String,
    corsAllowedSchemes: String,
    corsAllowedHeaders: List<String>,
    notifikasjonConsumer: NotifikasjonConsumer,
    varselsideUrl: String
) {
    val collectorRegistry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)

    install(DefaultHeaders)

    install(CORS) {
        host(corsAllowedOrigins, schemes = listOf(corsAllowedSchemes))
        allowCredentials = true
        header(HttpHeaders.ContentType)
        corsAllowedHeaders.forEach { approvedHeader ->
            header(approvedHeader)
        }
    }

    install(Authentication) {
        jwt {
            verifier(jwkProvider, jwtIssuer) {
                withAudience(jwtAudience)
            }
            authHeader {
                val cookie = it.request.cookies["selvbetjening-idtoken"] ?: throw CookieNotSetException()
                HttpAuthHeader.Single("Bearer", cookie)
            }
            validate { credentials ->
                requireNotNull(credentials.payload.claims.pid()) {
                    "Token må inneholde fødselsnummer for personen i pid claim"
                }

                JWTPrincipal(credentials.payload)
            }
        }
    }

    install(StatusPages) {
        exception<CookieNotSetException> { cause ->
            val status = HttpStatusCode.Unauthorized
            call.respond(status, cause.message ?: "")
        }

        exception<Throwable> { cause ->
            logger.error(cause) { "Kall mot ${call.request.path()} feilet. Feilmelding: ${cause.message}" }
            call.respond(HttpStatusCode.InternalServerError)
        }
    }

    install(ContentNegotiation) {
        json(jsonConfig())
    }

    install(MicrometerMetrics) {
        registry = collectorRegistry
    }

    routing() {
        route("/tms-varselbjelle-api") {
            healthApi(collectorRegistry)

            authenticate {
                varsel(notifikasjonConsumer, varselsideUrl)
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

private fun <V : Claim> Map<String, V>.pid() = firstNotNullOf { it.takeIf { it.key == "pid" } }.value

private class CookieNotSetException : RuntimeException("Cookie with name selvbetjening-idtoken not found")