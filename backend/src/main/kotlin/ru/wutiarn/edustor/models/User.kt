package ru.wutiarn.edustor.models

import com.fasterxml.jackson.annotation.JsonIgnore
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document

@Document
open class User() {
    @Indexed lateinit var email: String
    @JsonIgnore var sessions: MutableList<Session> = mutableListOf()
    @Id var id: String? = null

    constructor(email: String) : this() {
        this.email = email
    }

    override fun equals(other: Any?): Boolean {
        if (other !is User) return false
        return id == other.id
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: super.hashCode()
    }
}