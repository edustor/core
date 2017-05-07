package ru.edustor.core.model

import com.fasterxml.jackson.annotation.JsonIgnore
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import javax.persistence.*

@Entity
open class Lesson() : Comparable<Lesson> {
    @Id var id: String = UUID.randomUUID().toString()
    lateinit var date: LocalDate

    @ManyToOne(optional = false)
    lateinit var tag: Tag

    @ManyToOne(optional = false)
    lateinit var owner: Account

    var topic: String? = null

    @OneToMany(targetEntity = Page::class, cascade = arrayOf(CascadeType.ALL),
            mappedBy = "lesson", orphanRemoval = true)
    @OrderColumn(name = "index")
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

    constructor(tag: Tag, date: LocalDate, owner: Account) : this() {
        this.tag = tag
        this.date = date
        this.owner = owner
    }

    override fun compareTo(other: Lesson): Int {
        return date.compareTo(other.date)
    }

    override fun toString(): String {
        return "Lesson <ID: $id. Tag: $tag. Topic: $topic. Date: ${date.format(DateTimeFormatter.ISO_LOCAL_DATE)}>"
    }

    fun toDTO(): LessonDTO {
        return LessonDTO(id, owner.id, tag.id, topic, date, removed, pages.mapIndexed { i, page -> page.toDTO(i) })
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