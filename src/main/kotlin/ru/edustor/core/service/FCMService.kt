package ru.edustor.core.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.mashape.unirest.http.Unirest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import ru.edustor.core.model.User
import ru.edustor.core.model.internal.sync.FCMRequest
import ru.edustor.core.repository.SessionRepository
import rx.Observable
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit
import javax.annotation.PostConstruct

@Service
open class FCMService @Autowired constructor(
        val sessionRepository: SessionRepository,
        val objectMapper: ObjectMapper
) {
    val FCM_KEY = System.getenv("FCM_KEY")

    private val queue = LinkedBlockingQueue<FCMRequest>()

    fun sendUserSyncNotification(user: User) {
        queue.add(FCMRequest(user, user.currentSession))
    }

    @PostConstruct
    private fun handle() {
        Thread(Runnable {
            while (true) {
                val fcmRequest = queue.take()
                val sessions = sessionRepository.findByUser(fcmRequest.user).filter { it.FCMToken != null }
                        .filter { it.id != fcmRequest.activeSession?.id }
                val tokens = sessions.map { it.FCMToken }

                if (tokens.isEmpty()) continue

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

                val lastResultsIndex = results.size() - 1
                for (i in 0..lastResultsIndex) {
                    if (results.get(i).has("error")) {
                        sessionRepository.delete(sessions[i])
                    }
                }
            }
        }).start()
    }
}