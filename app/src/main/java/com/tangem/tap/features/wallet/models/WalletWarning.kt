package com.tangem.tap.features.wallet.models

sealed class WalletWarning(
    val showingPosition: Int,
) {
    data class ExistentialDeposit(
        val blockchainFullName: String,
        val existentialDepositString: String,
    ) : WalletWarning(1)

    object TransactionInProgress : WalletWarning(10)
    data class BalanceNotEnoughForFee(val blockchainFullName: String) : WalletWarning(30)
    data class Rent(val walletRent: WalletRent) : WalletWarning(40)
}

data class WalletWarningDescription(
    val title: String,
    val message: String,
)

data class WalletRent(
    val minRentValue: String,
    val rentExemptValue: String,
)
