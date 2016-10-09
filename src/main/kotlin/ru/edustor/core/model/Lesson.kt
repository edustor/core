package ru.edustor.core.model

import com.fasterxml.jackson.annotation.JsonIgnore
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import java.time.Instant
import java.time.LocalDate
import java.util.*
import javax.persistence.*

@Entity
class Lesson() : Comparable<Lesson> {
    @OneToOne(cascade = arrayOf(CascadeType.ALL))
    @OnDelete(action = OnDeleteAction.CASCADE)
    lateinit var subject: Subject

    @Column(nullable = false)
    lateinit var date: LocalDate

    var topic: String? = null

    @OneToMany(cascade = arrayOf(CascadeType.ALL))
    var documents: MutableList<Document> = mutableListOf()

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

    constructor(subject: Subject, date: LocalDate) : this() {
        this.subject = subject
        this.date = date
    }

    override fun compareTo(other: Lesson): Int {
        return date.compareTo(other.date)
    }
}