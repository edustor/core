package ru.edustor.core.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.hibernate5.Hibernate5Module
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration

@Configuration
open class JacksonConfig {
    @Autowired
    fun configureHibernateIntegration(objectMapper: ObjectMapper) {
        val hibernate5Module = Hibernate5Module()
        hibernate5Module.enable(Hibernate5Module.Feature.FORCE_LAZY_LOADING)
        objectMapper.registerModule(hibernate5Module)
    }
}