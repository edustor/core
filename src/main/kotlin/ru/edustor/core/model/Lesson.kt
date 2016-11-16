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
    @ManyToOne(optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JsonIgnore
    lateinit var folder: Folder

    @ManyToOne(optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JsonIgnore lateinit var owner: Account

    @Column(nullable = false)
    lateinit var date: LocalDate

    var topic: String? = null

    @OrderBy("index ASC")
    @OneToMany(mappedBy = "lesson", cascade = arrayOf(CascadeType.REMOVE), fetch = FetchType.EAGER)
    @Fetch(FetchMode.SUBSELECT)
    var pages: MutableList<Page> = mutableListOf()

    @Id var id: String = UUID.randomUUID().toString()

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

    constructor(folder: Folder, date: LocalDate) : this() {
        this.folder = folder
        this.date = date
    }

    override fun compareTo(other: Lesson): Int {
        return date.compareTo(other.date)
    }

    override fun toString(): String {
        return "$folder ${topic ?: "No topic"} on ${date.format(DateTimeFormatter.ISO_LOCAL_DATE)}"
    }
}