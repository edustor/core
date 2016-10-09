package ru.edustor.core.model.mongo

import com.fasterxml.jackson.annotation.JsonIgnore
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.DBRef
import org.springframework.data.mongodb.core.mapping.Document
import ru.edustor.core.model.Lesson
import ru.edustor.core.model.Subject
import java.time.Instant
import java.time.LocalDate
import java.util.*

@Document(collection = "lesson")
data class MongoLesson(
        @Indexed @DBRef var subject: Subject? = null,
        var date: LocalDate? = null,
        var topic: String? = null,
        @DBRef var documents: MutableList<MongoDocument> = mutableListOf(),
        @Id var id: String = UUID.randomUUID().toString()
) : Comparable<Lesson> {
    @JsonIgnore var removedOn: Instant? = null

    @JsonIgnore var removed: Boolean = false
        set(value) {
            field = value
            if (value) {
                removedOn = Instant.now()
            } else {
                removedOn = null
            }
        }

    override fun compareTo(other: Lesson): Int {
        return date?.compareTo(other.date) ?: 0
    }
}