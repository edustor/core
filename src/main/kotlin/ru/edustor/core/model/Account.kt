package ru.edustor.core.model

import java.util.*
import javax.persistence.ElementCollection
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.Id

@Entity
open class Account() {
    @Id var id: String = UUID.randomUUID().toString()

    @ElementCollection(targetClass = String::class, fetch = FetchType.EAGER)
    val fcmTokens: MutableSet<String> = mutableSetOf()

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
}