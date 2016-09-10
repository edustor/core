package ru.edustor.core.model.internal.pdf

import ru.edustor.core.model.Lesson
import ru.edustor.core.model.User

data class PdfUploadPreferences(
        var uploader: User,
        var lesson: Lesson? = null
)