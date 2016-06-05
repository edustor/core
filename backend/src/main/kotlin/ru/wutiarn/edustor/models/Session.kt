package ru.wutiarn.edustor.models

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.DBRef
import org.springframework.data.mongodb.core.mapping.Document
import java.util.*

@Document
class Session() {
    @DBRef @Indexed lateinit var user: User
    @Indexed val token: String = UUID.randomUUID().toString()
    @Id var id: String? = null

    constructor(user: User) : this() {
        this.user = user
    }
}