package ru.wutiarn.edustor.models.util.sync

class SyncTask() {
    lateinit var method: String
    lateinit var params: Map<String, String>

    constructor(method: String, params: Map<String, String>) : this() {
        this.method = method
        this.params = params
    }
}