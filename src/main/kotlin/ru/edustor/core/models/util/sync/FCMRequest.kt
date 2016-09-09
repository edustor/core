package ru.edustor.core.models.util.sync

import ru.edustor.core.models.Session
import ru.edustor.core.models.User

class FCMRequest() {
    lateinit var user: User
    var activeSession: Session? = null

    constructor(user: User, session: Session? = null) : this() {
        this.user = user
        this.activeSession = session
    }
}