package ru.wutiarn.edustor.api

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.wutiarn.edustor.models.User
import ru.wutiarn.edustor.models.util.sync.SyncTask
import ru.wutiarn.edustor.repository.LessonsRepository
import ru.wutiarn.edustor.repository.SubjectsRepository
import ru.wutiarn.edustor.sync.LessonsSyncController

@RestController
@RequestMapping("/api/sync")
class SyncController @Autowired constructor(
        val subjectRepo: SubjectsRepository,
        val lessonRepo: LessonsRepository,
        val lessonsSyncController: LessonsSyncController
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
    fun push(@RequestBody tasks: List<SyncTask>): MutableList<Any?> {
        val results = mutableListOf<Any?>()
        tasks.forEach {
            val taskResult = processTask(it)
            results.add(taskResult)
        }
        return results
    }

    private fun processTask(task: SyncTask): Any? {

        val (group, method) = task.method.split(delimiterRegex, 2)
        val localTask = SyncTask(method, task.params)

        when (group) {
            "lessons" -> return lessonsSyncController.processTask(localTask)
        }
        return null
    }
}
