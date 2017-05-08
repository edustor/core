package ru.edustor.core.sync

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import ru.edustor.core.model.internal.sync.SyncTask
import ru.edustor.core.rest.AccountController

@Component
open class AccountsSyncController @Autowired constructor(val accountController: AccountController) {
    fun processTask(task: SyncTask): Any {
        when (task.method) {
            "FCMToken/put" -> return setFCMToken(task)
            else -> throw NoSuchMethodException("AccountSyncController cannot resolve ${task.method}")
        }
    }

    fun setFCMToken(syncTask: SyncTask) {
        accountController.setFCMToken(syncTask.params["token"], syncTask.account)
    }
}