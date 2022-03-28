package personopplysninger.pdl.streams

import no.nav.aap.kafka.streams.consume
import org.apache.kafka.streams.StreamsBuilder
import personopplysninger.Topics

internal class GeografiskTilknytningStream(kStream: StreamsBuilder) {
    init {
        kStream
            .consume(Topics.geografiskTilknytning) { "consume-gt" }
    }
}
