package ru.edustor.core.model

import com.fasterxml.jackson.annotation.JsonIgnore
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Transient
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.util.*

@Document
open class User() {
    @Indexed lateinit var email: String
    @Indexed var telegramChatId: String? = null
    @Indexed var telegramLinkToken: String? = null
    @Id var id: String = UUID.randomUUID().toString()

    @Transient @JsonIgnore var currentSession: Session? = null

    constructor(email: String) : this() {
        this.email = email
    }

    override fun equals(other: Any?): Boolean {
        if (other !is User) return false
        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun toString(): String {
        return "User<$email>"
    }
}