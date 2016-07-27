package ru.wutiarn.edustor.models

import com.fasterxml.jackson.annotation.JsonIgnore
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.DBRef
import org.springframework.data.mongodb.core.mapping.Document
import java.util.*

@Document
class Subject() : Comparable<Subject> {

    lateinit var name: String
    @Indexed @DBRef @JsonIgnore lateinit var owner: User
    @Id var id: String = UUID.randomUUID().toString()

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