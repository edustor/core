package ru.edustor.core.model.util.sync

import ru.edustor.core.model.Session
import ru.edustor.core.model.User

data class FCMRequest(val user: User, val activeSession: Session?, var retryNum: Int = 0)