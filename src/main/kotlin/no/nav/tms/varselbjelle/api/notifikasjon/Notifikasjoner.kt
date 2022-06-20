package no.nav.tms.varselbjelle.api.notifikasjon

import no.nav.tms.varselbjelle.api.AccessToken
import no.nav.tms.varselbjelle.api.NyeVarsler

class Notifikasjoner(private val notifikasjonHttpclient: NotifikasjonHttpClient) {
    suspend fun somVarselbjellevarsel(accessToken: AccessToken): NyeVarsler {
        val notifikasjoner = notifikasjonHttpclient.getNotifikasjoner(accessToken)

        return NyeVarsler(emptyList(), 1)
    }
}