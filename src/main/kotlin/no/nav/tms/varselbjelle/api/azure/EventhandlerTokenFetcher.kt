package no.nav.tms.varselbjelle.api.azure

import no.nav.tms.token.support.azure.exchange.AzureService

class EventhandlerTokenFetcher(
    private val tokendingsService: AzureService,
    private val eventhandlerClientId: String
) {
    suspend fun fetchToken(): AccessToken {
        return AccessToken(tokendingsService.getAccessToken(eventhandlerClientId))
    }
}
