package no.nav.tms.varselbjelle.api

import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import no.nav.tms.varselbjelle.api.varsel.Varsel
import no.nav.tms.varselbjelle.api.varsel.VarselType
import org.junit.jupiter.api.Test
import java.time.ZoneOffset.UTC
import java.time.ZonedDateTime

class SammendragsVarselTest {

    @Test
    fun `returnerer tom liste ved ingen aktive notifikasjoner`() {
        val sammendragsVarselDto = SammendragsVarselDto.fromVarsler(emptyList(), "url")

        sammendragsVarselDto.totaltAntallUleste shouldBe 0
        sammendragsVarselDto.nyesteVarsler shouldHaveSize 0
    }

    @Test
    fun `konverterer notifikasjon til varselbjelle-varsel med de nødvendige feltene satt`() {
        val forstbehandlet = ZonedDateTime.now(UTC)
        val varselsideUrl = "www.nav.no/person/dittnav/varslinger"
        val sammendragsVarselDto = SammendragsVarselDto.fromVarsler(
            varsler = listOf(testVarsel(forstBehandlet = forstbehandlet)),
            varselsideUrl = varselsideUrl
        )

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
    fun `konverterer liste av notifikasjoner til varselbjelle-varsel med seneste dato`() {
        val senesteNotifikasjonstidspunkt = ZonedDateTime.now(UTC)
        val sammendragsVarselDto = SammendragsVarselDto.fromVarsler(
            listOf(
                testVarsel(
                    forstBehandlet = senesteNotifikasjonstidspunkt.minusMonths(1)
                ),
                testVarsel(
                    forstBehandlet = senesteNotifikasjonstidspunkt
                ),
                testVarsel(
                    forstBehandlet = senesteNotifikasjonstidspunkt.minusMonths(6)
                )
            ),
            "url"
        )

        sammendragsVarselDto.totaltAntallUleste shouldBe 1
        sammendragsVarselDto.nyesteVarsler shouldHaveSize 1

        val varsel = sammendragsVarselDto.nyesteVarsler.first()
        varsel.varseltekst shouldBe "Du har 3 varsler"
        varsel.datoOpprettet shouldBe senesteNotifikasjonstidspunkt.toInstant().toEpochMilli().toString()
    }

    private fun testVarsel(forstBehandlet: ZonedDateTime = ZonedDateTime.now(UTC), eksternVarslingKanaler: List<String> = emptyList()): Varsel =
        Varsel(
            eventId = "123",
            forstBehandlet = forstBehandlet,
            type = VarselType.BESKJED,
            sikkerhetsnivaa = 4,
            tekst = "",
            link = "",
            eksternVarslingSendt = eksternVarslingKanaler.isNotEmpty(),
            eksternVarslingKanaler = eksternVarslingKanaler
        )
}
