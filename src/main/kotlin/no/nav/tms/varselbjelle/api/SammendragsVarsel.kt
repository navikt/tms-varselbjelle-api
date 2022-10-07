package no.nav.tms.varselbjelle.api

import kotlinx.serialization.Serializable
import no.nav.tms.varselbjelle.api.varsel.Varsel

@Serializable
data class VarselbjelleResponse(val varsler: SammendragsVarselDto)

@Serializable
data class SammendragsVarselDto(val nyesteVarsler: List<VarselDto>, val totaltAntallUleste: Int) {

    companion object {
        fun fromVarsler(varsler: List<Varsel>, varselsideUrl: String): SammendragsVarselDto {

            if (varsler.isEmpty()) {
                return SammendragsVarselDto(emptyList(), 0)
            }

            val varseltekst = if (varsler.size == 1) {
                "Du har 1 varsel"
            } else {
                "Du har ${varsler.size} varsler"
            }

            val nyesteVarsler = listOf(
                VarselDto(
                    aktoerID = "placeholder",
                    url = varselsideUrl,
                    varseltekst = varseltekst,
                    varselId = "ubruktId",
                    id = 0L,
                    meldingsType = "MELDING",
                    datoOpprettet = varsler.maxOf { it.forstBehandlet }.toInstant().toEpochMilli().toString(),
                    datoLest = varsler.maxOf { it.forstBehandlet }.toInstant().toEpochMilli().toString()
                )
            )

            return SammendragsVarselDto(nyesteVarsler, 1)
        }
    }
}

@Serializable
data class VarselDto(
    val aktoerID: String,
    val url: String,
    val varseltekst: String,
    val varselId: String,
    val id: Long,
    val meldingsType: String,
    val datoOpprettet: String,
    val datoLest: String
)
