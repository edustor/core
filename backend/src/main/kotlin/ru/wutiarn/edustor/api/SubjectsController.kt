package ru.wutiarn.edustor.api

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import ru.wutiarn.edustor.exceptions.HttpRequestProcessingException
import ru.wutiarn.edustor.models.Lesson
import ru.wutiarn.edustor.models.Subject
import ru.wutiarn.edustor.models.User
import ru.wutiarn.edustor.repository.LessonsRepository
import ru.wutiarn.edustor.repository.SubjectsRepository

@RestController
@RequestMapping("/api/subjects")
class SubjectsController @Autowired constructor(val repo: SubjectsRepository,
                                                val lessonsRepository: LessonsRepository,
                                                val subjectsRepository: SubjectsRepository) {

    @RequestMapping("/list")
    fun listSubjects(@AuthenticationPrincipal user: User): List<Subject> {
        val result = subjectsRepository.findByOwner(user)
        return result.sorted()
    }

    @RequestMapping("/{subject}/lessons")
    fun getLessons(@PathVariable subject: Subject?, @RequestParam(required = false, defaultValue = "0") page: Int): List<Lesson> {
        subject ?: throw HttpRequestProcessingException(HttpStatus.NOT_FOUND)
        return lessonsRepository.findBySubject(subject, PageRequest(page, 30)).filter { it.documents.isNotEmpty() }.sortedDescending()
    }

    @RequestMapping("/create")
    fun createSubject(@AuthenticationPrincipal user: User, @RequestParam name: String, @RequestParam year: Int): Subject {

        val subject = Subject(name, year, user)
        repo.save(subject)

        return subject

    }
}
