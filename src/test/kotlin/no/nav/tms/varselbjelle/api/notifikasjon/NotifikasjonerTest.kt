package no.nav.tms.varselbjelle.api.notifikasjon

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.time.ZoneId
import java.time.ZonedDateTime

class NotifikasjonerTest {

    @Test
    fun `konverterer enkel beskjed til varselbjelle-varsel`() {
        val notifikasjoner = Notifikasjoner(
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
            )
        )

        val varselbjelleVarsel = notifikasjoner.somVarselbjellevarsel()

        varselbjelleVarsel.totaltAntallUleste shouldBe 1
    }
}