package no.nav.tms.varselbjelle.api.varsel

import io.ktor.client.HttpClient
import no.nav.tms.varselbjelle.api.config.getForIdent
import no.nav.tms.varselbjelle.api.azure.EventhandlerTokenFetcher
import java.net.URL

class EventHandlerConsumer(
    private val client: HttpClient,
    private val eventhandlerTokendings: EventhandlerTokenFetcher,
    eventHandlerBaseURL: String
) {
    private val varselEndpoint = URL("$eventHandlerBaseURL/fetch/varsel/on-behalf-of/aktive")

    suspend fun getVarsler(ident: String): List<Varsel> {
        val accessToken = eventhandlerTokendings.fetchToken()

        return client.getForIdent(varselEndpoint, ident, accessToken)
    }
}

