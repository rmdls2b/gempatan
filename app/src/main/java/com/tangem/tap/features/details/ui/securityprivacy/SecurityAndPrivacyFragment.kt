package com.tangem.tap.features.details.ui.securityprivacy

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import by.kirich1409.viewbindingdelegate.viewBinding
import com.tangem.tap.common.extensions.show
import com.tangem.tap.common.redux.navigation.AppScreen
import com.tangem.tap.common.redux.navigation.NavigationAction
import com.tangem.tap.features.BaseStoreFragment
import com.tangem.tap.features.details.redux.DetailsAction
import com.tangem.tap.features.details.redux.DetailsState
import com.tangem.tap.features.details.redux.PrivacySetting
import com.tangem.tap.features.details.redux.SecurityOption
import com.tangem.tap.store
import com.tangem.wallet.R
import com.tangem.wallet.databinding.FragmentDetailsSecurityPrivacyBinding
import org.rekotlin.StoreSubscriber

class SecurityAndPrivacyFragment : BaseStoreFragment(R.layout.fragment_details_security_privacy),
    StoreSubscriber<DetailsState> {

    private val binding: FragmentDetailsSecurityPrivacyBinding
            by viewBinding(FragmentDetailsSecurityPrivacyBinding::bind)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.onBackPressedDispatcher?.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                store.dispatch(NavigationAction.PopBackTo())
            }
        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.setNavigationOnClickListener {
            store.dispatch(NavigationAction.PopBackTo())
        }
        setListeners()

    }

    private fun setListeners() = with(binding) {
        tvAccessCode.setOnClickListener {
            store.dispatch(NavigationAction.NavigateTo(AppScreen.ChangeAccessCode))
        }

        switchSaveCards.setOnCheckedChangeListener { _, isChecked ->
            store.dispatch(
                DetailsAction.ManagePrivacy.SwitchPrivacySetting(
                    allow = isChecked,
                    setting = PrivacySetting.SAVE_CARDS
                )
            )
        }

        switchSavePasswords.setOnCheckedChangeListener { _, isChecked ->
            store.dispatch(
                DetailsAction.ManagePrivacy.SwitchPrivacySetting(
                    allow = isChecked,
                    setting = PrivacySetting.SAVE_ACCESS_CODE
                )
            )
        }
    }

    override fun newState(state: DetailsState) {
        with(binding) {

            val card = state.scanResponse?.card
            llAccessManagement.show(card != null)
            if (card != null) {
                llAccessManagement.setOnClickListener {
                    store.dispatch(
                        DetailsAction.ManageSecurity.CheckCurrentSecurityOption(
                            card
                        )
                    )
                }
            }

            val currentSecurity = when (state.securityScreenState?.currentOption) {
                SecurityOption.LongTap -> R.string.details_manage_security_long_tap
                SecurityOption.PassCode -> R.string.details_manage_security_passcode
                SecurityOption.AccessCode -> R.string.details_manage_security_access_code
                null -> null
            }
            currentSecurity?.let { tvAccessManagement.text = getString(it) }

            switchSaveCards.isChecked = state.saveCards
            switchSavePasswords.isChecked = state.savePasswords

            tvAccessCode.show(state.scanResponse?.card?.isAccessCodeSet == true)
        }

    }

    override fun subscribeToStore() {
        store.subscribe(this) { state ->
            state.skipRepeats { oldState, newState ->
                oldState.detailsState == newState.detailsState
            }.select { it.detailsState }
        }
        storeSubscribersList.add(this)
    }

}