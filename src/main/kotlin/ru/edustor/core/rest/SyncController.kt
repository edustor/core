package ru.edustor.core.rest

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.edustor.core.exceptions.HttpRequestProcessingException
import ru.edustor.core.model.Account
import ru.edustor.core.model.Page
import ru.edustor.core.model.internal.sync.SyncTask
import ru.edustor.core.repository.LessonRepository
import ru.edustor.core.repository.SubjectRepository
import ru.edustor.core.service.FCMService
import ru.edustor.core.sync.AccountsSyncController
import ru.edustor.core.sync.LessonsSyncController
import ru.edustor.core.sync.PagesSyncController
import ru.edustor.core.sync.SubjectsSyncController

@RestController
@RequestMapping("/api/sync")
open class SyncController @Autowired constructor(
        val subjectRepo: SubjectRepository,
        val lessonRepo: LessonRepository,
        val lessonsSyncController: LessonsSyncController,
        val pagesSyncController: PagesSyncController,
        val accountsSyncController: AccountsSyncController,
        val subjectsSyncController: SubjectsSyncController,
        val mapper: ObjectMapper,
        val fcmService: FCMService
) {
    val delimiterRegex = "/".toRegex()
    val logger: Logger = LoggerFactory.getLogger(SyncController::class.java)

    @RequestMapping("/fetch")
    fun fetch(user: Account): Map<*, *> {
        val subjects = subjectRepo.findByOwner(user).filter { it.removed == false }

        val lessons = lessonRepo.findByTagIn(subjects)

        logger.debug("FETCH: Database fetch finished")


        lessons.forEach { it.pages = (it.pages.filter { it.removed == false } as MutableList<Page>) }

        logger.debug("FETCH: Data preprocessing finished")

        return mapOf(
                "user" to user,
                "tags" to subjects,
                "lessons" to lessons
        )
    }

    @RequestMapping("/push")
    fun push(@RequestBody body: String, user: Account): MutableList<Any?> {
        val tasks = mapper.readValue<List<SyncTask>>(body, object : TypeReference<List<SyncTask>>() {})
        val results = mutableListOf<Any?>()
        tasks.forEach {
            it.user = user
            try {
                val taskResult = processTask(it)
                results.add(mapOf(
                        "success" to true,
                        "result" to taskResult
                ))
            } catch (e: Exception) {
                results.add(formatException(e))
            }
        }
        if (tasks.isNotEmpty()) fcmService.sendUserSyncNotification(user)
        return results
    }

    private fun processTask(task: SyncTask): Any {

        val (group, method) = task.method.split(delimiterRegex, 2)
        val localTask = SyncTask(method, task.params, task.user)

        return when (group) {
            "lessons" -> lessonsSyncController.processTask(localTask)
            "pages" -> pagesSyncController.processTask(localTask)
            "subjects" -> subjectsSyncController.processTask(localTask)
            "account" -> accountsSyncController.processTask(localTask)
            else -> throw NoSuchMethodException("SyncController cannot resolve $group")
        }
    }

    private fun formatException(e: Exception): MutableMap<String, Any?> {
        val resp = mutableMapOf<String, Any?>()
        resp["success"] = false
        val error = mutableMapOf<String, Any?>()
        resp["error"] = error
        error["message"] = "${e.javaClass.name}. ${e.message}"

        when (e) {
            is HttpRequestProcessingException -> {
                error["status"] = e.status.value()
                error["message"] = "${e.status.reasonPhrase}: ${e.message}"
            }
            is NoSuchMethodException -> error["status"] = 405
            else -> error["status"] = 500
        }
        return resp
    }
}
