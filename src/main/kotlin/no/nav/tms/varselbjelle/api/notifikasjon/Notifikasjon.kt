@file:UseSerializers(ZonedDateTimeSerializer::class)

package no.nav.tms.varselbjelle.api.notifikasjon

import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import no.nav.tms.varselbjelle.api.config.ZonedDateTimeSerializer
import java.time.ZonedDateTime

@Serializable
data class Notifikasjon(
    val forstBehandlet: ZonedDateTime
)