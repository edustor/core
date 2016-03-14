package ru.wutiarn.edustor.models

import com.fasterxml.jackson.annotation.JsonIgnore
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.DBRef

/**
 * Created by wutiarn on 28.02.16.
 */
data class Subject(
        var name: String? = null,
        var year: Int? = 1,
        @DBRef @JsonIgnore var owner: User? = null,
        @DBRef(lazy = true) @JsonIgnore var groups: MutableList<Group> = mutableListOf(),
        @Id var id: String? = null
)