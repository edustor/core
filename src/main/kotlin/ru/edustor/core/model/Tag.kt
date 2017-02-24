package ru.edustor.core.model

import org.springframework.data.annotation.Id
import java.time.Instant
import java.util.*

open class Tag() : Comparable<Tag> {
    @Id var id: String = UUID.randomUUID().toString()
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

    val parent: String?
        get() = parents.lastOrNull()

    val parents: List<String>
        get() = path.split("/").filter(String::isNotEmpty)

    constructor(name: String, parent: Tag? = null) : this() {
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

    fun toDTO(owner: String): TagDTO {
        return TagDTO(id, owner, path, parent, name, removed)
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