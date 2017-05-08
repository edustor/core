package ru.edustor.core.model

import java.util.*
import javax.persistence.*

@Entity
@Table(name = "accounts")
data class Account(
        @Id val id: String = UUID.randomUUID().toString(),

        @ElementCollection
        @CollectionTable(name = "account_fcm_tokens", indexes = arrayOf(Index(columnList = "account_id")))
        @Column(name = "fcm_token")
        @Basic(fetch = FetchType.LAZY)
        val fcmTokens: MutableSet<String> = mutableSetOf(),

        @OneToMany(targetEntity = Tag::class, mappedBy = "owner", orphanRemoval = true, cascade = arrayOf(CascadeType.ALL))
        @Basic(fetch = FetchType.LAZY)
        val tags: MutableList<Tag> = mutableListOf(),

        @OneToMany(targetEntity = Lesson::class, mappedBy = "owner", orphanRemoval = true, cascade = arrayOf(CascadeType.ALL))
        @Basic(fetch = FetchType.LAZY)
        val lessons: MutableList<Lesson> = mutableListOf()
) {
    fun toDTO(): AccountDTO {
        return AccountDTO(id)
    }

    data class AccountDTO(
            val id: String
    )
}