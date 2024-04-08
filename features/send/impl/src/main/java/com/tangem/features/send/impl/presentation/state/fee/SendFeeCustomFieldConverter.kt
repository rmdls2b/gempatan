package com.tangem.features.send.impl.presentation.state.fee

import com.tangem.blockchain.common.transaction.Fee
import com.tangem.domain.appcurrency.model.AppCurrency
import com.tangem.domain.tokens.model.CryptoCurrencyStatus
import com.tangem.features.send.impl.presentation.state.fee.custom.BitcoinCustomFeeConverter
import com.tangem.features.send.impl.presentation.state.fee.custom.EthereumCustomFeeConverter
import com.tangem.features.send.impl.presentation.state.fields.SendTextField
import com.tangem.features.send.impl.presentation.viewmodel.SendClickIntents
import com.tangem.utils.Provider
import com.tangem.utils.converter.Converter
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

internal class SendFeeCustomFieldConverter(
    private val clickIntents: SendClickIntents,
    private val appCurrencyProvider: Provider<AppCurrency>,
    private val feeCryptoCurrencyStatusProvider: Provider<CryptoCurrencyStatus?>,
) : Converter<Fee, ImmutableList<SendTextField.CustomFee>> {

    private val ethereumCustomFeeConverter by lazy(LazyThreadSafetyMode.NONE) {
        EthereumCustomFeeConverter(
            clickIntents = clickIntents,
            appCurrencyProvider = appCurrencyProvider,
            feeCryptoCurrencyStatusProvider = feeCryptoCurrencyStatusProvider,
        )
    }

    private val bitcoinCustomFeeConverter by lazy(LazyThreadSafetyMode.NONE) {
        BitcoinCustomFeeConverter(
            clickIntents = clickIntents,
            appCurrencyProvider = appCurrencyProvider,
            feeCryptoCurrencyStatusProvider = feeCryptoCurrencyStatusProvider,
        )
    }

    override fun convert(value: Fee): ImmutableList<SendTextField.CustomFee> {
        return when (value) {
            is Fee.Ethereum -> ethereumCustomFeeConverter.convert(value)
            is Fee.Bitcoin -> bitcoinCustomFeeConverter.convert(value)
            else -> persistentListOf()
        }
    }

    fun onValueChange(feeSelectorState: FeeSelectorState.Content, index: Int, value: String) = feeSelectorState.copy(
        customValues = when (val fee = feeSelectorState.fees.normal) {
            is Fee.Ethereum -> ethereumCustomFeeConverter.onValueChange(
                customValues = feeSelectorState.customValues,
                index = index,
                value = value,
            )
            is Fee.Bitcoin -> bitcoinCustomFeeConverter.onValueChange(
                customValues = feeSelectorState.customValues,
                index = index,
                value = value,
                txSize = fee.txSize,
            )
            else -> feeSelectorState.customValues
        },
    )
}
