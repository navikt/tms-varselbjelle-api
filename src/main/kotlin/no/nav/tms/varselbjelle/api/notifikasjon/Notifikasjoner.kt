package no.nav.tms.varselbjelle.api.notifikasjon

import no.nav.tms.varselbjelle.api.NyeVarsler

class Notifikasjoner(private val notifikasjoner: List<Notifikasjon>) {
    fun somVarselbjellevarsel(): NyeVarsler {

        return NyeVarsler(emptyList(), 1)
    }
}