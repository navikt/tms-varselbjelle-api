package no.nav.tms.varselbjelle.api

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import no.nav.tms.varselbjelle.api.varsel.EventHandlerConsumer
import no.nav.tms.varselbjelle.api.user.UserCallValidator.doIfValidRequest

fun Route.varsel(notifikasjonConsumer: EventHandlerConsumer, varselsideUrl: String) {

    get("rest/varsel/hentsiste") {
        doIfValidRequest { user ->
            val notifikasjoner = notifikasjonConsumer.getVarsler(user.ident)

            val sammendragsVarsel = SammendragsVarselDto.fromVarsler(notifikasjoner, varselsideUrl)
            call.respond(HttpStatusCode.OK, VarselbjelleResponse(sammendragsVarsel))
        }
    }

    post("rest/varsel/erlest/{id}") {
        //dummy-endepunkt for at varselbjella ikke skal feile
        call.respond(HttpStatusCode.OK)
    }
}
