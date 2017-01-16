package ru.edustor.core.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.mashape.unirest.http.Unirest
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import ru.edustor.core.model.Account
import ru.edustor.core.model.internal.sync.FCMRequest
import ru.edustor.core.repository.AccountRepository
import rx.Observable
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit
import javax.annotation.PostConstruct

// TODO: Manually call fcmService from all mutating methods
@Service
open class FCMService @Autowired constructor(
        val objectMapper: ObjectMapper,
        val accountRepository: AccountRepository,
        @Value("\${edustor.core.fcm-token}") val FCM_KEY: String?
) {
    private val queue = LinkedBlockingQueue<FCMRequest>()
    private val logger = LoggerFactory.getLogger(FCMService::class.java)

    fun sendUserSyncNotification(account: Account) {
        queue.add(FCMRequest(account))
    }

    @PostConstruct
    private fun handle() {
        if (FCM_KEY == null) {
            logger.warn("FCM_KEY env is not set. Sync notifications are disabled.")
            return
        }
        Thread(Runnable {
            while (true) {
                val fcmRequest = queue.take()
                try {
                    processFCMRequest(fcmRequest)
                } catch (e: Exception) {
                    logger.warn("Exception thrown while FCM request processing", e)
                }
            }
        }).start()
    }

    private fun processFCMRequest(fcmRequest: FCMRequest) {
        val tokens = fcmRequest.account.fcmTokens.toList()

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

        if (resp.status != 200) {
            fcmRequest.retryNum += 1
            if (fcmRequest.retryNum < 3) {
                Observable.timer(10, TimeUnit.SECONDS)
                        .subscribe { queue.add(fcmRequest) }
            }
        }
        val results = respJson.get("results")

        logger.info("FCM push sent to ${fcmRequest.account.id}")

        val lastResultsIndex = results.size() - 1
        (0..lastResultsIndex)
                .filter { results.get(it).has("error") }
                .forEach { fcmRequest.account.fcmTokens.remove(tokens[it]) }

        accountRepository.save(fcmRequest.account)
    }
}
