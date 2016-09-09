package ru.edustor.core.utils

import ru.edustor.core.models.Lesson
import ru.edustor.core.models.User

data class PdfUploadPreferences(
        var uploader: User,
        var lesson: Lesson? = null
)