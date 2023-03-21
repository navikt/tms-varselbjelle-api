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
            val groupedVarsler = varsler.groupBy { it.type }.mapValues { (_, groupedVarsler) ->
                groupedVarsler.map { it.toVarselbjelleVarsel(authLevel) }
            }
            return VarselbjelleVarslerByType(
                beskjeder = groupedVarsler[VarselType.BESKJED] ?: emptyList(),
                oppgaver = groupedVarsler[VarselType.OPPGAVE] ?: emptyList(),
                innbokser = groupedVarsler[VarselType.INNBOKS] ?: emptyList(),
            )
        }
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
    }
}

@Serializable
data class VarselbjelleVarsel(
    val eventId: String,
    val tidspunkt: ZonedDateTime,
    val isMasked: Boolean,
    val tekst: String?,
    val link: String?,
    val type: String,
    val eksternVarslingSendt: Boolean,
    val eksternVarslingKanaler: List<String>
)
