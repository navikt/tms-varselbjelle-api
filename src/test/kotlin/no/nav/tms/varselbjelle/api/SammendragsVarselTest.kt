package no.nav.tms.varselbjelle.api

import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import no.nav.tms.varselbjelle.api.notifikasjon.Notifikasjon
import org.junit.jupiter.api.Test
import java.time.ZoneId
import java.time.ZonedDateTime

class SammendragsVarselTest {

    @Test
    fun `returnerer tom liste ved ingen aktive notifikasjoner`() {
        val sammendragsVarsel = SammendragsVarsel(emptyList(), "url")

        sammendragsVarsel.totaltAntallUleste shouldBe 0
        sammendragsVarsel.nyesteVarsler shouldHaveSize 0
    }

    @Test
    fun `konverterer enkel beskjed til varselbjelle-varsel`() {
        val forstbehandlet = ZonedDateTime.of(2019, 1, 1, 1, 1, 1, 1, ZoneId.of("Europe/Oslo"))
        val varselsideUrl = "www.nav.no/person/dittnav/varslinger"
        val sammendragsVarsel = SammendragsVarsel(
            notifikasjoner = listOf(Notifikasjon(forstBehandlet = forstbehandlet)),
            varselsideUrl = varselsideUrl
        )

        sammendragsVarsel.totaltAntallUleste shouldBe 1
        sammendragsVarsel.nyesteVarsler shouldHaveSize 1

        val varsel = sammendragsVarsel.nyesteVarsler.first()
        varsel.varseltekst shouldBe "Du har 1 varsel"
        varsel.varselId shouldBe "ubruktId"
        varsel.url shouldBe varselsideUrl
        varsel.meldingsType shouldBe "default"
        varsel.datoOpprettet shouldBe forstbehandlet
    }

    @Test
    fun `konverterer liste av notifikasjoner til varselbjelle-varsel med tidligste dato`() {
        val tidligsteNotifikasjonTidspunkt = ZonedDateTime.of(2019, 1, 1, 1, 1, 1, 1, ZoneId.of("Europe/Oslo"))
        val sammendragsVarsel = SammendragsVarsel(
            listOf(
                Notifikasjon(
                    forstBehandlet = ZonedDateTime.of(2022, 2, 2, 1, 1, 1, 1, ZoneId.of("Europe/Oslo"))
                ),
                Notifikasjon(
                    forstBehandlet = tidligsteNotifikasjonTidspunkt
                ),
                Notifikasjon(
                    forstBehandlet = ZonedDateTime.of(2022, 3, 3, 1, 1, 1, 1, ZoneId.of("Europe/Oslo"))
                )
            ),
            "url"
        )

        sammendragsVarsel.totaltAntallUleste shouldBe 1
        sammendragsVarsel.nyesteVarsler shouldHaveSize 1

        val varsel = sammendragsVarsel.nyesteVarsler.first()
        varsel.varseltekst shouldBe "Du har 3 varsler"
        varsel.datoOpprettet shouldBe tidligsteNotifikasjonTidspunkt
    }
}