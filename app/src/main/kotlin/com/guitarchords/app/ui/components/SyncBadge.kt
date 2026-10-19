package com.guitarchords.app.ui.components

import com.guitarchords.app.ui.icons.AccordioIcons
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.guitarchords.app.R

/**
 * Etiqueta de estado de una partitura: sincronizada con el Worker (tiene clave
 * remota) o solo en local. [synced] = `song.remoteKey != null`.
 */
@Composable
fun SyncBadge(synced: Boolean, modifier: Modifier = Modifier) {
    if (synced) {
        Icon(
            AccordioIcons.nubeAlDia(),
            contentDescription = stringResource(R.string.badge_synced),
            tint = MaterialTheme.colorScheme.primary,
            modifier = modifier.size(18.dp)
        )
    } else {
        Icon(
            AccordioIcons.nubeSinConexion(),
            contentDescription = stringResource(R.string.badge_local_only),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = modifier.size(18.dp)
        )
    }
}
