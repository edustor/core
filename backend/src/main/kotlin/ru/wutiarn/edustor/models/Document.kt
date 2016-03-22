package ru.wutiarn.edustor.models

import com.fasterxml.jackson.annotation.JsonIgnore
import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.DBRef
import java.time.Instant

@org.springframework.data.mongodb.core.mapping.Document
data class Document(
        @DBRef @JsonIgnore var owner: User? = null,
        @Indexed var uuid: String? = null,
        var isUploaded: Boolean = false,
        var contentType: String? = null,
        var timestamp: Instant = Instant.now(),
        var uploadedTimestamp: Instant? = null,
        @Id var id: String = ObjectId.get().toHexString()
) {
    override fun equals(other: Any?): Boolean {
        if (other !is Document) return false
        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}