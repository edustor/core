package ru.edustor.core.telegram.commands

import com.pengrad.telegrambot.model.Message
import com.pengrad.telegrambot.request.AbstractSendRequest
import com.pengrad.telegrambot.request.SendMessage
import org.springframework.stereotype.Component
import ru.edustor.core.model.Account
import ru.edustor.core.repository.AccountRepository
import ru.edustor.core.repository.LessonsRepository
import ru.edustor.core.telegram.TelegramEventsRouter
import ru.edustor.core.telegram.TelegramHandler
import ru.edustor.core.util.extensions.cid
import ru.edustor.core.util.extensions.replyText
import java.time.Instant
import java.time.temporal.ChronoUnit

@Component
open class NextUploadCommandHandler(telegramEventsRouter: TelegramEventsRouter,
                                    val userRepository: AccountRepository,
                                    val lessonsRepository: LessonsRepository) : TelegramHandler {

    companion object {
        val uuidRegex = "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}".toRegex()
    }

    init {
        telegramEventsRouter.registerCommand("nu", this)
    }

    override fun process(msg: Message): AbstractSendRequest<SendMessage>? {
        val user = userRepository.findByTelegramChatId(msg.cid()) ?: return msg.replyText("You're not logged in")

        val arg = msg.text().split(" ").getOrNull(1) ?: let {
            user.pendingUpload = null
            userRepository.save(user)
            return msg.replyText("Pending upload request has been cleared")
        }
        val lessonId = uuidRegex.find(arg)?.value ?: return msg.replyText("Invalid URL/UUID")
        val lesson = lessonsRepository.findOne(lessonId) ?: return msg.replyText("Unknown lesson")

        user.pendingUpload = Account.PendingUploadRequest(lesson, Instant.now().plus(10, ChronoUnit.MINUTES))
        userRepository.save(user)

        return msg.replyText("Done. First uploaded file within next 10 minutes will be saved to $lesson")
    }
}