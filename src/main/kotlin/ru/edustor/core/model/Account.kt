package ru.edustor.core.model

import java.util.*
import javax.persistence.ElementCollection
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.Id

@Entity
open class Account() {
    lateinit var email: String
    var telegramChatId: String? = null
    var telegramLinkToken: String? = null
    @Id var id: String = UUID.randomUUID().toString()

    @ElementCollection(targetClass = String::class, fetch = FetchType.EAGER)
    val fcmTokens: MutableList<String> = mutableListOf()

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
        return "Account<$email>"
    }
}