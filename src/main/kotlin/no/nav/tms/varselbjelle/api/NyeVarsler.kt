@file:UseSerializers(ZonedDateTimeSerializer::class)

package no.nav.tms.varselbjelle.api

import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import java.time.ZonedDateTime

@Serializable
data class NyeVarsler(
    val nyesteVarsler: List<Varsel>,
    val totaltAntallUleste: Int
)

@Serializable
data class Varsel(
    val aktoerID: String,
    val url: String,
    val varseltekst: String,
    val varselId: String,
    val id: Long,
    val meldingsType: String,
    val datoOpprettet: ZonedDateTime,
    val datoLest: ZonedDateTime
)