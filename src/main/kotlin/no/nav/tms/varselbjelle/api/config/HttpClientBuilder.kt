package no.nav.tms.varselbjelle.api.config

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.apache.Apache
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.HttpTimeout
import io.ktor.client.features.json.serializer.*

object HttpClientBuilder {

    fun build(httpClientEngine: HttpClientEngine = Apache.create()): HttpClient {
        return HttpClient(httpClientEngine) {
            install(JsonFeature) {
                serializer = KotlinxSerializer(jsonConfig())
            }
            install(HttpTimeout)
        }
    }

}
