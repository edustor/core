package ru.edustor.core.model

import com.fasterxml.jackson.annotation.JsonIgnore
import org.hibernate.annotations.OnDelete
import org.springframework.data.mongodb.core.mapping.DBRef
import java.time.Instant
import java.time.LocalDate
import java.util.*
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.OneToMany
import javax.persistence.OneToOne

@Entity
data class Lesson(
        @OneToOne(cascade = arrayOf(CascadeType.ALL))
        @OnDelete(action = OnDeleteAction.CASCADE)
        var subject: Subject? = null,

        var date: LocalDate? = null,
        var topic: String? = null,

        @OneToMany(cascade = arrayOf(CascadeType.ALL))
        @DBRef var documents: MutableList<Document> = mutableListOf(),

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