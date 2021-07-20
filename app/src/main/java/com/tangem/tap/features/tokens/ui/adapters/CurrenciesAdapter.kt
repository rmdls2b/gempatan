package com.tangem.tap.features.tokens.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import com.tangem.blockchain.common.Blockchain
import com.tangem.blockchain.common.Token
import com.tangem.tap.common.extensions.getString
import com.tangem.tap.common.extensions.hide
import com.tangem.tap.common.extensions.loadCurrenciesIcon
import com.tangem.tap.common.extensions.show
import com.tangem.tap.domain.tokens.CardCurrencies
import com.tangem.tap.features.wallet.redux.WalletAction
import com.tangem.tap.store
import com.tangem.wallet.R
import kotlinx.android.synthetic.main.item_currency_subtitle.view.*
import kotlinx.android.synthetic.main.item_popular_token.view.*
import java.util.*

class CurrenciesAdapter : ListAdapter<CurrencyListItem, RecyclerView.ViewHolder>(DiffUtilCallback) {

    var addedCurrencies: CardCurrencies? = null

    private var unfilteredList = listOf<CurrencyListItem>()

    fun submitUnfilteredList(list: List<CurrencyListItem>) {
        unfilteredList = list
        submitList(list)
    }

    override fun getItemViewType(position: Int): Int {
        return when (currentList[position]) {
            is CurrencyListItem.TitleListItem -> 0
            is CurrencyListItem.BlockchainListItem, is CurrencyListItem.TokenListItem -> 1
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            0 -> TitleViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_currency_subtitle, parent, false)
            )
            1 -> CurrenciesViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_popular_token, parent, false)
            )
            else -> CurrenciesViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_popular_token, parent, false)
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val listItem = currentList[position]
        if (holder is TitleViewHolder && listItem is CurrencyListItem.TitleListItem) {
            holder.bind(listItem)
        } else if (holder is CurrenciesViewHolder) {
            holder.bind(listItem, addedCurrencies)
        }
    }

    object DiffUtilCallback : DiffUtil.ItemCallback<CurrencyListItem>() {
        override fun areContentsTheSame(
            oldItem: CurrencyListItem, newItem: CurrencyListItem
        ) = oldItem == newItem

        override fun areItemsTheSame(
            oldItem: CurrencyListItem, newItem: CurrencyListItem
        ) = oldItem == newItem
    }

    fun filter(query: CharSequence?) {
        val list = mutableListOf<CurrencyListItem>()

        if (!query.isNullOrEmpty()) {
            val queryNormalized = query.toString().toLowerCase(Locale.US)
            list.addAll(
                unfilteredList.filter { element ->
                    when (element) {
                        is CurrencyListItem.BlockchainListItem -> {
                            element.blockchain.currency.toLowerCase(Locale.US)
                                .contains(queryNormalized) ||
                                    element.blockchain.fullName.toLowerCase(Locale.US)
                                        .contains(queryNormalized)

                        }
                        is CurrencyListItem.TokenListItem -> {
                            element.token.name.toLowerCase(Locale.US)
                                .contains(queryNormalized) ||
                                    element.token.symbol.toLowerCase(Locale.US)
                                        .contains(queryNormalized)
                        }
                        is CurrencyListItem.TitleListItem -> true
                    }
                })
        } else {
            list.addAll(unfilteredList)
        }
        submitList(list)
    }

    class CurrenciesViewHolder(val view: View) :
        RecyclerView.ViewHolder(view) {
        fun bind(currency: CurrencyListItem, addedCurrencies: CardCurrencies?) {
            when (currency) {
                is CurrencyListItem.BlockchainListItem -> {
                    val blockchain = currency.blockchain
                    view.tv_currency_name.text = blockchain.fullName
                    view.tv_currency_symbol.text = blockchain.currency
                    val isAdded = addedCurrencies?.blockchains?.contains(blockchain) == true
                    view.btn_add_token.show(!isAdded)
                    view.btn_token_added.show(isAdded)

                    Picasso.get().loadCurrenciesIcon(
                        imageView = view.iv_currency,
                        textView = view.tv_token_letter,
                        blockchain = blockchain, token = null
                    )

                    view.btn_add_token.setOnClickListener {
                        store.dispatch(WalletAction.MultiWallet.AddBlockchain(blockchain))
                        view.btn_add_token.hide()
                        view.btn_token_added.show()
                    }
                }
                is CurrencyListItem.TokenListItem -> {
                    val token = currency.token
                    view.tv_currency_name.text = token.name
                    view.tv_currency_symbol.text = token.symbol

                    val isAdded = addedCurrencies?.tokens
                        ?.any {
                            it.symbol == token.symbol && it.contractAddress == token.contractAddress
                        } == true

                    view.btn_add_token.show(!isAdded)
                    view.btn_token_added.show(isAdded)

                    Picasso.get().loadCurrenciesIcon(
                        imageView = view.iv_currency,
                        textView = view.tv_token_letter,
                        token = token, blockchain = token.blockchain
                    )
                    view.btn_add_token.setOnClickListener {
                        store.dispatch(WalletAction.MultiWallet.AddToken(token))
                        view.btn_add_token.hide()
                        view.btn_token_added.show()
                    }
                }
            }
        }
    }

    class TitleViewHolder(val view: View) :
        RecyclerView.ViewHolder(view) {
        fun bind(title: CurrencyListItem.TitleListItem) {
            view.tv_subtitle.text = view.getString(title.titleResId).toUpperCase(Locale.US)
        }
    }
}


sealed class CurrencyListItem {
    data class TokenListItem(val token: Token) : CurrencyListItem()
    data class BlockchainListItem(val blockchain: Blockchain) : CurrencyListItem()
    data class TitleListItem(@StringRes val titleResId: Int) : CurrencyListItem()

    companion object {
        fun createListOfCurrencies(
            blockchains: List<Blockchain>,
            tokens: List<Token>
        ): List<CurrencyListItem> {
            val blockchainsTitle = R.string.add_tokens_subtitle_blockchains
            val ethereumTokensTitle = R.string.add_tokens_subtitle_ethereum_tokens
            val bscTokensTitle = R.string.add_tokens_subtitle_bsc_tokens
            val binanceTokensTitle = R.string.add_tokens_subtitle_binance_tokens

            val ethereumTokens = tokens.filter { it.blockchain == Blockchain.Ethereum }
            val bscTokens = tokens.filter { it.blockchain == Blockchain.BSC }
            val binanceTokens = tokens.filter { it.blockchain == Blockchain.Binance }
            return listOf(TitleListItem(blockchainsTitle)) +
                    blockchains.map { BlockchainListItem(it) } +
                    listOf(TitleListItem(ethereumTokensTitle)) +
                    ethereumTokens.map { TokenListItem(it) } +
                    listOf(TitleListItem(bscTokensTitle)) +
                    bscTokens.map { TokenListItem(it) } +
                    listOf(TitleListItem(binanceTokensTitle)) +
                    binanceTokens.map { TokenListItem(it) }
        }
    }
}


