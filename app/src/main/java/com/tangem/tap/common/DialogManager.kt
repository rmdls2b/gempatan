package com.tangem.tap.common

import android.app.Dialog
import android.content.Context
import com.tangem.tap.common.redux.global.GlobalState
import com.tangem.tap.features.details.redux.walletconnect.WalletConnectDialog
import com.tangem.tap.features.details.ui.walletconnect.dialogs.*
import com.tangem.tap.store
import com.tangem.wallet.R
import org.rekotlin.StoreSubscriber

class DialogManager : StoreSubscriber<GlobalState> {
    var context: Context? = null
    private var dialog: Dialog? = null

    fun onStart(context: Context) {
        this.context = context
        store.subscribe(this) { state ->
            state.skipRepeats { oldState, newState ->
                oldState.globalState == newState.globalState
            }.select { it.globalState }
        }
    }

    fun onStop() {
        this.context = null
        store.unsubscribe(this)
    }


    override fun newState(state: GlobalState) {
        if (state.dialog == null) {
            dialog?.dismiss()
            dialog = null
            return
        }
        val context = context ?: return
        if (dialog != null) return

        when (state.dialog) {
            is WalletConnectDialog.UnsupportedCard ->
                dialog = SimpleAlertDialog.create(
                    titleRes = R.string.wallet_connect,
                    messageRes = R.string.wallet_connect_scanner_error_no_ethereum_wallet,
                    context = context
                )
            is WalletConnectDialog.OpeningSessionRejected -> {
                dialog = SimpleAlertDialog.create(
                    titleRes = R.string.wallet_connect,
                    messageRes = R.string.wallet_connect_same_wcuri,
                    context = context
                )
            }
            is WalletConnectDialog.SessionTimeout -> {
                dialog = SimpleAlertDialog.create(
                    titleRes = R.string.wallet_connect,
                    messageRes = R.string.wallet_connect_error_timeout,
                    context = context
                )
            }
            is WalletConnectDialog.ApproveWcSession ->
                dialog = ApproveWcSessionDialog.create(state.dialog.session, context)
            is WalletConnectDialog.ClipboardOrScanQr ->
                dialog = ClipboardOrScanQrDialog.create(state.dialog.clipboardUri, context)
            is WalletConnectDialog.RequestTransaction ->
                dialog = TransactionDialog.create(state.dialog.dialogData, context)
            is WalletConnectDialog.PersonalSign ->
                dialog = PersonalSignDialog.create(state.dialog.data, context)
        }
        dialog?.show()
    }
}
