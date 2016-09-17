package ru.edustor.core.pdf.gen

import com.nhaarman.mockito_kotlin.argThat
import com.nhaarman.mockito_kotlin.spy
import com.nhaarman.mockito_kotlin.whenever
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.verify
import java.util.*

open class QRGenTest {

    @Test
    fun makeQR() {

    }

    /**
     * Check that new urls are generated correctly
     */
    @Test
    fun makePageQR() {
        val qrGen = Mockito.spy(QRGen::class.java)
        whenever(qrGen.makePageQR()).thenReturn(null)

        val uuid = UUID.randomUUID().toString()
        qrGen.makePageQR(uuid)
        verify(qrGen).makeQR("edustor://d/$uuid")
    }

    @Test
    open fun makePageQRDefault() {
        val qrGen: QRGen = spy()
        qrGen.makePageQR()
        com.nhaarman.mockito_kotlin.verify(qrGen).makeQR(argThat {
            matches("edustor://d/[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$".toRegex())
        })
    }

    @Test
    open fun testQRSize() {
        val qrGen: QRGen = spy()
        val qr = qrGen.makePageQR()

        assertEquals(qr.width, 300)
        assertEquals(qr.height, 300)
    }
}