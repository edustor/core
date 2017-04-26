package ru.edustor.core.rest.internal

import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.edustor.commons.auth.annotation.RequiresAuth
import ru.edustor.core.model.Lesson
import ru.edustor.core.model.Page

@RestController
@RequestMapping("/api/internal/lessons")
class InternalLessonsController {
    @RequestMapping("{lesson}/pages/files")
    @RequiresAuth("internal")
    fun lessonPageFiles(lesson: Lesson): List<String> {
        return lesson.pages
                .filter { !it.removed && it.contentType == "application/pdf" }
                .map(Page::fileId).filterNotNull()
    }
}