package no.nav.tms.varselbjelle.api

import io.ktor.application.Application
import io.ktor.application.ApplicationCall
import io.ktor.application.ApplicationStopping
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.auth.Authentication
import io.ktor.auth.authenticate
import io.ktor.client.HttpClient
import io.ktor.config.ApplicationConfig
import io.ktor.features.CORS
import io.ktor.features.ContentNegotiation
import io.ktor.features.DefaultHeaders
import io.ktor.http.HttpHeaders
import io.ktor.routing.routing
import io.ktor.serialization.json
import io.ktor.util.pipeline.PipelineContext
import no.nav.personbruker.dittnav.common.security.AuthenticatedUser
import no.nav.personbruker.dittnav.common.security.AuthenticatedUserFactory
import no.nav.security.token.support.ktor.tokenValidationSupport
import no.nav.tms.varselbjelle.api.config.jsonConfig
import no.nav.tms.varselbjelle.api.health.HealthService
import no.nav.tms.varselbjelle.api.health.healthApi
import no.nav.tms.varselbjelle.api.notifikasjon.NotifikasjonConsumer

fun Application.varselbjelleApi(
    healthService: HealthService,
    httpClient: HttpClient,
    corsAllowedOrigins: String,
    notifikasjonConsumer: NotifikasjonConsumer,
    varselsideUrl: String,
    installAuthenticatorsFunction: Application.() -> Unit = installAuth(this.environment.config)
) {

    install(DefaultHeaders)

    install(CORS) {
        host(corsAllowedOrigins)
        allowCredentials = true
        header(HttpHeaders.ContentType)
    }

    installAuthenticatorsFunction()

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

private fun installAuth(config: ApplicationConfig): Application.() -> Unit = {
    install(Authentication) {
        tokenValidationSupport(config = config)
    }
}

private fun Application.configureShutdownHook(httpClient: HttpClient) {
    environment.monitor.subscribe(ApplicationStopping) {
        httpClient.close()
    }
}

val PipelineContext<Unit, ApplicationCall>.authenticatedUser: AuthenticatedUser
    get() = AuthenticatedUserFactory.createNewAuthenticatedUser(call)