package ru.edustor.core.model

import java.time.Instant
import java.util.*
import javax.persistence.*

@Entity
@Table(name = "tags", indexes = arrayOf(
        Index(columnList = "owner_id"),
        Index(columnList = "removedOn")
))
open class Tag() : Comparable<Tag> {
    @Id var id: String = UUID.randomUUID().toString()

    @ManyToOne(optional = false)
    lateinit var owner: Account

    lateinit var name: String
    var path: String = "/"
    var removedOn: Instant? = null

    var removed: Boolean
        get() = removedOn != null
        set(value) {
            if (value) {
                removedOn = Instant.now()
            } else {
                removedOn = null
            }
        }

    @OneToMany(targetEntity = Lesson::class, cascade = arrayOf(CascadeType.ALL), fetch = FetchType.LAZY,
            mappedBy = "tag", orphanRemoval = true)
    var lessons: MutableList<Lesson> = mutableListOf()

    val parent: String?
        get() = parents.lastOrNull()

    val parents: List<String>
        get() = path.split("/").filter(String::isNotEmpty)

    constructor(account: Account, name: String, parent: Tag? = null) : this() {
        this.owner = account
        this.name = name
        parent?.let { setParent(parent) }
    }

    fun setParent(parent: Tag) {
        path = "${parent.path}/${parent.id}"
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
        return TagDTO(id, owner.id, path, parent, name, removed)
    }

    data class TagDTO(
            val id: String,
            val owner: String, // Reserved for future, used by android client
            val path: String,
            val parent: String?, // Last part of path, for clients convenience
            val name: String,
            val removed: Boolean
    )
}