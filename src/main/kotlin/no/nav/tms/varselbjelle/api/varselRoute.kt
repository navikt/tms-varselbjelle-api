package no.nav.tms.varselbjelle.api

import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.post
import no.nav.tms.varselbjelle.api.notifikasjon.NotifikasjonConsumer

fun Route.varsel(notifikasjonConsumer: NotifikasjonConsumer, varselsideUrl: String) {

    get("rest/varsel/hentsiste") {
        val token = call.request.cookies["selvbetjening-idtoken"]!!
        val notifikasjoner = notifikasjonConsumer.getNotifikasjoner(token)

        val varselbjelleResponse = VarselbjelleResponse(SammendragsVarsel(notifikasjoner, varselsideUrl).toDto())
        call.respond(HttpStatusCode.OK, varselbjelleResponse)
    }

    post("/erlest/{id}") {
        //dummy-endepunkt for at varselbjella ikke skal feile
        call.respond(HttpStatusCode.OK)
    }
}



