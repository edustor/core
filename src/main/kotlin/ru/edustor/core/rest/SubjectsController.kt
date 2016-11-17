package ru.edustor.core.rest

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import ru.edustor.core.exceptions.HttpRequestProcessingException
import ru.edustor.core.model.Account
import ru.edustor.core.model.Lesson
import ru.edustor.core.model.Subject
import ru.edustor.core.repository.LessonsRepository
import ru.edustor.core.repository.SubjectRepository
import ru.edustor.core.util.extensions.assertHasAccess

@RestController
@RequestMapping("/api/subjects")
class SubjectsController @Autowired constructor(val subjectRepository: SubjectRepository, val lessonsRepo: LessonsRepository) {

    @RequestMapping("/list")
    fun listSubjects(@AuthenticationPrincipal user: Account): List<Subject> {
        val result = subjectRepository.findByOwner(user).filter { !it.removed }
        return result.sorted()
    }

    @RequestMapping("/create")
    fun createSubject(@AuthenticationPrincipal user: Account,
                      @RequestParam name: String
    ): Subject {
        val subject = Subject(name, user)
        subjectRepository.save(subject)

        return subject
    }

    @RequestMapping("/{subject}/lessons")
    fun subjectLessons(@PathVariable subject: Subject?): List<Lesson> {
        subject ?: throw HttpRequestProcessingException(HttpStatus.NOT_FOUND)
        return lessonsRepo.findBySubject(subject).filter { it.pages.isNotEmpty() && !it.removed }.sortedDescending()
    }

    @RequestMapping("/{subject}", method = arrayOf(RequestMethod.DELETE))
    fun delete(@AuthenticationPrincipal user: Account, @PathVariable subject: Subject) {
        user.assertHasAccess(subject)
        subject.removed = true
        subjectRepository.save(subject)
    }

    @RequestMapping("/{subject}/restore")
    fun restore(@AuthenticationPrincipal user: Account, @PathVariable subject: Subject) {
        user.assertHasAccess(subject)
        subject.removed = false
        subjectRepository.save(subject)
    }
}
