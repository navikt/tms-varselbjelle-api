package no.nav.tms.varselbjelle.api

import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import no.nav.tms.varselbjelle.api.notifikasjon.NotifikasjonConsumer

fun Route.varsel(notifikasjonConsumer: NotifikasjonConsumer, varselsideUrl: String) {

    get("rest/varsel/hentsiste") {
        executeOnUnexpiredTokensOnly {
            val notifikasjoner = notifikasjonConsumer.getNotifikasjoner(authenticatedUser)

            call.respond(HttpStatusCode.OK, SammendragsVarsel(notifikasjoner, varselsideUrl))
        }
    }
}



