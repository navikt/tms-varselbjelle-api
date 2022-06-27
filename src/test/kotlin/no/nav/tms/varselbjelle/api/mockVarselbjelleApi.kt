package no.nav.tms.varselbjelle.api

import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.auth.Authentication
import io.ktor.auth.AuthenticationPipeline
import io.ktor.client.HttpClient
import io.mockk.mockk
import no.nav.security.token.support.core.context.TokenValidationContext
import no.nav.security.token.support.core.jwt.JwtToken
import no.nav.security.token.support.ktor.TokenValidationContextPrincipal
import no.nav.tms.varselbjelle.api.health.HealthService
import no.nav.tms.varselbjelle.api.notifikasjon.NotifikasjonConsumer

fun mockVarselbjelleApi(
    healthService: HealthService = mockk(relaxed = true),
    httpClient: HttpClient = mockk(relaxed = true),
    corsAllowedOrigins: String = "*.nav.no",
    notifikasjonConsumer: NotifikasjonConsumer = mockk(relaxed = true),
    varselsideUrl: String = "localhost",
    installAuthenticatorsFunction: Application.() -> Unit = {
        install(Authentication) {
            provider {
                pipeline.intercept(AuthenticationPipeline.RequestAuthentication) { context ->
                    context.principal(
                        TokenValidationContextPrincipal(
                            TokenValidationContext(mapOf("dummyName" to JwtToken(dummyValidJwt)))
                        )
                    )
                }
            }
        }
    }
): Application.() -> Unit {
    return fun Application.() {
        varselbjelleApi(
            healthService = healthService,
            httpClient = httpClient,
            corsAllowedOrigins = corsAllowedOrigins,
            notifikasjonConsumer = notifikasjonConsumer,
            varselsideUrl = varselsideUrl,
            installAuthenticatorsFunction = installAuthenticatorsFunction
        )
    }
}

/* dummyJwt:
{
    "acr_values":"Level4",
    "acr":"Level4",
    "pid":"123",
    "sub":"234",
    "exp":4000000000,
    "iat":1000000000,
    "jti":"STUB"
}
*/
private const val dummyValidJwt =
    "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhY3JfdmFsdWVzIjoiTGV2ZWw0IiwiYWNyIjoiTGV2ZWw0IiwicGlkIjoiMTIzIiwic3ViIjoiMjM0IiwiZXhwIjo0MDAwMDAwMDAwLCJpYXQiOjE2NDk2NzQ3MDUsImp0aSI6IlNUVUIifQ.IrMe_wIepEessc5cnfyfmmtS_1YY6ZqpCe5rTzKHz6w"
