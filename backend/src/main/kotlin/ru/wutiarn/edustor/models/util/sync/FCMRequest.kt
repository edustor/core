package ru.wutiarn.edustor.models.util.sync

import ru.wutiarn.edustor.models.Session
import ru.wutiarn.edustor.models.User

class FCMRequest() {
    lateinit var user: User
    var activeSession: Session? = null

    constructor(user: User, session: Session? = null) : this() {
        this.user = user
        this.activeSession = session
    }
}