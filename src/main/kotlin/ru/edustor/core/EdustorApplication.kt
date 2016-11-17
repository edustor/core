package ru.edustor.core

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.web.filter.CommonsRequestLoggingFilter
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter
import ru.edustor.core.interceptor.FCMInterceptor
import java.util.*
import javax.servlet.Filter

@SpringBootApplication
@EnableScheduling
@Configuration
open class EdustorApplication : WebMvcConfigurerAdapter() {

    companion object {
        val VERSION: String = "0.5.0.2"
    }

    @org.springframework.beans.factory.annotation.Autowired lateinit var fcmInterceptor: FCMInterceptor

    init {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
    }

    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(fcmInterceptor)
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