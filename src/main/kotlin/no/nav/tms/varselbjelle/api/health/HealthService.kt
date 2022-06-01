package no.nav.tms.varselbjelle.api.health

import no.nav.tms.varselbjelle.api.config.ApplicationContext

class HealthService(private val applicationContext: ApplicationContext) {

    suspend fun getHealthChecks(): List<HealthStatus> {
        return emptyList()
    }
}
