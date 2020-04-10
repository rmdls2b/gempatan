package com.tangem.tangemtest.ucase.domain.paramsManager.managers

import com.tangem.TangemSdk
import com.tangem.tangemtest._arch.structure.abstraction.Item
import com.tangem.tangemtest.ucase.domain.actions.ScanAction
import com.tangem.tangemtest.ucase.domain.paramsManager.ActionCallback

/**
 * Created by Anton Zhilenkov on 19/03/2020.
 */
class ScanItemsManager : BaseItemsManager(ScanAction()) {
    override fun createItemsList(): List<Item> = listOf()

    override fun invokeMainAction(tangemSdk: TangemSdk, callback: ActionCallback) {
        action.executeMainAction(this, getAttrsForAction(tangemSdk), callback)
    }
}