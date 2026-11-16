package com.guitarchords.app.sync

import com.guitarchords.app.data.Song
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SyncPlanTest {

    private fun song(
        remoteId: String? = "r1",
        remoteRev: Int = 1,
        dirty: Boolean = false,
        deletedAt: Long = 0
    ) = Song(
        id = 1,
        title = "T",
        remoteId = remoteId,
        remoteRev = remoteRev,
        dirty = dirty,
        deletedAt = deletedAt
    )

    @Test
    fun `sin copia local se importa`() {
        assertEquals(
            SyncPlan.PullAction.IMPORT,
            SyncPlan.decidePull(null, remoteRev = 3, remoteDeleted = false)
        )
    }

    @Test
    fun `cambio solo en el servidor se baja`() {
        assertEquals(
            SyncPlan.PullAction.DOWNLOAD,
            SyncPlan.decidePull(song(remoteRev = 1), remoteRev = 2, remoteDeleted = false)
        )
    }

    @Test
    fun `misma revision no toca nada`() {
        assertEquals(
            SyncPlan.PullAction.UP_TO_DATE,
            SyncPlan.decidePull(song(remoteRev = 7), remoteRev = 7, remoteDeleted = false)
        )
    }

    @Test
    fun `cambio en los dos lados es conflicto`() {
        assertEquals(
            SyncPlan.PullAction.CONFLICT,
            SyncPlan.decidePull(song(remoteRev = 1, dirty = true), remoteRev = 2, remoteDeleted = false)
        )
    }

    @Test
    fun `borrado en el servidor manda si aqui no se toco nada`() {
        assertEquals(
            SyncPlan.PullAction.DELETE_LOCAL,
            SyncPlan.decidePull(song(), remoteRev = 2, remoteDeleted = true)
        )
    }

    @Test
    fun `borrado en el servidor NO se aplica si hay cambios locales sin subir`() {
        // El usuario acaba de editarla: su edición se subirá y la resucitará.
        // Borrarla aquí sería tirar un trabajo que todavía no ha salido.
        assertEquals(
            SyncPlan.PullAction.UP_TO_DATE,
            SyncPlan.decidePull(song(dirty = true), remoteRev = 2, remoteDeleted = true)
        )
    }

    @Test
    fun `borrado en el servidor de algo que aqui ya no existe se ignora`() {
        assertEquals(
            SyncPlan.PullAction.IGNORE,
            SyncPlan.decidePull(null, remoteRev = 2, remoteDeleted = true)
        )
        assertEquals(
            SyncPlan.PullAction.IGNORE,
            SyncPlan.decidePull(song(deletedAt = 5), remoteRev = 2, remoteDeleted = true)
        )
    }

    @Test
    fun `una partitura nunca subida va sin baseRev`() {
        // 0 = "no compruebes": si no, el servidor vería un choque en cada alta.
        assertEquals(0, SyncPlan.baseRevFor(song(remoteId = null, remoteRev = 9)))
        assertEquals(4, SyncPlan.baseRevFor(song(remoteId = "r1", remoteRev = 4)))
    }

    @Test
    fun `lo que nacio y murio en el movil no se sube`() {
        assertFalse(SyncPlan.shouldPush(song(remoteId = null, deletedAt = 10)))
        assertTrue(SyncPlan.shouldPush(song(remoteId = null, deletedAt = 0)))
        // Lo que SÍ está en el servidor hay que subirlo aunque esté borrado:
        // esa es justamente la lápida que evita que resucite.
        assertTrue(SyncPlan.shouldPush(song(remoteId = "r1", deletedAt = 10)))
    }

    @Test
    fun `una app que aprende un dato nuevo recorre el feed entero una vez`() {
        // Cursor guardado por la 2.6/2.7 (época 0): se empieza desde el principio.
        assertEquals("", SyncPlan.cursorDeArranque("1731600000000:abc", epocaGuardada = 0))
        // Ya recorrido con esta época: se sigue por donde iba.
        assertEquals(
            "1731600000000:abc",
            SyncPlan.cursorDeArranque("1731600000000:abc", epocaGuardada = SyncPlan.FEED_EPOCH)
        )
        // Instalación nueva: no había cursor y sigue sin haberlo.
        assertEquals("", SyncPlan.cursorDeArranque("", epocaGuardada = 0))
    }

    @Test
    fun `volver a ver una partitura que no ha cambiado no la pisa`() {
        // Es lo que hace inofensivo el recorrido completo: misma rev = solo marcas,
        // aunque haya cambios locales sin subir.
        assertEquals(
            SyncPlan.PullAction.UP_TO_DATE,
            SyncPlan.decidePull(song(remoteId = "r1", remoteRev = 4), 4, false)
        )
        assertEquals(
            SyncPlan.PullAction.UP_TO_DATE,
            SyncPlan.decidePull(song(remoteId = "r1", remoteRev = 4).copy(dirty = true), 4, false)
        )
    }

    @Test
    fun `el nombre del conflicto lleva la fecha para no pisarse`() {
        val a = SyncPlan.conflictVersionName("25/08 14:32")
        val b = SyncPlan.conflictVersionName("25/08 14:40")
        assertTrue(a.contains("25/08 14:32"))
        assertTrue(a != b)
    }
}
