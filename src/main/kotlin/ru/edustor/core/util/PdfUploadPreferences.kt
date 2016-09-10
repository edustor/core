package ru.edustor.core.util

import ru.edustor.core.model.Lesson
import ru.edustor.core.model.User

data class PdfUploadPreferences(
        var uploader: User,
        var lesson: Lesson? = null
)