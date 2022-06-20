package no.nav.tms.varselbjelle.api.notifikasjon

import java.time.ZonedDateTime

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
    private val forstBehandlet: ZonedDateTime
)

enum class EventType(val eventType: String) {
    OPPGAVE("oppgave"),
    BESKJED("beskjed"),
    INNBOKS("innboks")
}