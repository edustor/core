package ru.wutiarn.edustor.models

import com.fasterxml.jackson.annotation.JsonIgnore
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.DBRef
import java.time.Instant
import java.util.*

@org.springframework.data.mongodb.core.mapping.Document
class Document(
        owner: User? = null,
        @Indexed var uuid: String? = null,
        var isUploaded: Boolean = false,
        var contentType: String? = null,
        var timestamp: Instant = Instant.now(),
        var uploadedTimestamp: Instant? = null,
        @Id var id: String = UUID.randomUUID().toString()
) {
    @DBRef @JsonIgnore lateinit var owner: User

    init {
        owner?.let {
            this.owner = it
        }
    }

    override fun equals(other: Any?): Boolean {
        if (other !is Document) return false
        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}