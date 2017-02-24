package ru.edustor.core.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.util.*

@Document
open class Account() {
    @Id var id: String = UUID.randomUUID().toString()
    var fcmTokens: MutableSet<String> = mutableSetOf()
    var tags: MutableList<Tag> = mutableListOf()

    constructor(id: String) : this() {
        this.id = id
    }

    override fun equals(other: Any?): Boolean {
        if (other !is Account) return false
        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun toString(): String {
        return "Account<$id>"
    }

    fun toDTO(): AccountDTO {
        return AccountDTO(id)
    }

    data class AccountDTO(
            val id: String
    )
}