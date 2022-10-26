package com.tangem.tap.features.onboarding.products.wallet.saltPay

import android.net.Uri
import com.tangem.blockchain.blockchains.ethereum.SignedEthereumTransaction
import com.tangem.blockchain.common.Amount
import com.tangem.blockchain.common.AmountType
import com.tangem.blockchain.common.BlockchainSdkError
import com.tangem.blockchain.common.TransactionSigner
import com.tangem.blockchain.extensions.successOr
import com.tangem.common.extensions.guard
import com.tangem.common.extensions.isZero
import com.tangem.common.services.Result
import com.tangem.domain.common.extensions.successOr
import com.tangem.network.api.paymentology.AttestationResponse
import com.tangem.network.api.paymentology.PaymentologyApiService
import com.tangem.network.api.paymentology.RegisterKYCRequest
import com.tangem.network.api.paymentology.RegisterWalletRequest
import com.tangem.network.api.paymentology.RegisterWalletResponse
import com.tangem.network.api.paymentology.RegistrationResponse
import com.tangem.operations.attestation.AttestWalletKeyResponse
import com.tangem.tap.common.extensions.safeUpdate
import com.tangem.tap.domain.getFirstToken
import com.tangem.tap.domain.tokens.UserWalletId
import com.tangem.tap.features.onboarding.products.wallet.saltPay.message.SaltPayActivationError
import java.math.BigDecimal

/**
 * Created by Anton Zhilenkov on 15.10.2022.
 */
class SaltPayActivationManager(
    val cardId: String,
    val cardPublicKey: ByteArray,
    val walletPublicKey: ByteArray,
    private val kycProvider: KYCProvider,
    private val paymentologyService: PaymentologyApiService,
    private val gnosisRegistrator: GnosisRegistrator,
) {
    val kycUrlProvider = KYCUrlProvider(walletPublicKey, kycProvider)

    private val approvalValue: BigDecimal = BigDecimal(2).pow(256).minus(BigDecimal.ONE)
        .movePointLeft(gnosisRegistrator.walletManager.wallet.blockchain.decimals())

    private val spendLimitValue: BigDecimal = BigDecimal("100")

    suspend fun checkHasGas(): Result<Unit> {
        return when (val hasGasResult = gnosisRegistrator.checkHasGas()) {
            is com.tangem.blockchain.extensions.Result.Success -> if (hasGasResult.data) {
                Result.Success(Unit)
            } else {
                Result.Failure(SaltPayActivationError.NoGas)
            }
            is com.tangem.blockchain.extensions.Result.Failure -> Result.Failure(hasGasResult.error as BlockchainSdkError)
        }
    }

    suspend fun registerKYC(): Result<Unit> {
        return when (val result = paymentologyService.registerKYC(makeRegisterKYCRequest())) {
            is Result.Success -> Result.Success(Unit)
            is Result.Failure -> result
        }
    }

    suspend fun checkRegistration(): Result<RegistrationResponse.Item> {
        val registrationResponse = paymentologyService.checkRegistration(cardId, cardPublicKey).successOr { return it }

        return try {
            if (!registrationResponse.success || registrationResponse.results.isEmpty()) {
                throw SaltPayActivationError.EmptyResponse(registrationResponse.error)
            }
            Result.Success(registrationResponse.results[0])
        } catch (ex: SaltPayActivationError) {
            Result.Failure(ex)
        }
    }

    suspend fun requestAttestationChallenge(): Result<AttestationResponse> {
        return paymentologyService.requestAttestationChallenge(cardId, cardPublicKey)
    }

    suspend fun sendTransactions(
        signedTransactions: List<SignedEthereumTransaction>,
    ): com.tangem.blockchain.extensions.Result<List<String>> {
        return gnosisRegistrator.sendTransactions(signedTransactions)
    }

    suspend fun registerWallet(
        attestResponse: AttestWalletKeyResponse,
        pinCode: String,
    ): Result<RegisterWalletResponse> {
        val request = RegisterWalletRequest(
            cardId = cardId,
            publicKey = cardPublicKey,
            walletPublicKey = walletPublicKey,
            walletSalt = attestResponse.salt,
            walletSignature = attestResponse.walletSignature,
            cardSalt = attestResponse.publicKeySalt ?: byteArrayOf(),
            cardSignature = attestResponse.cardSignature ?: byteArrayOf(),
            pin = pinCode,
        )

        return paymentologyService.registerWallet(request)
    }

    suspend fun getAmountToClaim(): Result<Amount> {
        val amount = gnosisRegistrator.getAllowance().successOr {
            return Result.Failure(SaltPayActivationError.FailedToGetFundsToClaim)
        }

        return if (amount.value == null || amount.value?.isZero() == true) {
            Result.Failure(SaltPayActivationError.NoFundsToClaim)
        } else {
            Result.Success(amount)
        }
    }

    suspend fun claim(amountToClaim: BigDecimal, signer: TransactionSigner): Result<Unit> {
        val result = gnosisRegistrator.transferFrom(amountToClaim, signer).successOr {
            return Result.Failure(SaltPayActivationError.ClaimTransactionFailed)
        }
        return Result.Success(Unit)
    }

    suspend fun getTokenAmount(): Result<BigDecimal> {
        val wallet = gnosisRegistrator.walletManager.safeUpdate().successOr { return it }
        val token = wallet.getFirstToken().guard {
            throw UnsupportedOperationException()
        }

        val amountValue = wallet.amounts[AmountType.Token(token)]?.value.guard {
            throw UnsupportedOperationException()
        }

        return Result.Success(amountValue)
    }

    fun makeRegistrationTask(challenge: ByteArray): SaltPayRegistrationTask {
        return SaltPayRegistrationTask(
            gnosisRegistrator = gnosisRegistrator,
            challenge = challenge,
            walletPublicKey = walletPublicKey,
            approvalValue = approvalValue,
            spendLimitValue = spendLimitValue,
        )
    }

    private fun makeRegisterKYCRequest(): RegisterKYCRequest = RegisterKYCRequest(
        cardId = cardId,
        publicKey = cardPublicKey,
        kycProvider = "UTORG",
        kycRefId = kycUrlProvider.kycRefId,
    )

    companion object {
        fun stub(): SaltPayActivationManager = SaltPayActivationManager(
            kycProvider = SaltPayConfig.stub().kycProvider,
            paymentologyService = PaymentologyApiService.stub(),
            gnosisRegistrator = GnosisRegistrator.stub(),
            cardId = "",
            cardPublicKey = byteArrayOf(),
            walletPublicKey = byteArrayOf(),
        )
    }
}

class KYCUrlProvider(
    walletPublicKey: ByteArray,
    kycProvider: KYCProvider,
) {

    val kycRefId = UserWalletId(walletPublicKey).stringValue

    val requestUrl: String = makeWebRequestUrl(kycProvider)

    val doneUrl = "https://success.tangem.com/"

    private fun makeWebRequestUrl(kycProvider: KYCProvider): String {
        val baseUri = Uri.parse(kycProvider.baseUrl)
        return Uri.Builder()
            .scheme(baseUri.scheme)
            .authority(baseUri.authority)
            .encodedPath(baseUri.path)
            .appendQueryParameter(kycProvider.sidParameterKey, kycProvider.sidValue)
            .appendQueryParameter(kycProvider.externalIdParameterKey, kycRefId)
            .build().toString()
    }
}

