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
            if (status != HttpStatusCode.OK)
                throw DoneFailedException(eventId, status)
        }
    }
}

class DoneFailedException(val eventId: String, val statusCode: HttpStatusCode) : Exception() {
    fun resolveHttpResponse() = when (statusCode) {
        HttpStatusCode.NotFound -> HttpStatusCode.NotFound
        else -> HttpStatusCode.InternalServerError
    }
}