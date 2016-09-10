package ru.edustor.core.sync

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import ru.edustor.core.api.account.AccountController
import ru.edustor.core.model.util.sync.SyncTask

@Component
open class AccountSyncController @Autowired constructor(val accountController: AccountController) {
    fun processTask(task: SyncTask): Any {
        when (task.method) {
            "FCMToken/put" -> return setFCMToken(task)
            else -> throw NoSuchMethodException("AccountSyncController cannot resolve ${task.method}")
        }
    }

    fun setFCMToken(syncTask: SyncTask) {
        accountController.setFCMToken(syncTask.params["token"], syncTask.user)
    }
}