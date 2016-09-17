package ru.edustor.core.pdf.qr

import com.nhaarman.mockito_kotlin.spy
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.*

class QRReaderTest {
    /**
     * Requires installed zbar
     */
    @Test
    fun readQR() {
        val qrGen: QRGenerator = spy()
        val qrReader: QRReader = spy()

        val testData = "edustor://d/${UUID.randomUUID()}"

        val qr = qrGen.makeQR(testData)
        val readData = qrReader.readQR(qr)

        assertEquals(readData, testData)
    }
}