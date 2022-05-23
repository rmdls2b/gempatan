package com.tangem.tap.features.wallet.ui.wallet

import android.app.Dialog
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.tangem.common.card.Card
import com.tangem.domain.common.TapWorkarounds.derivationStyle
import com.tangem.domain.common.TapWorkarounds.isTestCard
import com.tangem.tap.common.extensions.animateVisibility
import com.tangem.tap.common.extensions.formatAmountAsSpannedString
import com.tangem.tap.common.extensions.hide
import com.tangem.tap.common.extensions.show
import com.tangem.tap.common.redux.StateDialog
import com.tangem.tap.common.redux.navigation.AppScreen
import com.tangem.tap.common.redux.navigation.NavigationAction
import com.tangem.tap.currenciesRepository
import com.tangem.tap.features.tokens.redux.TokensAction
import com.tangem.tap.features.wallet.models.TotalBalance
import com.tangem.tap.features.wallet.redux.WalletAction
import com.tangem.tap.features.wallet.redux.WalletDialog
import com.tangem.tap.features.wallet.redux.WalletState
import com.tangem.tap.features.wallet.ui.BalanceStatus
import com.tangem.tap.features.wallet.ui.WalletFragment
import com.tangem.tap.features.wallet.ui.adapters.WalletAdapter
import com.tangem.tap.features.wallet.ui.dialogs.SignedHashesWarningDialog
import com.tangem.tap.store
import com.tangem.wallet.R
import com.tangem.wallet.databinding.FragmentWalletBinding


class MultiWalletView : WalletView {

    private var fragment: WalletFragment? = null
    private var binding: FragmentWalletBinding? = null
    private var dialog: Dialog? = null

    private lateinit var walletsAdapter: WalletAdapter


    override fun changeWalletView(fragment: WalletFragment, binding: FragmentWalletBinding) {
        setFragment(fragment, binding)
        onViewCreated()
        showMultiWalletView(binding)
    }


    private fun showMultiWalletView(binding: FragmentWalletBinding) = with(binding) {
        tvTwinCardNumber.hide()
        rvPendingTransaction.hide()
        lCardBalance.root.hide()
        lAddress.root.hide()
        lButtonsShort.root.hide()
        lButtonsLong.root.hide()
        rvMultiwallet.show()
        btnAddToken.show()
        setupWalletCardNumber(binding)
    }

    private fun setupWalletCardNumber(binding: FragmentWalletBinding) = with(binding) {
        val card = store.state.globalState.scanResponse?.card
        if (card?.backupStatus is Card.BackupStatus.Active) {
            val cardCount = (card.backupStatus as Card.BackupStatus.Active).cardCount + 1
            tvTwinCardNumber.show()
            tvTwinCardNumber.text =
                fragment?.getString(R.string.wallet_twins_chip_format, 1, cardCount)
        } else {
            tvTwinCardNumber.hide()
        }
    }

    override fun setFragment(fragment: WalletFragment, binding: FragmentWalletBinding) {
        this.fragment = fragment
        this.binding = binding
    }

    override fun removeFragment() {
        this.fragment = null
        this.binding = null
    }

    override fun onViewCreated() {
        setupWalletsRecyclerView()
    }

    private fun setupWalletsRecyclerView() {
        val fragment = fragment ?: return
        walletsAdapter = WalletAdapter()
        walletsAdapter.setHasStableIds(true)
        binding?.rvMultiwallet?.layoutManager = LinearLayoutManager(fragment.requireContext())
        binding?.rvMultiwallet?.adapter = walletsAdapter
    }

