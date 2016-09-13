package ru.edustor.core.model.internal.sync

import ru.edustor.core.model.User

class SyncTask() {
    lateinit var method: String
    lateinit var params: Map<String, String>
    lateinit var user: User

    constructor(method: String, params: Map<String, String>, user: User) : this() {
        this.method = method
        this.params = params
        this.user = user
    }
}