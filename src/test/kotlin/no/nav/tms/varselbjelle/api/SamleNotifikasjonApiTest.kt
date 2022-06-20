package no.nav.tms.varselbjelle.api

import io.kotest.matchers.shouldBe
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.withTestApplication
import org.junit.jupiter.api.Test

class SamleNotifikasjonApiTest {

    @Test
    fun `skal returnere varselbjelle-varsel på riktig format`() {

        val response = withTestApplication(mockVarselbjelleApi())  {
            handleRequest(HttpMethod.Get, "rest/varsel/hentsiste") {}
        }.response

        response.status() shouldBe HttpStatusCode.OK

        //val eventJson = ObjectMapper().readTree(response.content)[0]
    }
}
