package ru.edustor.core.model

import com.fasterxml.jackson.annotation.JsonIgnore
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import java.time.Instant
import java.util.*
import javax.persistence.CascadeType
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.OneToOne

@Entity
class Subject() : Comparable<Subject> {

    lateinit var name: String

    @OneToOne(cascade = arrayOf(CascadeType.ALL))
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JsonIgnore lateinit var owner: User

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

    constructor(name: String, owner: User) : this() {
        this.name = name
        this.owner = owner
    }


    override fun compareTo(other: Subject): Int {
        return name.compareTo(other.name)
    }

    override fun equals(other: Any?): Boolean {
        if (other !is Subject) return false
        return this.id.equals(other.id)
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}