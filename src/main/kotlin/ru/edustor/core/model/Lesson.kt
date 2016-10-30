package ru.edustor.core.model

import com.fasterxml.jackson.annotation.JsonIgnore
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import java.time.Instant
import java.time.LocalDate
import java.util.*
import javax.persistence.*

@Entity
@EntityListeners()
open class Lesson() : Comparable<Lesson> {
    @ManyToOne(optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    lateinit var folder: Folder

    @Column(nullable = false)
    lateinit var date: LocalDate

    var topic: String? = null

    @OneToMany(mappedBy = "lesson", cascade = arrayOf(CascadeType.REMOVE))
    @OrderBy("index ASC")
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

    constructor(folder: Folder, date: LocalDate) : this() {
        this.folder = folder
        this.date = date
    }

    override fun compareTo(other: Lesson): Int {
        return date.compareTo(other.date)
    }

    fun recalculateDocumentsIndexes() {
        var i = 0
        documents.forEach {
            it.index = i++
            it.lesson = this
        }
    }
}