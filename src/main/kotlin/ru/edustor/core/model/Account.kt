package ru.edustor.core.model

import com.fasterxml.jackson.annotation.JsonIgnore
import java.util.*
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Transient

@Entity
open class Account() {
    lateinit var email: String
    var telegramChatId: String? = null
    var telegramLinkToken: String? = null
    @Id var id: String = UUID.randomUUID().toString()

    @Transient @JsonIgnore var currentSession: Session? = null

    constructor(email: String) : this() {
        this.email = email
    }

    override fun equals(other: Any?): Boolean {
        if (other !is Account) return false
        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun toString(): String {
        return "User<$email>"
    }
}