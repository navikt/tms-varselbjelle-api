package no.nav.tms.varselbjelle.api

import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import java.time.ZonedDateTime

fun Route.varsel() {

    get("rest/varsel/hentsiste") {
        val dummyVarsel = NyeVarsler(
            listOf(
                Varsel(
                    "123",
                    "www.nav.no",
                    "",
                    "1234",
                    1L,
                    "",
                    ZonedDateTime.now(),
                    ZonedDateTime.now()
                )
            ),
            1
        )
        call.respond(HttpStatusCode.OK, dummyVarsel)
    }
}



