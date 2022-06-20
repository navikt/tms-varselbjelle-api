package no.nav.tms.varselbjelle.api

import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import no.nav.tms.varselbjelle.api.notifikasjon.NotifikasjonHttpClient

fun Route.varsel(notifikasjonHttpClient: NotifikasjonHttpClient) {

    get("rest/varsel/hentsiste") {
        call.respond(HttpStatusCode.OK, notifikasjonHttpClient.getNotifikasjoner(AccessToken("tull")).somVarselbjellevarsel())
    }
}



