package ru.edustor.core.rest

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import ru.edustor.core.exceptions.HttpRequestProcessingException
import ru.edustor.core.model.Account
import ru.edustor.core.model.Lesson
import ru.edustor.core.model.Tag
import ru.edustor.core.repository.LessonRepository
import ru.edustor.core.util.extensions.assertHasAccess

@RestController
@RequestMapping("/api/tags")
class TagsController @Autowired constructor(val tagRepository: TagRepository, val lessonRepo: LessonRepository) {

    @RequestMapping("/list")
    fun listTags(user: Account): List<Tag> {
        val result = tagRepository.findByOwner(user).filter { !it.removed }
        return result.sorted()
    }

    @RequestMapping("/create")
    fun createTag(user: Account, @RequestParam name: String): Tag {
        val tag = Tag(name, user)
        tagRepository.save(tag)

        return tag
    }

    @RequestMapping("/{tag}/lessons")
    fun tagLessons(@PathVariable tag: Tag?): List<Lesson> {
        tag ?: throw HttpRequestProcessingException(HttpStatus.NOT_FOUND)
        return lessonRepo.findByTagId(tag).filter { it.pages.isNotEmpty() && !it.removed }.sortedDescending()
    }

    @RequestMapping("/{tag}", method = arrayOf(RequestMethod.DELETE))
    fun delete(user: Account, @PathVariable tag: Tag) {
        user.assertHasAccess(tag)
        tag.removed = true
        tagRepository.save(tag)
    }

    @RequestMapping("/{tag}/restore")
    fun restore(user: Account, @PathVariable tag: Tag) {
        user.assertHasAccess(tag)
        tag.removed = false
        tagRepository.save(tag)
    }
}
