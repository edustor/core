package ru.edustor.core

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.convert.threeten.Jsr310JpaConverters
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter
import java.util.*

@SpringBootApplication
@EnableScheduling
@Configuration
@EntityScan(basePackageClasses = arrayOf(EdustorApplication::class, Jsr310JpaConverters::class))
open class EdustorApplication : WebMvcConfigurerAdapter() {
    init {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
    }

//    @Bean
//    open fun logFilter(): Filter {
//        val filter = CommonsRequestLoggingFilter()
//        filter.setIncludeQueryString(true)
//        filter.setIncludeClientInfo(true)
//        filter.setIncludePayload(true)
//        filter.setMaxPayloadLength(5120)
//        return filter
//    }
}