package personopplysninger.norg

import kotlinx.coroutines.runBlocking
import personopplysninger.Personopplysninger
import no.nav.aap.kafka.streams.produce
import org.apache.kafka.streams.kstream.KStream
import personopplysninger.Topics

internal fun norgStream(norgClient: NorgProxyClient) = { chain: KStream<String, Personopplysninger> ->
    chain
        .mapValues { personopplysninger -> settEnhet(personopplysninger, norgClient) }
        .mapValues(Personopplysninger::toDto)
        .produce(Topics.personopplysninger) { "produce-personopplysning-enhet" }
}

private fun settEnhet(person: Personopplysninger, norgClient: NorgProxyClient): Personopplysninger = person.apply {
    val request = Arbeidsfordeling.createRequest(person.toDto())
    val response = runBlocking { norgClient.hentArbeidsfordeling(request) }
    settEnhet(response.enhetNr)
}
