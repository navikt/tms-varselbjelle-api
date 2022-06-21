package no.nav.tms.varselbjelle.api

import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import no.nav.tms.varselbjelle.api.config.AccessToken
import no.nav.tms.varselbjelle.api.notifikasjon.NotifikasjonConsumer

fun Route.varsel(notifikasjonConsumer: NotifikasjonConsumer, varselsideUrl: String) {

    get("rest/varsel/hentsiste") {
        val notifikasjoner = notifikasjonConsumer.getNotifikasjoner(AccessToken("tull"))

        call.respond(HttpStatusCode.OK, SammendragsVarsel(notifikasjoner, varselsideUrl) )
    }
}



