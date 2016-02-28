package ru.wutiarn.edustor.models

import com.mongodb.gridfs.GridFSDBFile
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.DBRef
import java.time.Instant

/**
 * Created by wutiarn on 28.02.16.
 */
data class Document(
        @DBRef
        var fileId: GridFSDBFile? = null,
        var user: User? = null,
        var subject: Subject? = null,
        var uuid: String? = null,
        var timestamp: Instant = Instant.now(),
        @Id var id: String? = null
)