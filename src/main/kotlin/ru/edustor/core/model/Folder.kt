package ru.edustor.core.model

import com.fasterxml.jackson.annotation.JsonIgnore
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import java.time.Instant
import java.util.*
import javax.persistence.*

@Entity
class Folder() : Comparable<Folder> {

    @Column(nullable = false)
    lateinit var name: String

    @ManyToOne(optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JsonIgnore lateinit var owner: Account

    @OnDelete(action = OnDeleteAction.CASCADE)
    @ManyToOne
    lateinit var parent: Folder

    @OneToMany(mappedBy = "parent", cascade = arrayOf(CascadeType.REMOVE))
    var childFolders: List<Folder> = listOf()

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

    constructor(name: String, owner: Account) : this() {
        this.name = name
        this.owner = owner
    }


    override fun compareTo(other: Folder): Int {
        return name.compareTo(other.name)
    }

    override fun equals(other: Any?): Boolean {
        if (other !is Folder) return false
        return this.id.equals(other.id)
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}