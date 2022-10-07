package no.nav.tms.varselbjelle.api.user

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.util.pipeline.*
import mu.KotlinLogging

object UserCallValidator {

    val log = KotlinLogging.logger {}

    suspend inline fun PipelineContext<Unit, ApplicationCall>.doIfValidRequest(block: (fnr: User) -> Unit) {
        val identHeader = "fodselsnummer"
        val authLevelHeader = "auth_level"

        val ident = call.request.headers[identHeader]
        val authLevel = call.request.headers[authLevelHeader]

        if (ident != null && authLevel != null) {

            if (!isIdentOfValidLength(ident)) {
                val msg = "Header '$identHeader' inneholder ikke et gyldig fødselsnummer."
                log.warn(msg)
                call.respond(HttpStatusCode.BadRequest, msg)
            } else if (!isAuthLevelValid(authLevel)) {
                val msg = "Header '$identHeader' inneholder ikke et gyldig innloggingsnivå."
                log.warn(msg)
                call.respond(HttpStatusCode.BadRequest, msg)
            } else {
                val user = User(ident, authLevel)
                block.invoke(user)
            }
        } else {
            val msg = "Request mangler header '$identHeader' eller '$authLevelHeader'"
            log.warn(msg)
            call.respond(HttpStatusCode.BadRequest, msg)
        }
    }

    fun isIdentOfValidLength(ident: String) = ident.isNotEmpty() && ident.length == 11

    fun isAuthLevelValid(authLevel: String): Boolean {
        return authLevel.toIntOrNull() != null
    }
}
