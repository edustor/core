package ru.edustor.core.model.mongo

import com.fasterxml.jackson.annotation.JsonIgnore
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.DBRef
import org.springframework.data.mongodb.core.mapping.Document
import ru.edustor.core.model.Subject
import java.time.Instant
import java.util.*

@Document(collection = "subject")
class MongoSubject() : Comparable<Subject> {

    lateinit var name: String
    @Indexed @DBRef @JsonIgnore lateinit var owner: MongoUser
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

    constructor(name: String, owner: MongoUser) : this() {
        this.name = name
        this.owner = owner
    }


    override fun compareTo(other: Subject): Int {
        return name.compareTo(other.name)
    }

    override fun equals(other: Any?): Boolean {
        if (other !is Subject) return false
        return this.id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}