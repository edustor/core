package ru.edustor.core.model.internal.pdf

import ru.edustor.core.model.Account
import ru.edustor.core.model.Lesson

data class PdfUploadPreferences(
        var uploader: Account,
        var lesson: Lesson? = null
)