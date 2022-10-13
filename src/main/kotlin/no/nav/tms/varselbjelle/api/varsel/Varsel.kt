@file:UseSerializers(ZonedDateTimeSerializer::class)

package no.nav.tms.varselbjelle.api.varsel

import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
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
)

enum class VarselType {
    OPPGAVE,
    BESKJED,
    INNBOKS,
}
