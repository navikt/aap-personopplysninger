package nom.skjerming

import no.nav.aap.kafka.serde.JsonSerde
import no.nav.aap.kafka.streams.Topic
import no.nav.aap.kafka.streams.consume
import org.apache.kafka.streams.StreamsBuilder

object Topics {
    val skjerming = Topic("nom.skjermede-personer-v1", JsonSerde.jackson<SkjermetDto>())
}

fun topology(stream: StreamsBuilder) = stream
    .consume(Topics.skjerming) { "skjerming-consumed" }