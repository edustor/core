package ru.wutiarn.edustor.models

import com.fasterxml.jackson.annotation.JsonIgnore
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Version
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.DBRef
import java.time.LocalDate

/**
 * Created by wutiarn on 28.02.16.
 */
@org.springframework.data.mongodb.core.mapping.Document
data class Lesson(
        @Indexed @DBRef var subject: Subject? = null,
        var date: LocalDate? = null,
        var topic: String? = null,
        @DBRef(lazy = true) var documents: MutableList<Document> = mutableListOf(),
        @Id var id: String? = null,
        @Version @JsonIgnore var version: Long = 0
) : Comparable<Lesson> {
    override fun compareTo(other: Lesson): Int {
        return date?.compareTo(other.date) ?: 0
    }
}