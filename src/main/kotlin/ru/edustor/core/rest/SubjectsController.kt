package ru.edustor.core.rest

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import ru.edustor.core.exceptions.HttpRequestProcessingException
import ru.edustor.core.model.Account
import ru.edustor.core.model.Lesson
import ru.edustor.core.model.Tag
import ru.edustor.core.repository.LessonRepository
import ru.edustor.core.repository.SubjectRepository
import ru.edustor.core.util.extensions.assertHasAccess

@RestController
@RequestMapping("/api/subjects")
class SubjectsController @Autowired constructor(val subjectRepository: SubjectRepository, val lessonRepo: LessonRepository) {

    @RequestMapping("/list")
    fun listSubjects(user: Account): List<Tag> {
        val result = subjectRepository.findByOwner(user).filter { !it.removed }
        return result.sorted()
    }

    @RequestMapping("/create")
    fun createSubject(user: Account,
                      @RequestParam name: String
    ): Tag {
        val subject = Tag(name, user)
        subjectRepository.save(subject)

        return subject
    }

    @RequestMapping("/{subject}/lessons")
    fun subjectLessons(@PathVariable tag: Tag?): List<Lesson> {
        tag ?: throw HttpRequestProcessingException(HttpStatus.NOT_FOUND)
        return lessonRepo.findByTag(tag).filter { it.pages.isNotEmpty() && !it.removed }.sortedDescending()
    }

    @RequestMapping("/{subject}", method = arrayOf(RequestMethod.DELETE))
    fun delete(user: Account, @PathVariable tag: Tag) {
        user.assertHasAccess(tag)
        tag.removed = true
        subjectRepository.save(tag)
    }

    @RequestMapping("/{subject}/restore")
    fun restore(user: Account, @PathVariable tag: Tag) {
        user.assertHasAccess(tag)
        tag.removed = false
        subjectRepository.save(tag)
    }
}
