package ru.edustor.migration.model.mongo

import com.fasterxml.jackson.annotation.JsonIgnore
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.DBRef
import org.springframework.data.mongodb.core.mapping.Document
import ru.edustor.core.model.Account
import java.time.Instant
import java.util.*

@Document(collection = "session")
class MongoSession() {
    @DBRef @Indexed lateinit var user: Account
    @Indexed val token: String = UUID.randomUUID().toString()
    @JsonIgnore val createdAt = Instant.now()
    @JsonIgnore var FCMToken: String? = null
    @Id var id: String = UUID.randomUUID().toString()

    constructor(user: Account) : this() {
        this.user = user
    }
}