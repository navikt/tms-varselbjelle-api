package no.nav.tms.varselbjelle.api

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import no.nav.tms.token.support.tokenx.validation.user.TokenXUserFactory
import no.nav.tms.varselbjelle.api.notifikasjon.NotifikasjonConsumer

fun Route.varsel(notifikasjonConsumer: NotifikasjonConsumer, varselsideUrl: String) {

    get("rest/varsel/hentsiste") {
        val token = call.user.tokenString
        val notifikasjoner = notifikasjonConsumer.getNotifikasjoner(token)

        val varselbjelleResponse = VarselbjelleResponse(SammendragsVarsel(notifikasjoner, varselsideUrl).toDto())
        call.respond(HttpStatusCode.OK, varselbjelleResponse)
    }

    post("rest/varsel/erlest/{id}") {
        //dummy-endepunkt for at varselbjella ikke skal feile
        call.respond(HttpStatusCode.OK)
    }
}

val ApplicationCall.user get() = TokenXUserFactory.createTokenXUser(this)


