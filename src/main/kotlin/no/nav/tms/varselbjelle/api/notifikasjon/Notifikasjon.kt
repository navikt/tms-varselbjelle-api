@file:UseSerializers(ZonedDateTimeSerializer::class)

package no.nav.tms.varselbjelle.api.notifikasjon

import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import no.nav.tms.varselbjelle.api.config.ZonedDateTimeSerializer
import java.time.ZonedDateTime

@Serializable
data class Notifikasjon(
    private val grupperingsId: String,
    private val eventId: String,
    private val eventTidspunkt: ZonedDateTime,
    private val produsent: String,
    private val sikkerhetsnivaa: Int,
    private val sistOppdatert: ZonedDateTime,
    private val tekst: String,
    private val link: String,
    private val aktiv: Boolean,
    private val type: EventType,
    val forstBehandlet: ZonedDateTime
)

@Serializable
enum class EventType(val eventType: String) {
    OPPGAVE("oppgave"),
    BESKJED("beskjed"),
    INNBOKS("innboks")
}