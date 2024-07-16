package com.tangem.tap.features.tokens.impl.presentation.router

import com.tangem.blockchain.common.Blockchain
import com.tangem.core.navigation.AppScreen
import com.tangem.core.navigation.NavigationAction
import com.tangem.tap.common.extensions.dispatchDialogShow
import com.tangem.tap.common.extensions.dispatchNotification
import com.tangem.tap.common.redux.AppDialog
import com.tangem.tap.store
import com.tangem.wallet.R

/**
 * Default implementation of tokens list router
 * FIXME("Necessary to avoid using redux actions")
 *
 * @author Andrew Khokhlov on 24/03/2023
 */
internal class DefaultTokensListRouter : TokensListRouter {

    override fun popBackStack() {
        store.dispatch(NavigationAction.PopBackTo())
    }

    override fun openAddCustomTokenScreen() {
        store.dispatch(NavigationAction.NavigateTo(AppScreen.AddCustomToken))
    }

    override fun showAddressCopiedNotification() {
        store.dispatchNotification(R.string.contract_address_copied_message)
    }

    override fun openUnableHideMainTokenAlert(tokenName: String, tokenSymbol: String, networkName: String) {
        store.dispatchDialogShow(
            dialog = AppDialog.TokensAreLinkedDialog(
                currencyTitle = tokenName,
                currencySymbol = tokenSymbol,
                networkName = networkName,
            ),
        )
    }

    override fun openRemoveWalletAlert(tokenName: String, onOkClick: () -> Unit) {
        store.dispatchDialogShow(
            dialog = AppDialog.RemoveWalletDialog(currencyTitle = tokenName, onOk = onOkClick),
        )
    }

    override fun openUnsupportedNetworkAlert(blockchain: Blockchain) {
        val alert = AppDialog.SimpleOkDialogRes(
            headerId = R.string.common_warning,
            messageId = R.string.alert_manage_tokens_unsupported_curve_message,
            args = listOf(blockchain.fullName),
        )
        store.dispatchDialogShow(alert)
    }

    override fun showGenericErrorAlertAndPopBack() {
        val alert = AppDialog.SimpleOkDialogRes(
            headerId = R.string.common_error,
            messageId = R.string.common_unknown_error,
            onOk = { store.dispatch(NavigationAction.PopBackTo()) },
        )
        store.dispatchDialogShow(alert)
    }

    override fun openNetworkTokensNotSupportAlert(networkName: String) {
        store.dispatchDialogShow(
            AppDialog.SimpleOkDialogRes(
                headerId = R.string.common_warning,
                messageId = R.string.alert_manage_tokens_unsupported_message,
                args = listOf(networkName),
            ),
        )
    }
}
