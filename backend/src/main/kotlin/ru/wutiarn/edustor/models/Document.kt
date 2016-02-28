package ru.wutiarn.edustor.models

import com.mongodb.gridfs.GridFSDBFile
import org.springframework.data.mongodb.core.mapping.DBRef

/**
 * Created by wutiarn on 28.02.16.
 */
data class Document(
        @DBRef
        var fileId: GridFSDBFile? = null,
        var user: User? = null,
        var subject: Subject? = null,
        var uuid: String? = null
)