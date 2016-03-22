package ru.wutiarn.edustor.models

import com.fasterxml.jackson.annotation.JsonIgnore
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.DBRef
import org.springframework.data.mongodb.core.mapping.Document

@Document
open class User(
        @Indexed var login: String? = null,
        @JsonIgnore var password: String? = null,
        @JsonIgnore var sessions: MutableList<Session> = mutableListOf(),
        @JsonIgnore @DBRef(lazy = true) var groups: MutableList<Group> = mutableListOf(),
        @JsonIgnore var timetable: MutableList<TimetableEntry> = mutableListOf(),
        @Id var id: String? = null
) {
    override fun equals(other: Any?): Boolean {
        if (other !is User) return false
        return id == other.id
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: super.hashCode()
    }
}