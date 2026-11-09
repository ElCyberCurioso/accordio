package com.guitarchords.app.data

import com.guitarchords.app.R
import java.text.Normalizer

/** Criterio de ordenación de la lista de canciones de una carpeta. */
enum class SongSort(val labelRes: Int) {
    /** Orden manual definido por arrastre (campo position). Valor por defecto. */
    MANUAL(R.string.sort_manual),
    TITLE(R.string.sort_title),
    ARTIST(R.string.sort_artist),
    CREATED(R.string.sort_created),
    UPDATED(R.string.sort_updated)
}

/**
 * Aplica el criterio sobre una lista que YA viene ordenada por position (orden
 * manual de la consulta). Para [SongSort.MANUAL] se devuelve sin tocar.
 * Las fechas ordenan de más reciente a más antigua; texto sin distinguir
 * mayúsculas y dejando los valores vacíos al final.
 */
fun List<Song>.applySort(sort: SongSort): List<Song> = when (sort) {
    SongSort.MANUAL -> this
    SongSort.TITLE -> sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.title })
    SongSort.ARTIST -> sortedWith(
        compareBy<Song> { it.artist.isBlank() }
            .thenBy(String.CASE_INSENSITIVE_ORDER) { it.artist }
            .thenBy(String.CASE_INSENSITIVE_ORDER) { it.title }
    )
    SongSort.CREATED -> sortedByDescending { it.createdAt }
    SongSort.UPDATED -> sortedByDescending { it.updatedAt }
}

private val MARCAS_DIACRITICAS = Regex("\\p{Mn}+")

/**
 * Texto preparado para comparar: sin tildes, en minúsculas y sin espacios en
 * los extremos. "alvaro" encuentra "Álvaro" y "ÁLVARO", como en la web; antes
 * `contains(ignoreCase)` no doblaba las tildes y la búsqueda global (SQL LIKE
 * con NOCASE) solo entendía mayúsculas sin acento.
 */
fun normalizarBusqueda(texto: String): String =
    Normalizer.normalize(texto, Normalizer.Form.NFD)
        .replace(MARCAS_DIACRITICAS, "")
        .lowercase()
        .trim()

/**
 * Filtra por una consulta de texto en título, artista y categoría, sin
 * distinguir tildes ni mayúsculas. Una consulta vacía devuelve la lista intacta.
 *
 * Es LA búsqueda de la app: la usan el buscador de dentro de una lista, el de
 * «Sin lista», el de favoritas y el global. Antes el global tenía la suya en SQL
 * y miraba además la letra, así que la misma palabra daba resultados distintos
 * según desde dónde se buscara.
 */
fun List<Song>.filterByQuery(query: String): List<Song> {
    val q = normalizarBusqueda(query)
    if (q.isEmpty()) return this
    return filter { s ->
        normalizarBusqueda(s.title).contains(q) ||
            normalizarBusqueda(s.artist).contains(q) ||
            normalizarBusqueda(s.genre).contains(q)
    }
}

/**
 * Solo las de una categoría; null es «todas». La categoría es el mismo campo
 * `genre` que se pone en la web y viaja con la sincronización, así que el filtro
 * de la app y el de la web hablan de las mismas categorías.
 */
fun List<Song>.filterByGenre(genre: String?): List<Song> {
    if (genre == null) return this
    val g = normalizarBusqueda(genre)
    return filter { normalizarBusqueda(it.genre) == g }
}

/**
 * Las categorías que hay en esta lista de partituras, una vez cada una y en
 * orden alfabético. "Pop" y "pop " cuentan como la misma: se queda la primera
 * forma que aparece.
 */
fun List<Song>.genres(): List<String> =
    mapNotNull { it.genre.trim().ifBlank { null } }
        .distinctBy { normalizarBusqueda(it) }
        .sortedWith(String.CASE_INSENSITIVE_ORDER)
