package ru.edustor.core

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.web.filter.CommonsRequestLoggingFilter
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter
import ru.edustor.commons.api.RetrofitConfiguration
import java.util.*
import javax.servlet.Filter

@SpringBootApplication
@EnableScheduling
@Configuration
@Import(RetrofitConfiguration::class)
open class EdustorApplication : WebMvcConfigurerAdapter() {
    init {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
    }

    @Bean
    open fun logFilter(): Filter {
        val filter = CommonsRequestLoggingFilter()
        filter.setIncludeQueryString(true)
        filter.setIncludeClientInfo(true)
        filter.setIncludePayload(true)
        filter.setMaxPayloadLength(5120)
        return filter
    }
}