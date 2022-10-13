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
        fun fromVarsler(varsler: List<Varsel>): VarselbjelleVarslerByType {
            val groupedVarsler = varsler.groupBy { it.type }.mapValues {
                it.value.map { varsel ->
                    if(varsel.sikkerhetsnivaa != 4) {
                        VarselbjelleVarsel(
                            tidspunkt = varsel.forstBehandlet,
                            isMasked = true,
                            tekst = null,
                            link = null
                        )
                    }
                    else VarselbjelleVarsel(
                        tidspunkt = varsel.forstBehandlet,
                        isMasked = false,
                        tekst = varsel.tekst,
                        link = varsel.link
                    )
                }
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
data class VarselbjelleVarsel(
    val tidspunkt: ZonedDateTime,
    val isMasked: Boolean,
    val tekst: String?,
    val link: String?,
)