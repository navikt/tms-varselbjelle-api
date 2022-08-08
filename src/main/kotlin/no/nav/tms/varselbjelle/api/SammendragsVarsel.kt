package no.nav.tms.varselbjelle.api

import kotlinx.serialization.Serializable
import no.nav.tms.varselbjelle.api.notifikasjon.Notifikasjon

data class SammendragsVarsel(private val notifikasjoner: List<Notifikasjon>, private val varselsideUrl: String) {
    private val nyesteVarsler: List<Varsel>
    private val totaltAntallUleste: Int

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
                    meldingsType = "MELDING",
                    datoOpprettet = notifikasjoner.maxOf { it.forstBehandlet }.toInstant().toEpochMilli().toString(),
                    datoLest = notifikasjoner.maxOf { it.forstBehandlet }.toInstant().toEpochMilli().toString()
                )
            )
            totaltAntallUleste = 1
        }
    }

    fun toDto() = SammendragsVarselDto(nyesteVarsler, totaltAntallUleste)
}

@Serializable
data class VarselbjelleResponse(val varsler: SammendragsVarselDto)

@Serializable
data class SammendragsVarselDto(val nyesteVarsler: List<Varsel>, val totaltAntallUleste: Int)

@Serializable
data class Varsel(
    val aktoerID: String,
    val url: String,
    val varseltekst: String,
    val varselId: String,
    val id: Long,
    val meldingsType: String,
    val datoOpprettet: String,
    val datoLest: String
)