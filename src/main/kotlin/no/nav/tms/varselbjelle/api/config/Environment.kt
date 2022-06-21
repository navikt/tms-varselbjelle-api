package no.nav.tms.varselbjelle.api.config

import no.nav.personbruker.dittnav.common.util.config.StringEnvVar.getEnvVar

data class Environment(
    val corsAllowedOrigins: String = getEnvVar("CORS_ALLOWED_ORIGINS"),
    val eventHandlerURL: String = getEnvVar("EVENT_HANDLER_URL"),
    val varselsideUrl: String = getEnvVar("VARSELSIDE_URL")
)
