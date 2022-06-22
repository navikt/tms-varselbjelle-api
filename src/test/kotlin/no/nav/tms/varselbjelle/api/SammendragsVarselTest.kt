package no.nav.tms.varselbjelle.api

import io.kotest.matchers.collections.shouldHaveSize
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
        val sammendragsVarsel = SammendragsVarsel(emptyList(), "url")

        sammendragsVarsel.totaltAntallUleste shouldBe 0
        sammendragsVarsel.nyesteVarsler shouldHaveSize 0
    }

    @Test
    fun `konverterer enkel beskjed til varselbjelle-varsel`() {
        val forstbehandlet = ZonedDateTime.of(2019, 1, 1, 1, 1, 1, 1, ZoneId.of("Europe/Oslo"))
        val sammendragsVarsel = SammendragsVarsel(
            listOf(
                Notifikasjon(
                    grupperingsId = "123",
                    eventId = "456",
                    eventTidspunkt = ZonedDateTime.of(2020, 1, 1, 1, 1, 1, 1, ZoneId.of("Europe/Oslo")),
                    forstBehandlet = forstbehandlet,
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

        sammendragsVarsel.totaltAntallUleste shouldBe 1
        sammendragsVarsel.nyesteVarsler shouldHaveSize 1

        val varsel = sammendragsVarsel.nyesteVarsler.first()
        varsel.varseltekst shouldBe "Du har 1 varsel"
        varsel.datoOpprettet shouldBe forstbehandlet
    }

    @Test
    @Disabled
    fun `konverterer liste av notifikasjoner til varselbjelle-varsel`() {

    }
}