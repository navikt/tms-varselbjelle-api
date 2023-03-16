package no.nav.tms.varselbjelle.api.varsel

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import no.nav.tms.varselbjelle.api.azure.AzureTokenFetcher
import java.net.URL

class VarselService(
    private val client: HttpClient,
    private val azureTokenFetcher: AzureTokenFetcher,
    eventHandlerBaseURL: String,
    eventAggregatorBaseUrl: String
) {
    val secureLog = KotlinLogging.logger("secureLog")
    val log = KotlinLogging.logger{}


    private val varselEndpoint = URL("$eventHandlerBaseURL/fetch/varsel/on-behalf-of/aktive")
    private val doneEndpoint = URL("$eventAggregatorBaseUrl/on-behalf-of/beskjed/done")

    suspend fun getVarsler(ident: String): List<Varsel> {
        val accessToken = azureTokenFetcher.fetchEventhandlerToken()
        return client.getForIdent(varselEndpoint, ident, accessToken)
    }

    suspend fun postBeskjedDone(eventId: String, fnr: String) {
        val accessToken = azureTokenFetcher.fetchEventAggregatorToken()
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
            if (!status.isSuccess())
                throw DoneFailedException(eventId, status)
        }
    }

    suspend inline fun <reified T> HttpClient.getForIdent(url: URL, fnr: String, accessToken: String): T =
        withContext(Dispatchers.IO) {
            request {
                url(url)
                method = HttpMethod.Get
                header("fodselsnummer", fnr)
                header(HttpHeaders.Authorization, "Bearer $accessToken")
            }
        }.apply {
            if(!status.isSuccess()){
                log.warn { "Request til $url feilet med status $status" }
                secureLog.warn { "Request til $url feilet med status $status for ident $fnr" }
            }
        }.body()
}

class DoneFailedException(val eventId: String, val statusCode: HttpStatusCode) : Exception() {
    fun resolveHttpResponse() = when (statusCode) {
        HttpStatusCode.NotFound -> HttpStatusCode.NotFound
        else -> HttpStatusCode.InternalServerError
    }
}

