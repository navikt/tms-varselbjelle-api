package no.nav.tms.varselbjelle.api

import io.kotest.matchers.shouldBe
import no.nav.tms.varselbjelle.api.notifikasjon.EventType
import no.nav.tms.varselbjelle.api.notifikasjon.Notifikasjon
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.time.ZoneId
import java.time.ZonedDateTime

class SammendragsVarselTest {

    @Test
    @Disabled
    fun `returnerer tom liste ved ingen aktive notifikasjoner`() {

    }

    @Test
    fun `konverterer enkel beskjed til varselbjelle-varsel`() {
        val varselbjelleVarsel = SammendragsVarsel(
            listOf(
                Notifikasjon(
                    grupperingsId = "123",
                    eventId = "456",
                    eventTidspunkt = ZonedDateTime.of(2020, 1, 1, 1, 1, 1, 1, ZoneId.of("Europe/Oslo")),
                    forstBehandlet = ZonedDateTime.of(2020, 1, 1, 1, 1, 1, 1, ZoneId.of("Europe/Oslo")),
                    produsent = "produsent",
                    sikkerhetsnivaa = 4,
                    sistOppdatert = ZonedDateTime.of(2020, 1, 1, 1, 1, 1, 1, ZoneId.of("Europe/Oslo")),
                    tekst = "tekst",
                    link = "link",
                    aktiv = true,
                    type = EventType.BESKJED
                )
            ),
            "url"
        )

        varselbjelleVarsel.totaltAntallUleste shouldBe 1
    }

    @Test
    @Disabled
    fun `konverterer liste av notifikasjoner til varselbjelle-varsel`() {

    }
}