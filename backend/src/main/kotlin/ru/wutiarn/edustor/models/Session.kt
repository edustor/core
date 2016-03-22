package ru.wutiarn.edustor.models

import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.util.*

/**
 * Created by wutiarn on 28.02.16.
 */
@Document
data class Session(
        @Indexed val token: String = UUID.randomUUID().toString()
)