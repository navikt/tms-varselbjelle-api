package no.nav.tms.varselbjelle.api.notifikasjon

import io.ktor.client.HttpClient
import no.nav.tms.varselbjelle.api.config.AccessToken
import no.nav.tms.varselbjelle.api.config.get
import java.net.URL

class NotifikasjonConsumer(
    private val client: HttpClient,
    private val eventHandlerBaseURL: String
) {
    suspend fun getNotifikasjoner(accessToken: AccessToken): List<Notifikasjon> {
        return client.get(URL("$eventHandlerBaseURL/fetch/event/inaktive"), accessToken)
    }
}

