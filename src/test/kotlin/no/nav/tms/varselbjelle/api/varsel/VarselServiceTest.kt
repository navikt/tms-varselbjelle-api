package no.nav.tms.varselbjelle.api.varsel

import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import no.nav.tms.varselbjelle.api.azure.AccessToken
import no.nav.tms.varselbjelle.api.azure.AzureTokenFetcher
import no.nav.tms.varselbjelle.api.config.jsonConfig
import org.junit.jupiter.api.Test

class VarselServiceTest {
    private val aggreagtorTestHost = "https://aggregator.test"
    private val doneEndpoint = "on-behalf-of/beskjed/done"
    private val testEventId = "12345678kj"
    private val testFnr = "123456678910"

    @Test
    fun `setter done på varsel`() =
        shouldNotThrow<Exception> {
            withDoneResponse(HttpStatusCode.OK) {
                varselService().postBeskjedDone(testEventId, testFnr)
            }
        }

    @Test
    fun `kaster exception ved feil fra event-aggregator`() {
        shouldThrow<DoneFailedException> {
            withDoneResponse(HttpStatusCode.NotFound) {
                varselService().postBeskjedDone(testEventId, testFnr)
            }
        }.statusCode shouldBe HttpStatusCode.NotFound

        shouldThrow<DoneFailedException> {
            withDoneResponse(HttpStatusCode.BadRequest) {
                varselService().postBeskjedDone(testEventId, testFnr)
            }
        }.statusCode shouldBe HttpStatusCode.BadRequest
    }


    private fun withDoneResponse(statusCode: HttpStatusCode, function: suspend ApplicationTestBuilder.() -> Unit) =
        testApplication {
            externalServices {
                hosts(aggreagtorTestHost) {
                    routing {
                        post(doneEndpoint) {
                            call.respond(statusCode)
                        }
                    }
                }
            }
            runBlocking { function() }
        }


    private fun ApplicationTestBuilder.varselService() = VarselService(
        client = createClient { jsonConfig() },
        azureTokenFetcher = mockk<AzureTokenFetcher>().also {
            coEvery { it.fetchEventAggregatorToken() } returns AccessToken("tadda")
        },
        eventHandlerBaseURL = "https://tadda.test",
        eventAggregatorBaseUrl = aggreagtorTestHost
    )

}

