package ru.edustor.core.model

import com.fasterxml.jackson.annotation.JsonIgnore
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import java.time.Instant
import java.util.*
import javax.persistence.*

@Entity
open class Tag() : Comparable<Tag> {

    @Id var id: String = UUID.randomUUID().toString()

    @Column(nullable = false)
    lateinit var name: String

    @ManyToOne(optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JsonIgnore lateinit var owner: Account

    @ManyToOne(optional = true)
    @OnDelete(action = OnDeleteAction.CASCADE)
    var parent: Tag? = null

    @JsonIgnore var removedOn: Instant? = null

    @JsonIgnore
    @OneToMany(mappedBy = "tag", cascade = arrayOf(CascadeType.REMOVE), fetch = FetchType.LAZY)
    var lessons: MutableList<Lesson> = mutableListOf()

    @JsonIgnore
    @OneToMany(mappedBy = "parent", cascade = arrayOf(CascadeType.REMOVE), fetch = FetchType.LAZY)
    var leaves: MutableList<Tag> = mutableListOf()


    @JsonIgnore var removed: Boolean = false
        set(value) {
            field = value
            if (value) {
                removedOn = Instant.now()
            } else {
                removedOn = null
            }
        }


    constructor(name: String, owner: Account) : this() {
        this.name = name
        this.owner = owner
    }


    override fun compareTo(other: Tag): Int {
        return name.compareTo(other.name)
    }

    override fun equals(other: Any?): Boolean {
        if (other !is Tag) return false
        return this.id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun toString(): String {
        return "[$name]"
    }

    fun toDTO(): TagDTO {
        return TagDTO(id, owner.id, parent?.id, name, removed)
    }

    data class TagDTO(
            val id: String,
            val owner: String,
            val parent: String?,
            val name: String,
            val removed: Boolean
    )
}