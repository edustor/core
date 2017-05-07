package ru.edustor.core.model.internal.sync

data class FCMRequest(val accountId: String, var retryNum: Int = 0)