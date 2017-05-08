package ru.edustor.core.model

import java.time.Instant
import java.util.*
import javax.persistence.*

@Entity
@Table(name = "tags", indexes = arrayOf(
        Index(columnList = "owner_id"),
        Index(columnList = "removedOn")
))
class Tag(
        var name: String,

        @ManyToOne(optional = false)
        var owner: Account,

        var path: String = "/", // TODO: Replace with parent reference
        var removedOn: Instant? = null,

        @OneToMany(targetEntity = Lesson::class, cascade = arrayOf(CascadeType.ALL), fetch = FetchType.LAZY,
                mappedBy = "tag", orphanRemoval = true)
        val lessons: MutableList<Lesson> = mutableListOf(),

        @Id val id: String = UUID.randomUUID().toString()
) : Comparable<Tag> {
    var removed: Boolean
        get() = removedOn != null
        set(value) {
            if (value) {
                removedOn = Instant.now()
            } else {
                removedOn = null
            }
        }

    val parent: String?
        get() = parents.lastOrNull()

    val parents: List<String>
        get() = path.split("/").filter(String::isNotEmpty)


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