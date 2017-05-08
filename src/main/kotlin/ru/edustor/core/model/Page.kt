package ru.edustor.core.model

import java.time.Instant
import java.util.*
import javax.persistence.*

@Entity
@Table(name = "pages", indexes = arrayOf(
        Index(columnList = "lesson_id"),
        Index(columnList = "qr"),
        Index(columnList = "removedOn")
))
data class Page(
        @ManyToOne(optional = false)
        var lesson: Lesson,
        var index: Int = -1,
        var qr: String? = null,
        var timestamp: Instant = Instant.now(),
        var uploadedTimestamp: Instant? = null,
        var fileId: String? = null,
        var contentType: String? = null,
        var fileMD5: String? = null,
        var removedOn: Instant? = null,
        @Id val id: String = UUID.randomUUID().toString()
) {
    val isUploaded: Boolean
        get() = fileId != null

    var removed: Boolean
        get() = removedOn != null
        set(value) {
            if (value) {
                removedOn = Instant.now()
            } else {
                removedOn = null
            }
        }

    fun toDTO(): PageDTO {
        return PageDTO(id, index, timestamp, isUploaded, uploadedTimestamp, qr, contentType, fileMD5, removed)
    }

    data class PageDTO(
            val id: String,
            val index: Int, // For client deserialization convenience
            val timestamp: Instant,
            val uploaded: Boolean,
            val uploadedTimestamp: Instant?,
            val qr: String?,
            val contentType: String?,
            val fileMD5: String?,
            val removed: Boolean
    )
}