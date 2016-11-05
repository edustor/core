package ru.edustor.core.util.extensions

import ru.edustor.core.model.Lesson
import ru.edustor.core.model.Page

fun List<Page>.recalculateIndexes(lesson: Lesson) {
    var i = 0
    this.forEach {
        it.index = i++
        it.lesson = lesson
    }
}