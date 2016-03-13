package ru.wutiarn.edustor.utils

import ru.wutiarn.edustor.models.Lesson
import ru.wutiarn.edustor.models.User

/**
 * Created by wutiarn on 13.03.16.
 */
data class UploadPreferences(
        var lesson: Lesson? = null,
        var uploader: User? = null
)