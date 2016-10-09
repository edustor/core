package ru.edustor.core.model.internal.sync

import ru.edustor.core.model.Account

data class FCMRequest(val account: Account, var retryNum: Int = 0)