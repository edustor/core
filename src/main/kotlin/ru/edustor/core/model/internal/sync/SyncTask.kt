package ru.edustor.core.model.internal.sync

import com.fasterxml.jackson.annotation.JsonIgnore
import ru.edustor.core.model.Account

data class SyncTask(
        val method: String,
        val params: Map<String, String>
) {
    @JsonIgnore lateinit var account: Account

    constructor(method: String, params: Map<String, String>, user: Account) : this(method, params) {
        this.account = user
    }
}