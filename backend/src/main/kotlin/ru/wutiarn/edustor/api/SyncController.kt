package ru.wutiarn.edustor.api

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.wutiarn.edustor.exceptions.HttpRequestProcessingException
import ru.wutiarn.edustor.models.User
import ru.wutiarn.edustor.models.util.sync.SyncTask
import ru.wutiarn.edustor.repository.LessonsRepository
import ru.wutiarn.edustor.repository.SubjectsRepository
import ru.wutiarn.edustor.sync.DocumentsSyncController
import ru.wutiarn.edustor.sync.LessonsSyncController

@RestController
@RequestMapping("/api/sync")
class SyncController @Autowired constructor(
        val subjectRepo: SubjectsRepository,
        val lessonRepo: LessonsRepository,
        val lessonsSyncController: LessonsSyncController,
        val documentsSyncController: DocumentsSyncController,
        val mapper: ObjectMapper
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
        return results
    }

    private fun processTask(task: SyncTask): Any? {

        val (group, method) = task.method.split(delimiterRegex, 2)
        val localTask = SyncTask(method, task.params, task.user)

        when (group) {
            "lessons" -> return lessonsSyncController.processTask(localTask)
            "documents" -> return documentsSyncController.processTask(localTask)
        }
        return null
    }

    private fun formatException(e: Exception): MutableMap<String, Any?> {
        val resp = mutableMapOf<String, Any?>()
        resp["success"] = false
        val error = mutableMapOf<String, Any?>()
        resp["error"] = error
        if (e is HttpRequestProcessingException) {
            error["status"] = e.status.value()
            error["message"] = "${e.status.reasonPhrase}: ${e.message}"
        } else {
            error["status"] = 500
            error["message"] = "${e.javaClass.name}. ${e.message}"
        }

        return resp
    }
}
