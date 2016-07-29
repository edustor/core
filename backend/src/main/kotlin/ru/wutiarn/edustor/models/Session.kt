package ru.wutiarn.edustor.models

import com.fasterxml.jackson.annotation.JsonIgnore
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.DBRef
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant
import java.util.*

@Document
class Session() {
    @DBRef @Indexed lateinit var user: User
    @Indexed val token: String = UUID.randomUUID().toString()
    @JsonIgnore val createdAt = Instant.now()
    @Id var id: String = UUID.randomUUID().toString()

    constructor(user: User) : this() {
        this.user = user
    }
}