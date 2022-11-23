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
                    if (varsel.sikkerhetsnivaa > authLevel) {
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
            link = null,
            type = type.name
        )

        private fun Varsel.toVarselbjelleVarsel() = VarselbjelleVarsel(
            eventId = eventId,
            tidspunkt = forstBehandlet,
            isMasked = false,
            tekst = tekst,
            link = link,
            type = type.name
        )
    }
}

@Serializable
data class VarselbjelleVarsler(
    val beskjeder: List<VarselbjelleVarsel>,
    val oppgaver: List<VarselbjelleVarsel>,
) {
    companion object {
        fun fromVarsler(varsler: List<Varsel>, authLevel: Int): VarselbjelleVarsler {
            val groupedVarsler = varsler.groupBy { it.type }.mapValues { (_, varsler) ->
                varsler.map { varsel ->
                    varsel.toVarselbjelleVarsel(authLevel)
                }
            }

            return VarselbjelleVarsler(
                beskjeder = (groupedVarsler[VarselType.BESKJED] ?: emptyList()) + (groupedVarsler[VarselType.INNBOKS]
                    ?: emptyList()),
                oppgaver = groupedVarsler[VarselType.OPPGAVE] ?: emptyList()
            )
        }

        private fun Varsel.toVarselbjelleVarsel(authLevel: Int) = VarselbjelleVarsel(
            eventId = eventId,
            tidspunkt = forstBehandlet,
            isMasked = sikkerhetsnivaa > authLevel,
            tekst = if (sikkerhetsnivaa > authLevel) null else tekst,
            link = if (sikkerhetsnivaa > authLevel) null else link,
            type = type.name
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
    val type: String
) {
}
