package personopplysninger.norg

import kotlinx.coroutines.runBlocking
import no.nav.aap.kafka.streams.extension.produce
import org.apache.kafka.streams.kstream.KStream
import personopplysninger.Personopplysninger
import personopplysninger.Topics

internal fun norgStream(norgClient: NorgClient) = { chain: KStream<String, Personopplysninger> ->
    chain
        .mapValues { personopplysninger -> settEnhet(personopplysninger, norgClient) }
        .mapValues(Personopplysninger::toDto)
        .produce(Topics.personopplysninger, "produce-personopplysning-enhet")
}

private fun settEnhet(person: Personopplysninger, norgClient: NorgClient): Personopplysninger = person.apply {
    val request = Arbeidsfordeling.createRequest(person.toDto())
    val response = runBlocking { norgClient.hentArbeidsfordeling(request) }.singleOrNull()
    settEnhet(response?.enhetNr ?: "UKJENT")
}
