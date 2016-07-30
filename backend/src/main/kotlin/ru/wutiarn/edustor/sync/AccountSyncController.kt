package ru.wutiarn.edustor.sync

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import ru.wutiarn.edustor.api.account.AccountController
import ru.wutiarn.edustor.models.util.sync.SyncTask

@Component
open class AccountSyncController @Autowired constructor(val accountController: AccountController) {
    fun processTask(task: SyncTask): Any? {
        when (task.method) {
            "FCMToken/put" -> setFCMToken(task)
        }
        return null
    }

    fun setFCMToken(syncTask: SyncTask) {
        accountController.setFCMToken(syncTask.params["token"], syncTask.user)
    }
}