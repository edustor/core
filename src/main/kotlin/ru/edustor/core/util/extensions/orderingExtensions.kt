package ru.edustor.core.util.extensions

import ru.edustor.core.model.Document
import ru.edustor.core.model.Lesson

fun List<Document>.recalculateIndexes(lesson: Lesson) {
    var i = 0
    this.forEach {
        it.index = i++
        it.lesson = lesson
    }
}