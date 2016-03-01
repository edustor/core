package ru.wutiarn.edustor.models

import com.fasterxml.jackson.annotation.JsonIgnore
import com.mongodb.gridfs.GridFSFile
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.DBRef
import java.time.Instant

/**
 * Created by wutiarn on 28.02.16.
 */
data class Document(
        @DBRef(lazy = true) @JsonIgnore var fileId: GridFSFile? = null,
        @DBRef var owner: User? = null,
        @DBRef var lesson: Lesson? = null,
        var uuid: String? = null,
        var timestamp: Instant = Instant.now(),
        @Id var id: String? = null
)