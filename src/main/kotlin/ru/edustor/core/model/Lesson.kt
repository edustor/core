package ru.edustor.core.model

import com.fasterxml.jackson.annotation.JsonIgnore
import org.hibernate.annotations.Fetch
import org.hibernate.annotations.FetchMode
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import javax.persistence.*

@Entity
open class Lesson() : Comparable<Lesson> {
    @Id var id: String = UUID.randomUUID().toString()

    @ManyToOne(optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    lateinit var tag: Tag

    @Column(nullable = false)
    lateinit var date: LocalDate

    @ManyToOne(optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JsonIgnore lateinit var owner: Account

    var topic: String? = null

    @OrderBy("index ASC")
    @OneToMany(mappedBy = "lesson", cascade = arrayOf(CascadeType.REMOVE), fetch = FetchType.EAGER)
    @Fetch(FetchMode.SUBSELECT)
    var pages: MutableList<Page> = mutableListOf()

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

    constructor(tag: Tag, date: LocalDate, owner: Account) : this() {
        this.tag = tag
        this.date = date
        this.owner = owner
    }

    override fun compareTo(other: Lesson): Int {
        return date.compareTo(other.date)
    }

    override fun toString(): String {
        return "$tag ${topic ?: "No topic"} on ${date.format(DateTimeFormatter.ISO_LOCAL_DATE)}"
    }

    fun toDTO(): LessonDTO {
        return LessonDTO(id, tag.id, topic, date, removed, pages.map(Page::toDTO))
    }

    data class LessonDTO(
            val id: String,
            val tag: String,
            val topic: String?,
            val date: LocalDate,
            val removed: Boolean,
            val pages: List<Page.PageDTO>
    )
}