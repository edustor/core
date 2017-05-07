package ru.edustor.core.util.extensions

import ru.edustor.core.model.Page

fun MutableList<Page>.setIndexes() {
    this.mapIndexed { i, it ->
        it.index = i
    }
}