    override fun onNewState(state: WalletState) {
        val fragment = fragment ?: return
        val binding = binding ?: return

        handleTotalBalance(binding, state.totalBalance)
        walletsAdapter.submitList(state.walletsData, state.primaryBlockchain, state.primaryToken)

        binding.btnAddToken.setOnClickListener {
            val card = store.state.globalState.scanResponse!!.card
            store.dispatch(
                TokensAction.LoadCurrencies(
                    supportedBlockchains = currenciesRepository.getBlockchains(
                        card.firmwareVersion,
                        card.isTestCard
                    ),
                    scanResponse = store.state.globalState.scanResponse
                )
            )
            store.dispatch(TokensAction.AllowToAddTokens(true))
            store.dispatch(
                TokensAction.SetAddedCurrencies(
                    wallets = state.walletsData,
                    derivationStyle = card.derivationStyle
                )
            )
            store.dispatch(
                TokensAction.SetNonRemovableCurrencies(
                    state.walletsData.filterNot { state.canBeRemoved(it) })
            )
            store.dispatch(NavigationAction.NavigateTo(AppScreen.AddTokens))
        }
        handleErrorStates(state = state, binding = binding, fragment = fragment)
        handleDialogs(state.walletDialog)
    }

    private fun handleTotalBalance(
        binding: FragmentWalletBinding,
        totalBalance: TotalBalance?,
    ) = with(binding.lCardTotalBalance) {
        if (totalBalance == null) {
            this.root.hide()
            return@with
        }

        tvBalance.animateVisibility(
            show = totalBalance.state != TotalBalance.State.Loading,
            hiddenVisibility = View.INVISIBLE
        )
        pbLoading.animateVisibility(
            show = totalBalance.state == TotalBalance.State.Loading
        )
        tvProcessing.animateVisibility(
            show = totalBalance.state == TotalBalance.State.SomeTokensFailed
        )

        tvBalance.text = totalBalance.fiatAmount.formatAmountAsSpannedString(
            currencySymbol = totalBalance.fiatCurrency.symbol
        )
        tvCurrencyName.text = totalBalance.fiatCurrency.code

        tvCurrencyName.setOnClickListener {
            store.dispatch(WalletAction.AppCurrencyAction.ChooseAppCurrency)
        }
    }

    private fun handleErrorStates(
        state: WalletState,
        binding: FragmentWalletBinding,
        fragment: WalletFragment
    ) {
        when (state.primaryWallet?.currencyData?.status) {
            BalanceStatus.EmptyCard -> {
                showErrorState(
                    binding,
                    fragment.getText(R.string.wallet_error_empty_card),
                    fragment.getString(R.string.wallet_error_empty_card_subtitle)
                )
                configureButtonsForEmptyWalletState(binding)
            }
            BalanceStatus.UnknownBlockchain -> {
                showErrorState(
                    binding,
                    fragment.getText(R.string.wallet_error_unsupported_blockchain),
                    fragment.getString(R.string.wallet_error_unsupported_blockchain_subtitle)
                )
            }
            else -> { /* no-op */
            }
        }
    }

    private fun showErrorState(
        binding: FragmentWalletBinding, errorTitle: CharSequence, errorDescription: CharSequence,
    ) = with(binding) {
        lCardBalance.root.show()
        with(lCardBalance) {
            lBalance.root.hide()
            lBalanceError.root.show()
            rvMultiwallet.show()
            btnAddToken.hide()
            lBalanceError.tvErrorTitle.text = errorTitle
            lBalanceError.tvErrorDescriptions.text = errorDescription
        }
    }

    private fun configureButtonsForEmptyWalletState(binding: FragmentWalletBinding) =
        with(binding) {
            lButtonsLong.root.show()
            lButtonsLong.btnScanLong.setOnClickListener { store.dispatch(WalletAction.Scan) }
            lButtonsLong.btnConfirmLong.setOnClickListener { store.dispatch(WalletAction.CreateWallet) }
            lButtonsLong.btnConfirmLong.text =
                fragment?.getText(R.string.wallet_button_create_wallet)
        }

    private fun handleDialogs(walletDialog: StateDialog?) {
        val fragment = fragment ?: return
        val context = fragment.context ?: return
        when (walletDialog) {
            is WalletDialog.SignedHashesMultiWalletDialog -> {
                if (dialog == null) {
                    dialog = SignedHashesWarningDialog.create(context).apply { show() }
                }
            }
            else -> {
                dialog?.dismiss()
                dialog = null
            }
        }
    }
}
