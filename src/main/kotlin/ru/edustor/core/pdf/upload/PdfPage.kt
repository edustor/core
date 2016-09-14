package ru.edustor.core.pdf.upload

import ru.edustor.core.model.Lesson
import java.awt.image.BufferedImage

class PdfPage(
        val pageNumber: Int,
        val binary: ByteArray,
        val preview: BufferedImage,
        val qrImages: List<BufferedImage>,
        val uuid: String?,

        var lesson: Lesson? = null
)