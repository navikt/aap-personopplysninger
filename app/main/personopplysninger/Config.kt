package personopplysninger

import no.nav.aap.kafka.streams.KStreamsConfig
import no.nav.aap.ktor.client.AzureConfig
import personopplysninger.rest.NorgConfig
import personopplysninger.graphql.PdlConfig


internal data class Config(
    val toggle: Toggle,
    val pdl: PdlConfig,
    val norg: NorgConfig,
    val azure: AzureConfig,
    val kafka: KStreamsConfig,
)

internal data class Toggle(
    val settOppAktørStream: Boolean,
)
