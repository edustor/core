package ru.edustor.core.pdf.upload

import ru.edustor.core.model.Lesson
import java.awt.image.BufferedImage

class PdfPage(
        val pageNumber: Int,
        val uuid: String?,

        var binary: ByteArray?,
        var preview: BufferedImage?,
        var qrImages: List<BufferedImage>?,

        var lesson: Lesson? = null,

        val exception: Exception? = null
)