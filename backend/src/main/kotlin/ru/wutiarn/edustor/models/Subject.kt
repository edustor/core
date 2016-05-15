package ru.wutiarn.edustor.models

import com.fasterxml.jackson.annotation.JsonIgnore
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.DBRef
import org.springframework.data.mongodb.core.mapping.Document

@Document
class Subject() : Comparable<Subject> {

    lateinit var name: String
    @Indexed @DBRef @JsonIgnore lateinit var owner: User
    @Id var id: String? = null

    constructor(name: String, owner: User) : this() {
        this.name = name
        this.owner = owner
    }

    override fun compareTo(other: Subject): Int {
        return name.compareTo(other.name)
    }

    override fun equals(other: Any?): Boolean {
        if (other !is Subject) return false
        if (other.id == null) return false
        return this.id?.equals(other.id!!) ?: false
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: super.hashCode()
    }
}