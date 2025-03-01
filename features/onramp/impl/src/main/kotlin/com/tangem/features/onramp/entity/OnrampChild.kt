package com.tangem.features.onramp.entity

import kotlinx.serialization.Serializable

@Serializable
internal sealed class OnrampChild {
    @Serializable
    data object Main : OnrampChild()

    @Serializable
    data object Settings : OnrampChild()
}
