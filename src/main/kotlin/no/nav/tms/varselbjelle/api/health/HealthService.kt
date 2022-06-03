package no.nav.tms.varselbjelle.api.health

class HealthService() {

    suspend fun getHealthChecks(): List<HealthStatus> {
        return emptyList()
    }
}
