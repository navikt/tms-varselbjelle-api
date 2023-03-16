package no.nav.tms.varselbjelle.api

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import mu.KotlinLogging
import no.nav.tms.varselbjelle.api.user.UserCallValidator.doIfValidRequest
import no.nav.tms.varselbjelle.api.varsel.VarselService

fun Route.varsel(varselService: VarselService, varselsideUrl: String) {

    val log = KotlinLogging.logger {}

    get("/varsel") {
        doIfValidRequest { user ->
            val varsler = varselService.getVarsler(user.ident)
            val varslerByType = VarselbjelleVarsler.fromVarsler(varsler, user.authLevel.toInt())
            call.respond(HttpStatusCode.OK, varslerByType)
        }
    }

    get("/varsel/sammendrag") {
        doIfValidRequest { user ->
            try {
                val notifikasjoner = varselService.getVarsler(user.ident)

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
            val varsler = varselService.getVarsler(user.ident)
            val varslerByType = VarselbjelleVarslerByType.fromVarsler(varsler, user.authLevel.toInt())
            call.respond(HttpStatusCode.OK, varslerByType)
        }
    }

    post("/varsel/erlest/{id}") {
        //dummy-endepunkt for at varselbjella ikke skal feile
        call.respond(HttpStatusCode.OK)
    }

    post("/varsel/beskjed/done") {
        doIfValidRequest { user ->
            varselService.postBeskjedDone(eventId = call.eventId(), fnr = user.ident)
            call.respond(HttpStatusCode.OK)
        }
    }
}

private suspend fun ApplicationCall.eventId(): String = receive<String>().let {
    if (it.isEmpty()) {
        throw IllegalArgumentException("request mangler body innhold")
    }
    Json.parseToJsonElement(it).jsonObject["eventId"]?.jsonPrimitive?.content
        ?: throw IllegalArgumentException("eventId finnes ikke i body")
}
