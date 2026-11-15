package com.guitarchords.app.sync

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File
import java.io.IOException
import java.io.InputStream

/**
 * Partitura en PDF adjunta a una canción.
 *
 * El fichero vive en el servidor y **se pide con la sesión**: la ruta
 * `/api/songs/<id>/pdf` aplica el mismo permiso que la partitura, así que el
 * móvil no tiene ninguna forma de llegar a un PDF que la cuenta no pueda ver.
 * Lo bajado queda en la caché privada de la app (`cacheDir/pdf`), que es lo que
 * permite abrirlo sin cobertura si ya se abrió antes.
 */
object SongPdf {
    /** El mismo tope que aplica el servidor (limits.js, MAX_PDF). */
    const val MAX_BYTES = 10 * 1024 * 1024

    private val FIRMA = byteArrayOf(0x25, 0x50, 0x44, 0x46, 0x2d)   // %PDF-

    /**
     * ¿Empieza como un PDF? La firma tiene que estar en los primeros 1024
     * bytes, igual que comprueba el servidor: se mira aquí también para no
     * subir diez megas de otra cosa y enterarse al final.
     */
    fun pareceUnPdf(bytes: ByteArray): Boolean {
        val hasta = minOf(bytes.size, 1024) - FIRMA.size
        for (i in 0..hasta) {
            var igual = true
            for (j in FIRMA.indices) {
                if (bytes[i + j] != FIRMA[j]) { igual = false; break }
            }
            if (igual) return true
        }
        return false
    }

    /**
     * Revisión que hay que guardar tras subir o quitar el PDF.
     *
     * Esa escritura sube `rev` en el servidor en uno. Si lo que devuelve es
     * justo la que se tenía más uno, nadie más tocó la partitura y se puede
     * adoptar: la siguiente subida de la ficha irá con la revisión buena. Si
     * no, hubo además un cambio ajeno que aún no se ha bajado: se conserva la
     * vieja para que la sincronización lo vea y lo resuelva como siempre.
     */
    fun revTrasEscribir(revLocal: Int, revServidor: Int): Int =
        if (revServidor == revLocal + 1) revServidor else revLocal

    /**
     * Lee como mucho [MAX_BYTES] + 1: con uno de más ya se sabe que se pasa,
     * sin cargar en memoria un fichero de un giga elegido por error.
     */
    fun leerAcotado(entrada: InputStream): ByteArray {
        val salida = java.io.ByteArrayOutputStream()
        val trozo = ByteArray(64 * 1024)
        while (salida.size() <= MAX_BYTES) {
            val n = entrada.read(trozo)
            if (n < 0) break
            salida.write(trozo, 0, n)
        }
        return salida.toByteArray()
    }

    private fun carpeta(context: Context) = File(context.cacheDir, "pdf").apply { mkdirs() }

    /** El id remoto es un UUID; aun así no se deja pasar nada que no sea de nombre de fichero. */
    private fun fichero(context: Context, remoteId: String) =
        File(carpeta(context), remoteId.filter { it.isLetterOrDigit() || it == '-' } + ".pdf")

    /**
     * Deja el PDF en la caché y devuelve el fichero.
     *
     * Siempre se intenta bajar: así un PDF sustituido en la web no se queda
     * viejo en el móvil. La copia guardada solo se usa si falla LA RED. Si el
     * servidor contesta que no hay PDF o que la sesión no vale, la copia se
     * borra y el error sube: no se sigue enseñando un fichero al que ya no se
     * tiene acceso.
     */
    suspend fun traer(context: Context, client: AccordioClient, remoteId: String): File {
        val destino = fichero(context, remoteId)
        try {
            client.downloadPdf(remoteId, destino)
        } catch (e: UnauthorizedException) {
            destino.delete(); throw e
        } catch (e: PdfNoDisponibleException) {
            destino.delete(); throw e
        } catch (e: IOException) {
            if (!destino.exists()) throw e
        }
        return destino
    }

    /** Intent para abrirlo con el visor de PDF que tenga el teléfono. */
    fun intentDeVer(context: Context, fichero: File): Intent {
        val uri = FileProvider.getUriForFile(context, context.packageName + ".fileprovider", fichero)
        return Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    /** Al quitar el PDF de una partitura, fuera también su copia. */
    fun olvidar(context: Context, remoteId: String) { fichero(context, remoteId).delete() }

    /** Al cerrar sesión: lo bajado era de esa cuenta. */
    fun vaciar(context: Context) { carpeta(context).listFiles()?.forEach { it.delete() } }
}
