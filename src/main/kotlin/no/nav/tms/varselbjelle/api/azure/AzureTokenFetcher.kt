package no.nav.tms.varselbjelle.api.azure

import no.nav.tms.token.support.azure.exchange.AzureService

class AzureTokenFetcher(
    private val tokendingsService: AzureService,
    private val eventhandlerClientId: String,
    private val eventaggregatorClientId: String
) {
    suspend fun fetchEventhandlerToken(): AccessToken {
        return AccessToken(tokendingsService.getAccessToken(eventhandlerClientId))
    }

    suspend fun fetchEventAggregatorToken(): AccessToken =
        AccessToken(tokendingsService.getAccessToken(eventaggregatorClientId))

}
