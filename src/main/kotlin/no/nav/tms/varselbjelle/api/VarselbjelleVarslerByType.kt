@file:UseSerializers(ZonedDateTimeSerializer::class)
package no.nav.tms.varselbjelle.api

import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import no.nav.tms.varselbjelle.api.config.ZonedDateTimeSerializer
import no.nav.tms.varselbjelle.api.varsel.Varsel
import no.nav.tms.varselbjelle.api.varsel.VarselType
import java.time.ZonedDateTime

@Serializable
data class VarselbjelleVarslerByType(
    val beskjeder: List<VarselbjelleVarsel>,
    val oppgaver: List<VarselbjelleVarsel>,
    val innbokser: List<VarselbjelleVarsel>
    ) {
    companion object {
        fun fromVarsler(varsler: List<Varsel>, authLevel: Int): VarselbjelleVarslerByType {
            val groupedVarsler = varsler.groupBy { it.type }.mapValues { (_, varsler) ->
                varsler.map { varsel ->
                    if(varsel.sikkerhetsnivaa > authLevel) {
                        varsel.toMaskedVarselbjelleVarsel()
                    } else {
                        varsel.toVarselbjelleVarsel()
                    }
                }
            }

            return VarselbjelleVarslerByType(
                beskjeder = groupedVarsler[VarselType.BESKJED] ?: emptyList(),
                oppgaver = groupedVarsler[VarselType.OPPGAVE] ?: emptyList(),
                innbokser = groupedVarsler[VarselType.INNBOKS] ?: emptyList(),
            )
        }

        private fun Varsel.toMaskedVarselbjelleVarsel() = VarselbjelleVarsel(
            eventId = eventId,
            tidspunkt = forstBehandlet,
            isMasked = true,
            tekst = null,
            link = null
        )

        private fun Varsel.toVarselbjelleVarsel() = VarselbjelleVarsel(
            eventId = eventId,
            tidspunkt = forstBehandlet,
            isMasked = false,
            tekst = tekst,
            link = link
        )
    }
}

@Serializable
data class VarselbjelleVarsel(
    val eventId: String,
    val tidspunkt: ZonedDateTime,
    val isMasked: Boolean,
    val tekst: String?,
    val link: String?,
)
