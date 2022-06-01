package no.nav.tms.varselbjelle.api.health

interface HealthCheck {

    suspend fun status(): HealthStatus

}
