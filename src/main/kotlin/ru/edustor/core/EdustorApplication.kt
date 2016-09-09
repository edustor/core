package ru.edustor.core

@org.springframework.boot.autoconfigure.SpringBootApplication
@org.springframework.scheduling.annotation.EnableScheduling
open class EdustorApplication : org.springframework.amqp.rabbit.annotation.RabbitListenerConfigurer, org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter() {

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

    @org.springframework.context.annotation.Bean
    open fun handlerMethodFactory(): org.springframework.messaging.handler.annotation.support.DefaultMessageHandlerMethodFactory {
        val factory = org.springframework.messaging.handler.annotation.support.DefaultMessageHandlerMethodFactory()
        factory.setMessageConverter(org.springframework.messaging.converter.MappingJackson2MessageConverter())
        return factory
    }

    override fun configureRabbitListeners(registrar: org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistrar) {
        registrar.messageHandlerMethodFactory = handlerMethodFactory()
    }

    @org.springframework.beans.factory.annotation.Autowired
    fun configureRabbitTemplate(template: org.springframework.amqp.rabbit.core.RabbitTemplate) {
        template.messageConverter = org.springframework.amqp.support.converter.Jackson2JsonMessageConverter()
    }

    override fun addInterceptors(registry: org.springframework.web.servlet.config.annotation.InterceptorRegistry) {
        registry.addInterceptor(fcmInterceptor)
    }
}