package no.nav.tms.varselbjelle.api

import com.auth0.jwk.JwkProvider
import com.auth0.jwt.interfaces.Claim
import io.ktor.client.HttpClient
import io.ktor.http.HttpStatusCode
import io.ktor.http.auth.HttpAuthHeader
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationStopping
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
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
import no.nav.tms.varselbjelle.api.config.jsonConfig
import no.nav.tms.varselbjelle.api.health.healthApi
import no.nav.tms.varselbjelle.api.notifikasjon.NotifikasjonConsumer


fun Application.varselbjelleApi(
    jwkProvider: JwkProvider,
    jwtIssuer: String,
    jwtAudience: String,
    httpClient: HttpClient,
    //corsAllowedOrigins: String,
    //corsAllowedSchemes: String,
    //corsAllowedHeaders: List<String>,
    notifikasjonConsumer: NotifikasjonConsumer,
    varselsideUrl: String
) {
    val collectorRegistry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)

    install(DefaultHeaders)

    install(StatusPages) {
        exception<Throwable> { call, cause ->
            when (cause) {
                is CookieNotSetException -> call.respond(HttpStatusCode.Unauthorized)
            }
        }

    }

    install(CORS) {
        /*   host(corsAllowedOrigins, schemes = listOf(corsAllowedSchemes))
           allowCredentials = true
           header(HttpHeaders.ContentType)
           corsAllowedHeaders.forEach { approvedHeader ->
               header(approvedHeader)
           }*/
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