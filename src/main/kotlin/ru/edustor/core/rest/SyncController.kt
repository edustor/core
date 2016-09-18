package ru.edustor.core.rest

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.edustor.core.exceptions.HttpRequestProcessingException
import ru.edustor.core.model.User
import ru.edustor.core.model.internal.sync.SyncTask
import ru.edustor.core.repository.LessonsRepository
import ru.edustor.core.repository.SubjectsRepository
import ru.edustor.core.service.FCMService
import ru.edustor.core.sync.AccountSyncController
import ru.edustor.core.sync.DocumentsSyncController
import ru.edustor.core.sync.LessonsSyncController
import ru.edustor.core.sync.SubjectsSyncController

@RestController
@RequestMapping("/api/sync")
class SyncController @Autowired constructor(
        val subjectRepo: SubjectsRepository,
        val lessonRepo: LessonsRepository,
        val lessonsSyncController: LessonsSyncController,
        val documentsSyncController: DocumentsSyncController,
        val accountSyncController: AccountSyncController,
        val subjectsSyncController: SubjectsSyncController,
        val mapper: ObjectMapper,
        val fcmService: FCMService
) {
    val delimiterRegex = "/".toRegex()

    @RequestMapping("/fetch")
    fun fetch(@AuthenticationPrincipal user: User): Map<*, *> {
        val subjects = subjectRepo.findByOwner(user)
        val lessons = lessonRepo.findBySubjectIn(subjects)
        return mapOf(
                "user" to user,
                "subjects" to subjects,
                "lessons" to lessons
        )
    }

    @RequestMapping("/push")
    fun push(@RequestBody body: String, @AuthenticationPrincipal user: User): MutableList<Any?> {
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

        when (group) {
            "lessons" -> return lessonsSyncController.processTask(localTask)
            "documents" -> return documentsSyncController.processTask(localTask)
            "subjects" -> return subjectsSyncController.processTask(localTask)
            "account" -> return accountSyncController.processTask(localTask)
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
