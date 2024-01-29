package com.tangem.features.send.impl.presentation.ui.fee

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.tangem.core.ui.components.notifications.Notification
import com.tangem.core.ui.res.TangemTheme
import com.tangem.features.send.impl.presentation.state.SendStates
import com.tangem.features.send.impl.presentation.state.fee.FeeSelectorState
import com.tangem.features.send.impl.presentation.state.fee.SendFeeNotification
import com.tangem.features.send.impl.presentation.viewmodel.SendClickIntents
import kotlinx.collections.immutable.ImmutableList

private const val FEE_SELECTOR_KEY = "FEE_SELECTOR_KEY"
private const val FEE_CUSTOM_KEY = "FEE_CUSTOM_KEY"

@Composable
internal fun SendSpeedAndFeeContent(state: SendStates.FeeState?, clickIntents: SendClickIntents) {
    if (state == null) return
    val feeSendState = state.feeSelectorState
    val notifications = state.notifications
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(TangemTheme.colors.background.tertiary)
            .padding(
                horizontal = TangemTheme.dimens.spacing16,
            ),
    ) {
        item(
            key = FEE_SELECTOR_KEY,
        ) {
            SendSpeedSelector(
                state = feeSendState,
                clickIntents = clickIntents,
            )
        }
        customFee(feeSendState)
        notifications(notifications)
        subtractButton(
            receivedAmount = state.receivedAmount,
            isSubtract = state.isSubtract,
            clickIntents = clickIntents,
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
internal fun LazyListScope.notifications(configs: ImmutableList<SendFeeNotification>, modifier: Modifier = Modifier) {
    items(
        items = configs,
        key = { it::class.java },
        contentType = { it::class.java },
        itemContent = {
            Notification(
                config = it.config,
                modifier = modifier
                    .padding(top = TangemTheme.dimens.spacing12)
                    .animateItemPlacement(),
                containerColor = when (it) {
                    is SendFeeNotification.Error.ExceedsBalance,
                    is SendFeeNotification.Warning.NetworkFeeUnreachable,
                    -> TangemTheme.colors.background.primary
                    else -> TangemTheme.colors.button.disabled
                },
                iconTint = when (it) {
                    is SendFeeNotification.Informational -> TangemTheme.colors.icon.accent
                    else -> null
                },
            )
        },
    )
}

@OptIn(ExperimentalFoundationApi::class)
internal fun LazyListScope.customFee(feeSendState: FeeSelectorState, modifier: Modifier = Modifier) {
    item(
        key = FEE_CUSTOM_KEY,
    ) {
        AnimatedVisibility(
            visible = feeSendState is FeeSelectorState.Content,
            modifier = modifier
                .fillMaxWidth()
                .animateItemPlacement()
                .background(TangemTheme.colors.background.tertiary),
        ) {
            (feeSendState as? FeeSelectorState.Content)?.let { fee ->
                val customValues = fee.customValues
                SendCustomFeeEthereum(
                    customValues = customValues,
                    selectedFee = fee.selectedFee,
                    modifier = Modifier.padding(top = TangemTheme.dimens.spacing12),
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
internal fun LazyListScope.subtractButton(
    receivedAmount: String,
    isSubtract: Boolean,
    clickIntents: SendClickIntents,
    modifier: Modifier = Modifier,
) {
    item {
        SendSpeedSubtract(
            receivingAmount = receivedAmount,
            isSubtract = isSubtract,
            onSelectClick = clickIntents::onSubtractSelect,
            modifier = modifier
                .padding(vertical = TangemTheme.dimens.spacing12)
                .animateItemPlacement(),
        )
    }
}
