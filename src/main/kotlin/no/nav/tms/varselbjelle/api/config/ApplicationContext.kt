package no.nav.tms.varselbjelle.api.config

import no.nav.tms.varselbjelle.api.health.HealthService

class ApplicationContext {

    val httpClient = HttpClientBuilder.build()
    val healthService = HealthService(this)

}
