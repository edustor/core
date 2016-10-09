package ru.edustor.core.model.internal.sync

import ru.edustor.core.model.Account
import ru.edustor.core.model.Session

data class FCMRequest(val user: Account, val activeSession: Session?, var retryNum: Int = 0)