package com.tangem.tap.common.redux.global

import com.tangem.TangemSdkError
import com.tangem.commands.common.network.Result
import com.tangem.common.CompletionResult
import com.tangem.tap.common.extensions.dispatchOnMain
import com.tangem.tap.common.redux.AppState
import com.tangem.tap.domain.configurable.warningMessage.WarningMessagesManager
import com.tangem.tap.features.home.redux.HomeAction
import com.tangem.tap.features.send.redux.SendAction
import com.tangem.tap.features.wallet.redux.WalletAction
import com.tangem.tap.network.moonpay.MoonpayService
import com.tangem.tap.preferencesStorage
import com.tangem.tap.scope
import com.tangem.tap.store
import kotlinx.coroutines.launch
import org.rekotlin.Middleware

val globalMiddleware: Middleware<AppState> = { dispatch, appState ->
    { nextDispatch ->
        { action ->
            when (action) {
                is GlobalAction.ScanFailsCounter.ChooseBehavior -> {
                    when (action.result) {
                        is CompletionResult.Success -> store.dispatch(GlobalAction.ScanFailsCounter.Reset)
                        is CompletionResult.Failure -> {
                            if (action.result.error is TangemSdkError.UserCancelled) {
                                store.dispatch(GlobalAction.ScanFailsCounter.Increment)
                                if (store.state.globalState.scanCardFailsCounter >= 2) {
                                    store.dispatch(HomeAction.ShowDialog.ScanFails)
                                    store.dispatch(WalletAction.ShowDialog.ScanFails)
                                }
                            } else {
                                store.dispatch(GlobalAction.ScanFailsCounter.Reset)
                            }
                        }
                    }
                }
                is GlobalAction.RestoreAppCurrency -> {
                    store.dispatch(GlobalAction.RestoreAppCurrency.Success(
                            preferencesStorage.getAppCurrency()
                    ))
                }
                is GlobalAction.HideWarningMessage -> {
                    store.state.globalState.warningManager?.let {
                        if (it.hideWarning(action.warning)) {
                            if (WarningMessagesManager.isAlreadySignedHashesWarning(action.warning)) {
                                //TODO: No appropriate warningMessage identification. Make it better later
                                store.dispatch(WalletAction.Warnings.CheckHashesCount.SaveCardId)
                            }

                            store.dispatch(WalletAction.Warnings.Update)
                            store.dispatch(SendAction.Warnings.Update)
                        }
                    }
                }

                is GlobalAction.SendFeedback -> {
                    store.state.globalState.feedbackManager?.send(action.emailData)
                }
                is GlobalAction.UpdateWalletSignedHashes -> {
                    store.dispatch(WalletAction.Warnings.CheckRemainingSignatures(action.remainingSignatures))
                }
                is GlobalAction.UpdateFeedbackInfo -> {
                    store.state.globalState.feedbackManager?.infoHolder
                        ?.setWalletsInfo(action.walletManagers)
                }
                is GlobalAction.GetMoonPayUserStatus -> {
                    val apiKey = appState()?.globalState?.configManager?.config?.moonPayApiKey
                    if (apiKey != null) {
                        scope.launch {
                            val userStatusResponse = MoonpayService().getUserStatus(apiKey)
                            if (userStatusResponse is Result.Success) {
                                store.dispatchOnMain(
                                    GlobalAction.GetMoonPayUserStatus.Success(userStatusResponse.data)
                                )
                            }
                        }
                    }
                }
            }
            nextDispatch(action)
        }
    }
}