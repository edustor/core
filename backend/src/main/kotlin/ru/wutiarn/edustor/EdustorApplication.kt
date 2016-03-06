package ru.wutiarn.edustor

import org.springframework.boot.autoconfigure.SpringBootApplication
import java.util.*

/**
 * Created by wutiarn on 22.02.16.
 */
@SpringBootApplication
open class EdustorApplication {
    init {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
    }
}