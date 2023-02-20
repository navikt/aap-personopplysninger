package personopplysninger

import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import no.nav.aap.kafka.streams.v2.config.StreamsConfig
import no.nav.aap.kafka.streams.v2.test.KStreamsMock
import no.nav.aap.ktor.client.AzureConfig
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import personopplysninger.graphql.PdlConfig
import personopplysninger.graphql.PdlGraphQLClient
import personopplysninger.rest.NorgClient
import personopplysninger.rest.NorgConfig
import java.io.File
import java.net.URI
import java.net.URL

class DescribeTopology {
    @BeforeEach
    fun setup() {
        System.setProperty("KAFKA_SCHEMA_REGISTRY", "mock://schema-reg.test")
        System.setProperty("KAFKA_SCHEMA_REGISTRY_USER", "")
        System.setProperty("KAFKA_SCHEMA_REGISTRY_PASSWORD", "")
        System.setProperty("KAFKA_TRUSTSTORE_PATH", "")
        System.setProperty("KAFKA_KEYSTORE_PATH", "")
        System.setProperty("KAFKA_CREDSTORE_PASSWORD", "")
    }

    @Test
    fun mermaid() {
        val pdlClient = PdlGraphQLClient(
            PdlConfig(URI.create("http://mock.io"), "scope"),
            AzureConfig(URL("http://azure.io"), "client", "secret")
        )
        val norgClient = NorgClient(NorgConfig(URL("http://norg.io")))
        val topology = topology(pdlClient, norgClient, true)

        val kafka = KStreamsMock()
        kafka.connect(topology, StreamsConfig("", ""), SimpleMeterRegistry())

        val mermaid = kafka.visulize().mermaid().generateDiagram()
        File("../docs/topology.mmd").apply { writeText(mermaid) }
        File("../docs/topology.md").apply { writeText(markdown(mermaid)) }
    }
}

private fun markdown(mermaid: String) = """
```mermaid
$mermaid
```
"""