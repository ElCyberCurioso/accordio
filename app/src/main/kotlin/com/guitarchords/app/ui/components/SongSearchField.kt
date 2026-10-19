package com.guitarchords.app.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.guitarchords.app.R
import com.guitarchords.app.ui.icons.AccordioIcons

/** Campo de búsqueda dentro de una carpeta: filtra por título/artista/género. */
@Composable
fun SongSearchField(
    query: String,
    onQuery: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQuery,
        modifier = modifier.fillMaxWidth(),
        singleLine = true,
        placeholder = { Text(stringResource(R.string.search_songs)) },
        leadingIcon = { Icon(AccordioIcons.buscar(), null, tint = Color.Unspecified) },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQuery("") }) {
                    Icon(AccordioIcons.cerrar(), stringResource(R.string.clear), tint = Color.Unspecified)
                }
            }
        }
    )
}
