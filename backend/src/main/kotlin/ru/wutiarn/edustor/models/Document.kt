package ru.wutiarn.edustor.models

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.DBRef
import java.time.Instant

/**
 * Created by wutiarn on 28.02.16.
 */
data class Document(
        @DBRef var owner: User? = null,
        var uuid: String? = null,
        var isUploaded: Boolean = false,
        var contentType: String? = null,
        var timestamp: Instant = Instant.now(),
        @Id var id: String = ObjectId.get().toHexString()
) : Comparable<Document> {
    override fun compareTo(other: Document): Int {
        return timestamp.compareTo(other.timestamp)
    }
}