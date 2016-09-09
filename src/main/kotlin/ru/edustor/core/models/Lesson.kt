package ru.edustor.core.models

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.DBRef
import java.time.LocalDate
import java.util.*

@org.springframework.data.mongodb.core.mapping.Document(collection = "lesson")
data class Lesson(
        @Indexed @DBRef var subject: Subject? = null,
        var date: LocalDate? = null,
        var topic: String? = null,
        @DBRef var documents: MutableList<Document> = mutableListOf(),
        @Id var id: String = UUID.randomUUID().toString()
) : Comparable<Lesson> {
    override fun compareTo(other: Lesson): Int {
        return date?.compareTo(other.date) ?: 0
    }
}