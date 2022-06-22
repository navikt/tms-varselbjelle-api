@file:UseSerializers(ZonedDateTimeSerializer::class)

package no.nav.tms.varselbjelle.api

import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import no.nav.tms.varselbjelle.api.config.ZonedDateTimeSerializer
import no.nav.tms.varselbjelle.api.notifikasjon.Notifikasjon
import java.time.ZonedDateTime

@Serializable
data class SammendragsVarsel(private val notifikasjoner: List<Notifikasjon>, private val varselsideUrl: String) {
    val nyesteVarsler: List<Varsel>
    val totaltAntallUleste: Int

    init {
        val varseltekst =
            if (notifikasjoner.size == 1) "Du har 1 varsel"
            else "Du har ${notifikasjoner.size} varsler"

        if (notifikasjoner.isEmpty()) {
            nyesteVarsler = emptyList()
            totaltAntallUleste = 0
        } else {
            nyesteVarsler = listOf(
                Varsel(
                    aktoerID = "placeholder",
                    url = varselsideUrl,
                    varseltekst = varseltekst,
                    varselId = "ubruktId",
                    id = 0L,
                    meldingsType = "default",
                    datoOpprettet = notifikasjoner.minOf { it.forstBehandlet },
                    datoLest = notifikasjoner.minOf { it.forstBehandlet })
            )
            totaltAntallUleste = 1
        }
    }

}

@Serializable
data class Varsel(
    val aktoerID: String,
    val url: String,
    val varseltekst: String,
    val varselId: String,
    val id: Long,
    val meldingsType: String,
    val datoOpprettet: ZonedDateTime,
    val datoLest: ZonedDateTime
)