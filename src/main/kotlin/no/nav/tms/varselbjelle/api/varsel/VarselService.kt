package no.nav.tms.varselbjelle.api.varsel

import io.ktor.client.HttpClient
import io.ktor.client.request.header
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mu.KLogger
import mu.KotlinLogging
import no.nav.tms.varselbjelle.api.config.getForIdent
import no.nav.tms.varselbjelle.api.azure.AzureTokenFetcher
import java.lang.Exception
import java.net.URL

class VarselService(
    private val client: HttpClient,
    private val azureTokenFetcher: AzureTokenFetcher,
    eventHandlerBaseURL: String,
    eventAggregatorBaseUrl: String
) {
    val log = KotlinLogging.logger {}

    private val varselEndpoint = URL("$eventHandlerBaseURL/fetch/varsel/on-behalf-of/aktive")
    private val doneEndpoint = URL("$eventAggregatorBaseUrl/on-behalf-of/beskjed/done")

    suspend fun getVarsler(ident: String): List<Varsel> {
        val accessToken = azureTokenFetcher.fetchEventhandlerToken()
        return client.getForIdent(varselEndpoint, ident, accessToken)
    }

    suspend fun postBeskjedDone(eventId: String, fnr: String) {
        val accessToken = azureTokenFetcher.fetchEventAggregatorToken()
        log.info("Sender done til eventaggreagtor for eventid $eventId")
        withContext(Dispatchers.IO) {
            client.request {
                url(doneEndpoint)
                method = HttpMethod.Post
                header("fodselsnummer", fnr)
                header(HttpHeaders.Authorization, "Bearer $accessToken")
                header(HttpHeaders.ContentType, ContentType.Application.Json)
                setBody("""{"eventId": "$eventId"}""")
            }
        }.apply {
            log.info("Mottok respons fra event-aggragtaor: $status for eventid $eventId")
            if (status != HttpStatusCode.OK)
                throw DoneFailedException(eventId, status)
        }
    }

    suspend fun getToken() = azureTokenFetcher.fetchEventAggregatorToken()
}

class DoneFailedException(val eventId: String, val statusCode: HttpStatusCode) : Exception() {
    fun resolveHttpResponse() = when (statusCode) {
        HttpStatusCode.NotFound -> HttpStatusCode.NotFound
        else -> HttpStatusCode.InternalServerError
    }
}