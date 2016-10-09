package ru.edustor.core.model.internal.sync

import ru.edustor.core.model.Account

class SyncTask() {
    lateinit var method: String
    lateinit var params: Map<String, String>
    lateinit var user: Account

    constructor(method: String, params: Map<String, String>, user: Account) : this() {
        this.method = method
        this.params = params
        this.user = user
    }
}