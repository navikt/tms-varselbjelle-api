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
        val sammendragsVarselDto = SammendragsVarsel(emptyList(), "url").toDto()

        sammendragsVarselDto.totaltAntallUleste shouldBe 0
        sammendragsVarselDto.nyesteVarsler shouldHaveSize 0
    }

    @Test
    fun `konverterer notifikasjon til varselbjelle-varsel med de nødvendige feltene satt`() {
        val forstbehandlet = ZonedDateTime.of(2019, 1, 1, 1, 1, 1, 1, ZoneId.of("Europe/Oslo"))
        val varselsideUrl = "www.nav.no/person/dittnav/varslinger"
        val sammendragsVarselDto = SammendragsVarsel(
            notifikasjoner = listOf(Notifikasjon(forstBehandlet = forstbehandlet)),
            varselsideUrl = varselsideUrl
        ).toDto()

        sammendragsVarselDto.totaltAntallUleste shouldBe 1
        sammendragsVarselDto.nyesteVarsler shouldHaveSize 1

        val varsel = sammendragsVarselDto.nyesteVarsler.first()
        varsel.varseltekst shouldBe "Du har 1 varsel"
        varsel.varselId shouldBe "ubruktId"
        varsel.url shouldBe varselsideUrl
        varsel.meldingsType shouldBe "MELDING"
        varsel.datoOpprettet shouldBe forstbehandlet.toInstant().toEpochMilli().toString()
    }

    @Test
    fun `konverterer liste av notifikasjoner til varselbjelle-varsel med tidligste dato`() {
        val tidligsteNotifikasjonstidspunkt = ZonedDateTime.of(2019, 1, 1, 1, 1, 1, 1, ZoneId.of("Europe/Oslo"))
        val sammendragsVarselDto = SammendragsVarsel(
            listOf(
                Notifikasjon(
                    forstBehandlet = ZonedDateTime.of(2022, 2, 2, 1, 1, 1, 1, ZoneId.of("Europe/Oslo"))
                ),
                Notifikasjon(
                    forstBehandlet = tidligsteNotifikasjonstidspunkt
                ),
                Notifikasjon(
                    forstBehandlet = ZonedDateTime.of(2022, 3, 3, 1, 1, 1, 1, ZoneId.of("Europe/Oslo"))
                )
            ),
            "url"
        ).toDto()

        sammendragsVarselDto.totaltAntallUleste shouldBe 1
        sammendragsVarselDto.nyesteVarsler shouldHaveSize 1

        val varsel = sammendragsVarselDto.nyesteVarsler.first()
        varsel.varseltekst shouldBe "Du har 3 varsler"
        varsel.datoOpprettet shouldBe tidligsteNotifikasjonstidspunkt.toInstant().toEpochMilli().toString()
    }
}