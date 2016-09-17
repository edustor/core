package ru.edustor.core.pdf.upload

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import ru.edustor.core.pdf.gen.BlankPdfGenerator
import ru.edustor.core.pdf.qr.QRGenerator

class PdfProcessorTest : PdfProcessor() {

    private lateinit var pdfGenerator: BlankPdfGenerator

    @Before
    fun init() {
        pdfGenerator = BlankPdfGenerator(QRGenerator())
        val pdf = pdfGenerator.genPdf(1, BlankPdfGenerator.PdfTemplates.GRID, BlankPdfGenerator.QRLocations.values().toList())
        super.init(pdf.inputStream())
    }

    @Test
    fun testPageRenderAndQrRead() {
        val pageImg = renderPage(1)
        val readResult = readQR(pageImg)
        val uuid = readResult.first

        assertNotNull("Main QR reader failed to find qr code", uuid)
        assertTrue(uuid!!.isNotEmpty())

        for (pos in BlankPdfGenerator.QRLocations.values()) {
            val posReadResult = readQRAtLocation(pageImg, pos)
            val posUuid = posReadResult.first
            assertNotNull("Cannot find qr at ${pos.name} position", posUuid)
            assertEquals("Found different UUIDs on the same page", posUuid, uuid)
        }
    }
}