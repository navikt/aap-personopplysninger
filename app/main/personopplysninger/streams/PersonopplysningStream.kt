package personopplysninger.streams

import kotlinx.coroutines.runBlocking
import no.nav.aap.kafka.streams.v2.KTable
import no.nav.aap.kafka.streams.v2.Topology
import no.nav.aap.kafka.streams.v2.stream.ConsumedStream
import org.slf4j.LoggerFactory
import personopplysninger.domain.Personopplysninger
import personopplysninger.domain.PersonopplysningerInternDto
import personopplysninger.graphql.PdlGraphQLClient
import personopplysninger.kafka.Topics
import personopplysninger.rest.ArbeidsfordelingDtoRequest
import personopplysninger.rest.NorgClient

private val secureLog = LoggerFactory.getLogger("secureLog")

internal fun Topology.personopplysningStream(
    skjermetKTable: KTable<SkjermetDto>,
    pdlClient: PdlGraphQLClient,
    norgClient: NorgClient,
) =
    consume(Topics.personopplysningerIntern)
        .branch({ dto -> dto.kanSetteSkjerming() }, { it.skjermingBranch(skjermetKTable) })
        .branch({ dto -> dto.kanSettePdlopplysninger() }, { it.pdlBranch(pdlClient) })
        .branch({ dto -> dto.kanSetteEnhet() }, { it.norgBranch(norgClient) })
        .default { it.ferdigBranch() }

internal fun ConsumedStream<PersonopplysningerInternDto>.skjermingBranch(skjermede: KTable<SkjermetDto>) = this
    .leftJoinWith(skjermede)
    .map { personopplysningerInternDto, skjermetDto ->
        val personopplysninger = Personopplysninger.restore(personopplysningerInternDto)
        personopplysninger.settSkjerming(skjermetDto?.fom(), skjermetDto?.tom())
        personopplysninger.toDto()
    }
    .produce(Topics.personopplysningerIntern)

internal fun ConsumedStream<PersonopplysningerInternDto>.pdlBranch(pdlClient: PdlGraphQLClient) = this
    .mapNotNull { personident, dto ->
        val personopplysninger = Personopplysninger.restore(dto)
        val response = runBlocking { pdlClient.hentAlt(personident) }
        if (response.errors != null) {
            secureLog.warn("Feil fra PDL, ignorerer behov, ${response.errors}")
            return@mapNotNull null
        }
        personopplysninger.settGeografiskTilknytning(response.geografiskTilknytning())
        personopplysninger.settAdressebeskyttelse(response.gradering())
        val navn = requireNotNull(response.data?.hentPerson?.navn?.last()) { "Fant ikke navn i PDL" }
        personopplysninger.settNavn(navn.fornavn, navn.mellomnavn, navn.etternavn)
        personopplysninger.toDto()
    }
    .produce(Topics.personopplysningerIntern)

internal fun ConsumedStream<PersonopplysningerInternDto>.norgBranch(norgClient: NorgClient) = this
    .mapNotNull { personident, dto ->
        val request = ArbeidsfordelingDtoRequest.create(dto)
        val response = runBlocking { norgClient.hentArbeidsfordeling(request) }.singleOrNull()
        if (response == null) {
            secureLog.warn("Tom enhetsliste fra norg på $personident (${dto})")
            return@mapNotNull null
        }
        val personopplysninger = Personopplysninger.restore(dto)
        personopplysninger.settEnhet(response.enhetNr)
        personopplysninger.toDto()
    }
    .produce(Topics.personopplysningerIntern)

internal fun ConsumedStream<PersonopplysningerInternDto>.ferdigBranch() = this
    .map { _, value -> value.mapTilPersonopplysningerDto() }
    .produce(Topics.personopplysninger)
