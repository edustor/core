package ru.edustor.core

@org.springframework.boot.autoconfigure.SpringBootApplication
@org.springframework.scheduling.annotation.EnableScheduling
open class EdustorApplication : org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter() {

    companion object {
        val VERSION: String = "0.4.3.3"
    }

    @org.springframework.beans.factory.annotation.Autowired lateinit var fcmInterceptor: ru.edustor.core.interceptor.FCMInterceptor

    init {
        java.util.TimeZone.setDefault(java.util.TimeZone.getTimeZone("UTC"))
    }

    @org.springframework.beans.factory.annotation.Autowired
    fun configureMongoTemplate(mongoTemplate: org.springframework.data.mongodb.core.MongoTemplate) {
        mongoTemplate.setWriteConcern(com.mongodb.WriteConcern.ACKNOWLEDGED)
    }

    override fun addInterceptors(registry: org.springframework.web.servlet.config.annotation.InterceptorRegistry) {
        registry.addInterceptor(fcmInterceptor)
    }
}