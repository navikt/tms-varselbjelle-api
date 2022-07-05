package no.nav.tms.varselbjelle.api.tokenx

import no.nav.tms.token.support.tokendings.exchange.TokendingsService

class EventhandlerTokendings(
    private val tokendingsService: TokendingsService,
    private val eventhandlerClientId: String
) {
    suspend fun exchangeToken(token: String): AccessToken {
        return AccessToken(tokendingsService.exchangeToken(token, eventhandlerClientId))
    }
}
