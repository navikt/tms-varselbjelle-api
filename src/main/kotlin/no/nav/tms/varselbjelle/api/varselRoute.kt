package no.nav.tms.varselbjelle.api

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import mu.KotlinLogging
import no.nav.tms.varselbjelle.api.user.UserCallValidator.doIfValidRequest
import no.nav.tms.varselbjelle.api.varsel.EventHandlerConsumer

fun Route.varsel(notifikasjonConsumer: EventHandlerConsumer, varselsideUrl: String) {

    val log = KotlinLogging.logger {}

    get("/varsel/sammendrag") {
        doIfValidRequest { user ->
            try {
                val notifikasjoner = notifikasjonConsumer.getVarsler(user.ident)

                val sammendragsVarsel = SammendragsVarselDto.fromVarsler(notifikasjoner, varselsideUrl)
                call.respond(HttpStatusCode.OK, VarselbjelleResponse(sammendragsVarsel))
            } catch (e: Exception) {
                log.error("Uventet feil ved henting av varsel-sammendrag.", e)
                call.respond(HttpStatusCode.InternalServerError)
            }
        }
    }

    get("/varsel/aktive") {
        doIfValidRequest { user ->
            val varsler = notifikasjonConsumer.getVarsler(user.ident)
            call.respond(HttpStatusCode.OK, VarselbjelleVarslerByType.fromVarsler(varsler))
        }
    }

    post("/varsel/erlest/{id}") {
        //dummy-endepunkt for at varselbjella ikke skal feile
        call.respond(HttpStatusCode.OK)
    }
}
