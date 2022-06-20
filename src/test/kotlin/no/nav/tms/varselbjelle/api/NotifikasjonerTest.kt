package no.nav.tms.varselbjelle.api

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import no.nav.tms.varselbjelle.api.notifikasjon.EventType
import no.nav.tms.varselbjelle.api.notifikasjon.Notifikasjon
import no.nav.tms.varselbjelle.api.notifikasjon.NotifikasjonHttpClient
import no.nav.tms.varselbjelle.api.notifikasjon.Notifikasjoner
import org.junit.jupiter.api.Test
import java.time.ZoneId
import java.time.ZonedDateTime

class NotifikasjonerTest {

    @Test
    fun `konverterer enkel beskjed til varselbjelle-varsel`() {
        val notifikasjonHttpClient: NotifikasjonHttpClient = mockk()
        val dummyToken = AccessToken("dummytoken")

        coEvery {
            notifikasjonHttpClient.getNotifikasjoner(dummyToken)
        } returns listOf(
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

        val notifikasjoner = Notifikasjoner(notifikasjonHttpClient)

        val varselbjelleVarsel = runBlocking { notifikasjoner.somVarselbjellevarsel(dummyToken) }

        varselbjelleVarsel.totaltAntallUleste shouldBe 1
    }
}