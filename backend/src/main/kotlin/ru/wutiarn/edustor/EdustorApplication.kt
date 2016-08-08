package ru.wutiarn.edustor

import com.mongodb.WriteConcern
import org.springframework.amqp.rabbit.annotation.RabbitListenerConfigurer
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistrar
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.messaging.converter.MappingJackson2MessageConverter
import org.springframework.messaging.handler.annotation.support.DefaultMessageHandlerMethodFactory
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter
import ru.wutiarn.edustor.interceptor.FCMInterceptor
import java.util.*

@SpringBootApplication
@EnableScheduling
open class EdustorApplication : RabbitListenerConfigurer, WebMvcConfigurerAdapter() {

    companion object {
        val VERSION: String = "0.4.2.2"
    }

    @Autowired lateinit var fcmInterceptor: FCMInterceptor

    init {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
    }

    @Autowired
    fun configureMongoTemplate(mongoTemplate: MongoTemplate) {
        mongoTemplate.setWriteConcern(WriteConcern.ACKNOWLEDGED)
    }

    @Bean
    open fun handlerMethodFactory(): DefaultMessageHandlerMethodFactory {
        val factory = DefaultMessageHandlerMethodFactory()
        factory.setMessageConverter(MappingJackson2MessageConverter())
        return factory
    }

    override fun configureRabbitListeners(registrar: RabbitListenerEndpointRegistrar) {
        registrar.messageHandlerMethodFactory = handlerMethodFactory()
    }

    @Autowired
    fun configureRabbitTemplate(template: RabbitTemplate) {
        template.messageConverter = Jackson2JsonMessageConverter()
    }

    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(fcmInterceptor)
    }
}