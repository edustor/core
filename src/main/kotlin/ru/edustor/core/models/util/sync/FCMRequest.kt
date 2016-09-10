package ru.edustor.core.models.util.sync

import ru.edustor.core.models.Session
import ru.edustor.core.models.User

data class FCMRequest(val user: User, val activeSession: Session?, var retryNum: Int = 0)