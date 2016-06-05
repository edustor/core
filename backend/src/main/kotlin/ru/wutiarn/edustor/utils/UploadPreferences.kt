package ru.wutiarn.edustor.utils

import ru.wutiarn.edustor.models.Lesson
import ru.wutiarn.edustor.models.User

data class UploadPreferences(
        var uploader: User,
        var lesson: Lesson? = null
)