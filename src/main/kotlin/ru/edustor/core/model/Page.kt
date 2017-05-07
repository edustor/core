package ru.edustor.core.model

import java.time.Instant
import java.util.*
import javax.persistence.Basic
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.ManyToOne

@Entity
open class Page() {
    @Id var id: String = UUID.randomUUID().toString()
    var timestamp: Instant = Instant.now()
    var uploadedTimestamp: Instant? = null
    var fileId: String? = null
    var contentType: String? = null
    var qr: String? = null
    var fileMD5: String? = null
    var removedOn: Instant? = null

    @ManyToOne
    @Basic(optional = false)
    lateinit var lesson: Lesson
    var index: Int = 0

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

    constructor(qr: String?, timestamp: Instant, id: String) : this() {
        this.qr = qr
        this.timestamp = timestamp
        this.id = id
    }

    constructor(qr: String?) : this() {
        this.qr = qr
    }

    override fun equals(other: Any?): Boolean {
        if (other !is Page) return false
        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    fun toDTO(index: Int): PageDTO {
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