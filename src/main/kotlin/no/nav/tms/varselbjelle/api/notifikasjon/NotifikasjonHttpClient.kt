package no.nav.tms.varselbjelle.api.notifikasjon

import io.ktor.client.HttpClient
import no.nav.tms.varselbjelle.api.AccessToken
import no.nav.tms.varselbjelle.api.config.get
import java.net.URL

class NotifikasjonHttpClient(
    private val client: HttpClient,
    private val eventHandlerBaseURL: URL
) {
    suspend fun getNotifikasjoner(accessToken: AccessToken): Notifikasjoner {
        return client.get(URL("$eventHandlerBaseURL/fetch/event/inaktive"), accessToken)
    }
}

