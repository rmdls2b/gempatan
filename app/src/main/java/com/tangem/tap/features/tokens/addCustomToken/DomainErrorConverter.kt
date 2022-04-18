package com.tangem.tap.features.tokens.addCustomToken

import android.content.Context
import com.tangem.domain.DomainError
import com.tangem.domain.ErrorConverter
import com.tangem.domain.features.addCustomToken.AddCustomTokenError
import com.tangem.domain.features.addCustomToken.AddCustomTokenWarning
import com.tangem.wallet.R

/**
 * Created by Anton Zhilenkov on 06/04/2022.
 */
class DomainErrorConverter(
    private val context: Context
) : ErrorConverter<String> {

    override fun convertError(error: DomainError): String {
        val errorMessage = when (error) {
            is AddCustomTokenError -> AddCustomTokenConverter(context).convertError(error)
            else -> null
        }
        return errorMessage?.let { it } ?: "Unknown error: ${error::class.java.simpleName}"
    }
}

private class AddCustomTokenConverter(
    private val context: Context
) : ErrorConverter<String> {

    override fun convertError(error: DomainError): String {
        val customTokenError = (error as? AddCustomTokenError) ?: throw UnsupportedOperationException()

        val rawMessage = when (customTokenError) {
            AddCustomTokenWarning.PotentialScamToken -> R.string.custom_token_validation_error_not_found
            AddCustomTokenWarning.TokenAlreadyAdded -> R.string.custom_token_validation_error_already_added
            AddCustomTokenError.InvalidContractAddress -> R.string.custom_token_creation_error_invalid_contract_address
            AddCustomTokenError.NetworkIsNotSelected -> R.string.custom_token_creation_error_network_not_selected
            AddCustomTokenError.InvalidDerivationPath -> R.string.custom_token_creation_error_invalid_derivation_path
            AddCustomTokenError.InvalidDecimalsCount -> {
                context.getString(R.string.custom_token_creation_error_wrong_decimals, 30)
            }
            AddCustomTokenError.FieldIsEmpty -> R.string.custom_token_creation_error_required_field
            else -> null
        }
        return when (rawMessage) {
            is Int -> context.getString(rawMessage)
            is String -> rawMessage
            else -> "Unknown error: ${customTokenError::class.java.simpleName}"
        }
    }
}