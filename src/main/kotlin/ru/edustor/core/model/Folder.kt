package ru.edustor.core.model

import com.fasterxml.jackson.annotation.JsonIgnore
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import java.time.Instant
import java.util.*
import javax.persistence.*

@Entity
open class Folder() : Comparable<Folder> {

    @Column(nullable = false)
    lateinit var name: String

    @ManyToOne(optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JsonIgnore lateinit var owner: Account

    @OnDelete(action = OnDeleteAction.CASCADE)
    @ManyToOne
    var parent: Folder? = null

    @OneToMany(mappedBy = "parent", cascade = arrayOf(CascadeType.REMOVE))
    var childFolders: MutableList<Folder> = mutableListOf()

    @OneToMany(mappedBy = "folder", cascade = arrayOf(CascadeType.REMOVE))
    var lessons: MutableList<Lesson> = mutableListOf()

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

    constructor(name: String, owner: Account, parent: Folder? = null) : this() {
        this.name = name
        this.owner = owner
        this.parent = parent
    }


    override fun compareTo(other: Folder): Int {
        return name.compareTo(other.name)
    }

    override fun equals(other: Any?): Boolean {
        if (other !is Folder) return false
        return this.id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun toString(): String {
        val breadcrumbs = mutableListOf(this)

        while (breadcrumbs.lastOrNull()?.parent != null) {
            breadcrumbs.add(breadcrumbs.last().parent!!)
        }

        return breadcrumbs.reversed().map { it.name }.joinToString("/", "/")
    }
}