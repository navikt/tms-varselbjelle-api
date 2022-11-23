package no.nav.tms.varselbjelle.api

import io.kotest.matchers.shouldBe
import no.nav.tms.varselbjelle.api.varsel.VarselType
import org.junit.jupiter.api.Test

internal class VarselbjelleVarslerTest {
    private val testData =
        testVarsel(VarselType.BESKJED) * 2 + testVarsel(VarselType.INNBOKS) * 1 + testVarsel(VarselType.OPPGAVE) * 4 + testVarsel(
                VarselType.BESKJED,
                sikkerhetsnivaa = 3
            ) * 2


    @Test
    fun `grupperer varsel riktig`(){
        VarselbjelleVarsler.fromVarsler(testData,4).assert {
            beskjeder.size shouldBe 5
            oppgaver.size shouldBe 4
            beskjeder.any { it.isMasked } shouldBe false
            beskjeder.any { it.type == "INNBOKS" } shouldBe true
            beskjeder.any { it.type == "BESKJED" } shouldBe true
            beskjeder.any { it.type == "OPPGAVE" } shouldBe false

            oppgaver.any { it.isMasked } shouldBe false
            oppgaver.any { it.type == "INNBOKS" } shouldBe false
            oppgaver.any { it.type == "BESKJED" } shouldBe false
            oppgaver.any { it.type == "OPPGAVE" } shouldBe true
        }
    }

    @Test
    fun `maskerer data ved for lavt autentiseringsniåva`(){
        VarselbjelleVarsler.fromVarsler(testData,3).assert {
            beskjeder.size shouldBe 5
            oppgaver.size shouldBe 4
            beskjeder.filter { it.isMasked }.assert {
                size shouldBe 3
                first().tekst shouldBe null
                first().link shouldBe null
            }
            oppgaver.filter { it.isMasked }.size shouldBe 4
        }
    }
}