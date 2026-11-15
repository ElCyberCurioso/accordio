package com.guitarchords.app.sync

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SongPdfTest {

    private fun bytes(texto: String) = texto.toByteArray(Charsets.ISO_8859_1)

    @Test
    fun `reconoce un PDF por su firma`() {
        assertTrue(SongPdf.pareceUnPdf(bytes("%PDF-1.7\n...")))
    }

    @Test
    fun `tolera basura delante de la firma, como el servidor`() {
        assertTrue(SongPdf.pareceUnPdf(bytes("\u00ef\u00bb\u00bf\n\n%PDF-1.4")))
    }

    @Test
    fun `la firma mas alla de los primeros 1024 bytes no cuenta`() {
        assertFalse(SongPdf.pareceUnPdf(bytes("x".repeat(1024) + "%PDF-1.4")))
    }

    @Test
    fun `lo que no es un PDF se rechaza`() {
        assertFalse(SongPdf.pareceUnPdf(bytes("<html><script>alert(1)</script></html>")))
        assertFalse(SongPdf.pareceUnPdf(bytes("PK\u0003\u0004 un zip con otro nombre")))
        assertFalse(SongPdf.pareceUnPdf(ByteArray(0)))
        assertFalse(SongPdf.pareceUnPdf(bytes("%PDF")))
    }

    @Test
    fun `tras subir el PDF se adopta la revision del servidor si solo ha subido uno`() {
        assertEquals(8, SongPdf.revTrasEscribir(revLocal = 7, revServidor = 8))
    }

    @Test
    fun `si el servidor va mas adelantado hubo un cambio ajeno y la revision no se toca`() {
        // Adoptarla escondería ese cambio: la siguiente subida lo pisaría sin
        // que nadie viera un conflicto.
        assertEquals(7, SongPdf.revTrasEscribir(revLocal = 7, revServidor = 10))
    }

    @Test
    fun `leer acotado corta en cuanto se pasa del tope`() {
        val enorme = java.io.ByteArrayInputStream(ByteArray(SongPdf.MAX_BYTES * 2))
        val leido = SongPdf.leerAcotado(enorme)
        assertTrue(leido.size > SongPdf.MAX_BYTES)
        assertTrue(leido.size < SongPdf.MAX_BYTES * 2)
    }

    @Test
    fun `un fichero que cabe se lee entero`() {
        val contenido = bytes("%PDF-1.4 hola")
        assertEquals(contenido.size, SongPdf.leerAcotado(contenido.inputStream()).size)
    }
}
