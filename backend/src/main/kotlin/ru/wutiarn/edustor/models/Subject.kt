package ru.wutiarn.edustor.models

import com.fasterxml.jackson.annotation.JsonIgnore
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.DBRef
import org.springframework.data.mongodb.core.mapping.Document

@Document
data class Subject(
        var name: String? = null,
        var year: Int? = 1,
        @Indexed @DBRef @JsonIgnore var owner: User? = null,
        @Indexed @DBRef(lazy = true) @JsonIgnore var groups: MutableList<Group> = mutableListOf(),
        @Id var id: String? = null
) : Comparable<Subject> {
    override fun compareTo(other: Subject): Int {
        if (year != null && other.year != null) {
            val yearsComp = year?.compareTo(other.year!!)
            if (yearsComp != 0 && yearsComp != null) return yearsComp
        }
        if (name != null && other.name != null) {
            return name?.compareTo(other.name!!) ?: 0
        }
        return 0
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