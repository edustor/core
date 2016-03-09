package ru.wutiarn.edustor.models

import com.fasterxml.jackson.annotation.JsonIgnore
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Version
import org.springframework.data.mongodb.core.mapping.DBRef
import java.time.LocalDate

/**
 * Created by wutiarn on 28.02.16.
 */
data class Lesson(
        @DBRef var subject: Subject? = null,
        var date: LocalDate? = null,
        var topic: String? = null,
        @DBRef(lazy = true) var documents: MutableList<Document> = mutableListOf(),
        @Id var id: String? = null,
        @Version @JsonIgnore var version: Long = 0
)