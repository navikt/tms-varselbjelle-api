@file:UseSerializers(ZonedDateTimeSerializer::class)

package no.nav.tms.varselbjelle.api.varsel

import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import no.nav.tms.varselbjelle.api.VarselbjelleVarsel
import no.nav.tms.varselbjelle.api.config.ZonedDateTimeSerializer
import java.time.ZonedDateTime

@Serializable
data class Varsel(
    val eventId: String,
    val forstBehandlet: ZonedDateTime,
    val type: VarselType,
    val sikkerhetsnivaa: Int,
    val tekst: String,
    val link: String,
    val eksternVarslingSendt: Boolean,
    val eksternVarslingKanaler: List<String>
){

    fun toVarselbjelleVarsel(authLevel: Int) = VarselbjelleVarsel(
    eventId = eventId,
    tidspunkt = forstBehandlet,
    isMasked = sikkerhetsnivaa > authLevel,
    tekst = if (sikkerhetsnivaa > authLevel) null else tekst,
    link = if (sikkerhetsnivaa > authLevel) null else link,
    type = type.name
    )
}

enum class VarselType {
    OPPGAVE,
    BESKJED,
    INNBOKS,
}
