package personopplysninger

import no.nav.aap.kafka.streams.v2.config.StreamsConfig
import no.nav.aap.ktor.client.AzureConfig
import personopplysninger.graphql.PdlConfig
import personopplysninger.rest.NorgConfig


internal data class Config(
    val toggle: Toggle,
    val pdl: PdlConfig,
    val norg: NorgConfig,
    val azure: AzureConfig,
    val kafka: StreamsConfig,
)

internal data class Toggle(
    val settOppAktørStream: Boolean,
)
