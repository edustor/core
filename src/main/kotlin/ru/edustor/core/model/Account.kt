package ru.edustor.core.model

import java.util.*
import javax.persistence.*

@Entity
open class Account() {
    @Id var id: String = UUID.randomUUID().toString()

    @ElementCollection
    @CollectionTable(name = "account_fcm_tokens")
    @Column(name = "fcm_token")
    @Basic(fetch = FetchType.LAZY)
    var fcmTokens: MutableSet<String> = mutableSetOf()

    @OneToMany(targetEntity = Tag::class, mappedBy = "account", orphanRemoval = true, cascade = arrayOf(CascadeType.ALL))
    @Basic(fetch = FetchType.LAZY)
    var tags: MutableList<Tag> = mutableListOf()

    @OneToMany(targetEntity = Lesson::class, mappedBy = "owner", orphanRemoval = true, cascade = arrayOf(CascadeType.ALL))
    @Basic(fetch = FetchType.LAZY)
    var lessons: MutableList<Lesson> = mutableListOf()

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
        return "AccountProfile<$id>"
    }

    fun toDTO(): AccountDTO {
        return AccountDTO(id)
    }

    data class AccountDTO(
            val id: String
    )
}