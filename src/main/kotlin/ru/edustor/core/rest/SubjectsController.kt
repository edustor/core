package ru.edustor.core.rest

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import ru.edustor.core.exceptions.HttpRequestProcessingException
import ru.edustor.core.model.Account
import ru.edustor.core.model.Folder
import ru.edustor.core.model.Lesson
import ru.edustor.core.repository.LessonsRepository
import ru.edustor.core.repository.SubjectsRepository
import ru.edustor.core.util.extensions.assertHasAccess

@RestController
@RequestMapping("/api/subjects")
class SubjectsController @Autowired constructor(val subjectsRepository: SubjectsRepository, val lessonsRepo: LessonsRepository) {

    @RequestMapping("/list")
    fun listSubjects(@AuthenticationPrincipal user: Account): List<Folder> {
        val result = subjectsRepository.findByOwner(user).filter { !it.removed }
        return result.sorted()
    }

    @RequestMapping("/create")
    fun createSubject(@AuthenticationPrincipal user: Account, @RequestParam name: String): Folder {

        val subject = Folder(name, user)
        subjectsRepository.save(subject)

        return subject
    }

    @RequestMapping("/{subject}/lessons")
    fun subjectLessons(@PathVariable folder: Folder?): List<Lesson> {
        folder ?: throw HttpRequestProcessingException(HttpStatus.NOT_FOUND)
        return lessonsRepo.findByFolder(folder).filter { it.documents.isNotEmpty() && !it.removed }.sortedDescending()
    }

    @RequestMapping("/{subject}", method = arrayOf(RequestMethod.DELETE))
    fun delete(@AuthenticationPrincipal user: Account, @PathVariable folder: Folder) {
        user.assertHasAccess(folder)
        folder.removed = true
        subjectsRepository.save(folder)
    }

    @RequestMapping("/{subject}/restore")
    fun restore(@AuthenticationPrincipal user: Account, @PathVariable folder: Folder) {
        user.assertHasAccess(folder)
        folder.removed = false
        subjectsRepository.save(folder)
    }
}
