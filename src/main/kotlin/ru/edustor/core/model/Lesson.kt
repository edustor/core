package ru.edustor.core.model

import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import javax.persistence.*

@Entity
@Table(name = "lessons", indexes = arrayOf(
        Index(columnList = "owner_id"),
        Index(columnList = "tag_id"),
        Index(columnList = "removedOn")
))
class Lesson(
        @ManyToOne(optional = false)
        var owner: Account,

        @ManyToOne(optional = false)
        var tag: Tag,

        var date: LocalDate,
        var topic: String? = null,
        var removedOn: Instant? = null,

        @OneToMany(targetEntity = Page::class, cascade = arrayOf(CascadeType.ALL),
                mappedBy = "lesson", orphanRemoval = true)
        @OrderBy("index") // @OrderColumn is not used due to it allows sparse lists
        val pages: MutableList<Page> = mutableListOf(),

        @Id val id: String = UUID.randomUUID().toString()
) : Comparable<Lesson> {
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

    override fun compareTo(other: Lesson): Int {
        return date.compareTo(other.date)
    }

    override fun toString(): String {
        return "Lesson <ID: $id. Tag: $tag. Topic: $topic. Date: ${date.format(DateTimeFormatter.ISO_LOCAL_DATE)}>"
    }

    fun toDTO(): LessonDTO {
        return LessonDTO(id, owner.id, tag.id, topic, date, removed, pages.filter { !it.removed }.map { page -> page.toDTO() })
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