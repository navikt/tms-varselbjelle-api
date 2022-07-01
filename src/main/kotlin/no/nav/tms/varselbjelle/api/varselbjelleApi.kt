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
import io.ktor.response.respond
import io.ktor.routing.routing
import io.ktor.serialization.json
import no.nav.tms.varselbjelle.api.config.jsonConfig
import no.nav.tms.varselbjelle.api.health.HealthService
import no.nav.tms.varselbjelle.api.health.healthApi
import no.nav.tms.varselbjelle.api.notifikasjon.NotifikasjonConsumer

fun Application.varselbjelleApi(
    jwkProvider: JwkProvider,
    jwtIssuer: String,
    jwtAudience: String,
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
                    "Token må inneholde fødselsnummer for personen i enten pid claim"
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
    }

    install(ContentNegotiation) {
        json(jsonConfig())
    }

    routing {
        healthApi(healthService)

        authenticate {
            varsel(notifikasjonConsumer, varselsideUrl)
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