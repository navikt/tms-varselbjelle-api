package no.nav.tms.varselbjelle.api

import com.auth0.jwk.JwkProviderBuilder
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import no.nav.tms.token.support.tokendings.exchange.TokendingsServiceBuilder
import no.nav.tms.varselbjelle.api.config.Environment
import no.nav.tms.varselbjelle.api.config.HttpClientBuilder
import no.nav.tms.varselbjelle.api.health.HealthService
import no.nav.tms.varselbjelle.api.notifikasjon.NotifikasjonConsumer
import no.nav.tms.varselbjelle.api.tokenx.EventhandlerTokendings
import java.net.URL
import java.util.concurrent.TimeUnit

fun main() {

    val httpClient = HttpClientBuilder.build()
    val healthService = HealthService()

    val environment = Environment()

    val tokendingsService = TokendingsServiceBuilder.buildTokendingsService()
    val eventhandlerTokendings = EventhandlerTokendings(tokendingsService, environment.eventhandlerClientId)

    val notifikasjonConsumer = NotifikasjonConsumer(httpClient, eventhandlerTokendings, environment.eventHandlerURL)

    val jwkProvider = JwkProviderBuilder(URL(environment.jwksUrl))
        .cached(10, 24, TimeUnit.HOURS)
        .rateLimited(10, 1, TimeUnit.MINUTES)
        .build()

    embeddedServer(Netty, port = 8080) {
        varselbjelleApi(
            jwkProvider = jwkProvider,
            jwtIssuer = "selvbetjening",
            jwtAudience = environment.loginserviceIdportenAudience,
            healthService = healthService,
            httpClient = httpClient,
            corsAllowedOrigins = environment.corsAllowedOrigins,
            notifikasjonConsumer = notifikasjonConsumer,
            varselsideUrl = environment.varselsideUrl
        )
    }.start(wait = true)

}
