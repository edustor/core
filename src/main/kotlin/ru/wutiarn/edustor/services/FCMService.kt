package ru.wutiarn.edustor.services

import com.fasterxml.jackson.databind.ObjectMapper
import com.mashape.unirest.http.Unirest
import com.rabbitmq.client.Channel
import org.apache.http.HttpException
import org.springframework.amqp.core.Message
import org.springframework.amqp.rabbit.annotation.Exchange
import org.springframework.amqp.rabbit.annotation.Queue
import org.springframework.amqp.rabbit.annotation.QueueBinding
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import ru.wutiarn.edustor.models.User
import ru.wutiarn.edustor.models.util.sync.FCMRequest
import ru.wutiarn.edustor.repository.SessionRepository

@Service
open class FCMService @Autowired constructor(
        val rabbitTemplate: RabbitTemplate,
        val sessionRepository: SessionRepository,
        val objectMapper: ObjectMapper
) {
    val FCM_KEY = System.getenv("FCM_KEY")

    fun sendUserSyncNotification(user: User) {
        rabbitTemplate.convertAndSend("edustor", "fcm-sync-notifications", FCMRequest(user, user.currentSession))
    }

    @RabbitListener(bindings = arrayOf(QueueBinding(
            value = Queue("fcm-sync-notifications", durable = "true"),
            exchange = Exchange(value = "edustor", durable = "true"),
            key = "fcm-sync-notifications"
    )))
    private fun process(request: FCMRequest, message: Message, channel: Channel) {
        if (message.messageProperties.isRedelivered) {
            channel.basicNack(message.messageProperties.deliveryTag, false, false)
        }

        val sessions = sessionRepository.findByUser(request.user).filter { it.FCMToken != null }
                .filter { it.id != request.activeSession?.id }
        val tokens = sessions.map { it.FCMToken }

        if (tokens.isEmpty()) return

        val reqBody = objectMapper.writeValueAsString(mapOf(
                "data" to mapOf("command" to "sync"),
                "registration_ids" to tokens
        ))

        val req = Unirest.post("https://fcm.googleapis.com/fcm/send")
                .header("Authorization", "key=$FCM_KEY")
                .header("Content-Type", "application/json")
                .body(reqBody)

        val resp = req.asString()

        val respJson = objectMapper.readTree(resp.body)

        if (resp.status != 200) throw HttpException("Response code ${resp.status}")
        val results = respJson.get("results")

        val lastResultsIndex = results.size() - 1
        for (i in 0..lastResultsIndex) {
            if (results.get(i).has("error")) {
                sessionRepository.delete(sessions[i])
            }
        }
    }


}