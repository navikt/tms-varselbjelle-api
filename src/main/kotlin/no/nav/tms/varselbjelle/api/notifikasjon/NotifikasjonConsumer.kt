package no.nav.tms.varselbjelle.api.notifikasjon

import io.ktor.client.HttpClient
import no.nav.tms.varselbjelle.api.config.get
import no.nav.tms.varselbjelle.api.tokenx.EventhandlerTokendings
import java.net.URL

class NotifikasjonConsumer(
    private val client: HttpClient,
    private val eventhandlerTokendings: EventhandlerTokendings,
    private val eventHandlerBaseURL: String
) {
    suspend fun getNotifikasjoner(token: String): List<Notifikasjon> {
        val exchangedToken = eventhandlerTokendings.exchangeToken(token)
        return client.get(URL("$eventHandlerBaseURL/fetch/beskjed/inaktive"), exchangedToken)
    }
}

