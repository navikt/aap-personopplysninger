package personopplysninger.skjerming

import model.Personopplysninger
import no.nav.aap.kafka.streams.leftJoin
import no.nav.aap.kafka.streams.produce
import org.apache.kafka.streams.kstream.GlobalKTable
import org.apache.kafka.streams.kstream.KStream
import personopplysninger.Topics

fun skjermingStream(skjermingTable: GlobalKTable<String, SkjermetDto>) = { chain: KStream<String, Personopplysninger> ->
    chain
        .leftJoin(::keyMapper, skjermingTable, ::valueJoiner)
        .mapValues { (personopplysninger, skjerming) ->
            personopplysninger.apply {
                settSkjerming(
                    skjerming?.skjermetFra?.toLocalDate(),
                    skjerming?.skjermetTil?.toLocalDate(),
                )
            }
        }
        .mapValues(Personopplysninger::toDto)
        .produce(Topics.personopplysninger) { "produced-personopplysning-skjermet" }
}


private fun keyMapper(personident: String, person: Personopplysninger): String = personident
private fun valueJoiner(person: Personopplysninger, skjerming: SkjermetDto?) = person to skjerming
