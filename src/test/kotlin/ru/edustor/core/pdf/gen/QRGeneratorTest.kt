package ru.edustor.core.pdf.gen

import com.nhaarman.mockito_kotlin.argThat
import com.nhaarman.mockito_kotlin.spy
import com.nhaarman.mockito_kotlin.whenever
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.verify
import ru.edustor.core.pdf.qr.QRGenerator
import java.util.*

open class QRGeneratorTest {

    @Test
    fun makeQR() {

    }

    /**
     * Check that new urls are generated correctly
     */
    @Test
    fun makePageQR() {
        val qrGen = Mockito.spy(QRGenerator::class.java)
        whenever(qrGen.makePageUriQR()).thenReturn(null)

        val uuid = UUID.randomUUID().toString()
        qrGen.makePageUriQR(uuid)
        verify(qrGen).makeQR("edustor://d/$uuid")
    }

    @Test
    open fun makePageQRDefault() {
        val qrGen: QRGenerator = spy()
        qrGen.makePageUriQR()
        com.nhaarman.mockito_kotlin.verify(qrGen).makeQR(argThat {
            matches("edustor://d/[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$".toRegex())
        })
    }

    @Test
    open fun testQRSize() {
        val qrGen: QRGenerator = spy()
        val qr = qrGen.makePageUriQR()

        assertEquals(qr.width, 300)
        assertEquals(qr.height, 300)
    }
}