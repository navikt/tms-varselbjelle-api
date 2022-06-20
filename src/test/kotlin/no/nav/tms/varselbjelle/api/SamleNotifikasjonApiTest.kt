package no.nav.tms.varselbjelle.api

import com.fasterxml.jackson.databind.ObjectMapper
import io.kotest.matchers.shouldBe
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.withTestApplication
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

class SamleNotifikasjonApiTest {

    @Test
    @Disabled
    fun `returnerer samle-notifikasjon på varselbjelle-formmat`() {

        val response = withTestApplication(mockVarselbjelleApi())  {
            handleRequest(HttpMethod.Get, "rest/varsel/hentsiste") {}
        }.response

        response.status() shouldBe HttpStatusCode.OK

        val varselJson = ObjectMapper().readTree(response.content)

        varselJson["nyesteVarsler"].size() shouldBe 1
        varselJson["totaltAntallUleste"].asInt() shouldBe 1
    }

    @Test
    @Disabled
    fun `ende til ende`() {

    }

}
