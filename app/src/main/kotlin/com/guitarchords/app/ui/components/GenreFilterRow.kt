package com.guitarchords.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.guitarchords.app.R

/**
 * Filtro por categoría: «Todas» y una pastilla por cada categoría presente.
 * Pulsar la elegida otra vez la quita. Las categorías son las mismas que en la
 * web (el campo `genre` de cada partitura), así que lo que se clasifica allí se
 * filtra aquí.
 *
 * Con menos de dos categorías no se pinta nada: filtrar entre una sola no
 * sirve de nada y la fila se comería alto de pantalla.
 */
@Composable
fun GenreFilterRow(
    genres: List<String>,
    selected: String?,
    onSelect: (String?) -> Unit,
    modifier: Modifier = Modifier,
    horizontalPadding: Dp = 12.dp
) {
    if (genres.size < 2 && selected == null) return
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = horizontalPadding)
    ) {
        item {
            FilterChip(
                selected = selected == null,
                onClick = { onSelect(null) },
                label = { Text(stringResource(R.string.genre_all)) }
            )
        }
        items(genres, key = { it }) { g ->
            FilterChip(
                selected = selected == g,
                onClick = { onSelect(if (selected == g) null else g) },
                label = { Text(g) }
            )
        }
    }
}
