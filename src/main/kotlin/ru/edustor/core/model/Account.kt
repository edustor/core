package ru.edustor.core.model

import java.time.Instant
import java.util.*
import javax.persistence.*

@Entity
open class Account() {
    @Id var id: String = UUID.randomUUID().toString()

    var telegramChatId: String? = null
    var telegramLinkToken: String? = null
    @Embedded var pendingUpload: PendingUploadRequest? = null

    @ElementCollection(targetClass = String::class, fetch = FetchType.EAGER)
    val fcmTokens: MutableSet<String> = mutableSetOf()

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

    @Embeddable
    class PendingUploadRequest() {
        @ManyToOne
        @JoinColumn(name = "pending_upload_lesson_id")
        lateinit var lesson: Lesson

        @Column(name = "pending_upload_valid_until")
        lateinit var validUntil: Instant

        constructor(lesson: Lesson, validUntil: Instant) : this() {
            this.lesson = lesson
            this.validUntil = validUntil
        }
    }
}