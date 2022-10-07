package no.nav.tms.varselbjelle.api.config

import no.nav.personbruker.dittnav.common.util.config.StringEnvVar
import no.nav.personbruker.dittnav.common.util.config.StringEnvVar.getEnvVar

data class Environment(
    val corsAllowedOrigins: String = getEnvVar("CORS_ALLOWED_ORIGINS"),
    val corsAllowedSchemes: String = getEnvVar("CORS_ALLOWED_SCHEMES","https"),
    val corsAllowedHeaders: List<String> = StringEnvVar.getEnvVarAsList("CORS_ALLOWED_HEADERS"),
    val eventHandlerURL: String = getEnvVar("EVENT_HANDLER_URL"),
    val eventhandlerClientId: String = getEnvVar("EVENTHANDLER_CLIENT_ID"),
    val varselsideUrl: String = getEnvVar("VARSELSIDE_URL")
)
