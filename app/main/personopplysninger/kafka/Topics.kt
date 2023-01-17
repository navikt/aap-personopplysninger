package personopplysninger.kafka

import no.nav.aap.kafka.serde.json.JsonSerde
import no.nav.aap.kafka.streams.Topic
import personopplysninger.domain.PersonopplysningerDto
import personopplysninger.streams.GtMedIdenter
import personopplysninger.streams.SkjermetDto
import personopplysninger.streams.SøknadDto

object Topics {
    //    val leesah = Topic("aapen-person-pdl-leesah-v1", AvroSerde.generic())
    val skjerming = Topic("nom.skjermede-personer-v1", JsonSerde.jackson<SkjermetDto>())
    val personopplysninger = Topic("aap.personopplysninger.v1", JsonSerde.jackson<PersonopplysningerDto>())
    val søknad = Topic("aap.soknad-sendt.v1", JsonSerde.jackson<SøknadDto>())
    val geografiskTilknytning = Topic("aapen-pdl-geografisktilknytning-v1", JsonSerde.jackson<GtMedIdenter>())
}
