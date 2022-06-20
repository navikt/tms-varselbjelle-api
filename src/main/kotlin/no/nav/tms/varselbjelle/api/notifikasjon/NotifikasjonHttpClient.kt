package no.nav.tms.varselbjelle.api.notifikasjon

import io.ktor.client.HttpClient
import no.nav.tms.varselbjelle.api.AccessToken
import no.nav.tms.varselbjelle.api.config.get
import java.net.URL

class NotifikasjonHttpClient(
    private val client: HttpClient,
    private val eventHandlerBaseURL: String
) {
    suspend fun getNotifikasjoner(accessToken: AccessToken): Notifikasjoner {
        val notifikasjoner = client.get<List<Notifikasjon>>(URL("$eventHandlerBaseURL/fetch/event/inaktive"), accessToken)
        return Notifikasjoner(notifikasjoner)
    }
}

