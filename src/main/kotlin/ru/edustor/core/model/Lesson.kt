package ru.edustor.core.model

import com.fasterxml.jackson.annotation.JsonIgnore
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

@Document
open class Lesson() : Comparable<Lesson> {
    @Id var id: String = UUID.randomUUID().toString()
    lateinit var tagId: String
    lateinit var date: LocalDate
    lateinit var ownerId: String

    var topic: String? = null

    var pages: MutableList<Page> = mutableListOf()

    @JsonIgnore var removedOn: Instant? = null


    var removed: Boolean
        get() = removedOn != null
        set(value) {
            if (value) {
                removedOn = Instant.now()
            } else {
                removedOn = null
            }
        }

    override fun equals(other: Any?): Boolean {
        if (other !is Lesson) return false
        return id == other.id
    }

    constructor(tagId: String, date: LocalDate, ownerId: String) : this() {
        this.tagId = tagId
        this.date = date
        this.ownerId = ownerId
    }

    override fun compareTo(other: Lesson): Int {
        return date.compareTo(other.date)
    }

    override fun toString(): String {
        return "Lesson <ID: $id. Tag: $tagId. Topic: $topic. Date: ${date.format(DateTimeFormatter.ISO_LOCAL_DATE)}>"
    }

    fun toDTO(): LessonDTO {
        return LessonDTO(id, id, id, topic, date, removed, pages.map(Page::toDTO))
    }

    data class LessonDTO(
            val id: String,
            val owner: String,
            val tag: String,
            val topic: String?,
            val date: LocalDate,
            val removed: Boolean,
            val pages: List<Page.PageDTO>
    )
}