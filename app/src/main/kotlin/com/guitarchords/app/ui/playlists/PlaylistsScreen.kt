package com.guitarchords.app.ui.playlists

import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.graphics.Color
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.clickable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.guitarchords.app.R
import com.guitarchords.app.data.Playlist
import com.guitarchords.app.ui.components.EmptyState
import com.guitarchords.app.ui.theme.accordioTopBarColors
import com.guitarchords.app.ui.icons.AccordioIcons

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistsScreen(
    onPlaylistClick: (Long) -> Unit,
    onOpenSearch: () -> Unit = {},
    onAddSong: () -> Unit = {},
    onOpenUnassigned: () -> Unit = {},
    onBack: () -> Unit = {},
    vm: PlaylistsViewModel = viewModel()
) {
    val playlists by vm.playlists.collectAsStateWithLifecycle()
    val unassignedCount by vm.unassignedCount.collectAsStateWithLifecycle()
    var showCreate by remember { mutableStateOf(false) }
    var showFabChooser by remember { mutableStateOf(false) }
    var renaming by remember { mutableStateOf<Playlist?>(null) }
    var deleting by remember { mutableStateOf<Playlist?>(null) }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) vm.import(uri) {}
    }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = accordioTopBarColors(),
                title = { Text(stringResource(R.string.playlists)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(AccordioIcons.volver(), stringResource(R.string.back), tint = Color.Unspecified)
                    }
                },
                actions = {
                    IconButton(onClick = onOpenSearch) {
                        Icon(AccordioIcons.buscar(), stringResource(R.string.search_songs), tint = Color.Unspecified)
                    }
                    IconButton(onClick = {
                        importLauncher.launch(arrayOf("application/zip", "*/*"))
                    }) {
                        Icon(AccordioIcons.importar(), stringResource(R.string.import_file), tint = Color.Unspecified)
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showFabChooser = true }) {
                Icon(AccordioIcons.mas(), stringResource(R.string.add), tint = Color.Unspecified)
            }
        }
    ) { pv ->
        if (playlists.isEmpty() && unassignedCount == 0) {
            EmptyState(
                icon = AccordioIcons.partitura(),
                title = stringResource(R.string.empty_playlists_title),
                subtitle = stringResource(R.string.empty_playlists_subtitle),
                actionLabel = stringResource(R.string.create_playlist),
                onAction = { showCreate = true },
                modifier = Modifier.padding(pv)
            )
        } else {
            LazyColumn(
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(pv)
            ) {
                if (unassignedCount > 0) {
                    item(key = "unassigned") {
                        UnassignedRow(count = unassignedCount, onClick = onOpenUnassigned)
                    }
                }
                items(playlists, key = { it.id }) { pl ->
                    PlaylistRow(
                        playlist = pl,
                        onClick = { onPlaylistClick(pl.id) },
                        onRename = { renaming = pl },
                        onDelete = { deleting = pl }
                    )
                }
            }
        }
    }

    if (showFabChooser) {
        AlertDialog(
            onDismissRequest = { showFabChooser = false },
            title = { Text(stringResource(R.string.fab_chooser_title)) },
            text = {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(role = Role.Button) {
                                showFabChooser = false
                                showCreate = true
                            }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(AccordioIcons.partitura(), null, tint = Color.Unspecified)
                        Spacer(Modifier.size(12.dp))
                        Text(stringResource(R.string.new_playlist), style = MaterialTheme.typography.bodyLarge)
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(role = Role.Button) {
                                showFabChooser = false
                                onAddSong()
                            }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(AccordioIcons.notas(), null, tint = Color.Unspecified)
                        Spacer(Modifier.size(12.dp))
                        Text(stringResource(R.string.new_song), style = MaterialTheme.typography.bodyLarge)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showFabChooser = false }) { Text(stringResource(R.string.cancel)) }
            }
        )
    }

    if (showCreate) {
        TextDialog(
            title = stringResource(R.string.new_playlist),
            initial = "",
            onDismiss = { showCreate = false },
            onConfirm = {
                vm.create(it)
                showCreate = false
            }
        )
    }
    renaming?.let { p ->
        TextDialog(
            title = stringResource(R.string.rename),
            initial = p.name,
            onDismiss = { renaming = null },
            onConfirm = {
                vm.rename(p, it)
                renaming = null
            }
        )
    }
    deleting?.let { p ->
        AlertDialog(
            onDismissRequest = { deleting = null },
            title = { Text(stringResource(R.string.delete_playlist_title)) },
            text = { Text(stringResource(R.string.delete_playlist_msg, p.name)) },
            confirmButton = {
                TextButton(onClick = {
                    vm.delete(p.id)
                    deleting = null
                }) { Text(stringResource(R.string.delete)) }
            },
            dismissButton = {
                TextButton(onClick = { deleting = null }) { Text(stringResource(R.string.cancel)) }
            }
        )
    }
}

@Composable
private fun UnassignedRow(count: Int, onClick: () -> Unit) {
    Card(
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Icon(AccordioIcons.notas(), null, tint = Color.Unspecified)
            Spacer(Modifier.size(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    stringResource(R.string.unassigned_songs),
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    pluralStringResource(R.plurals.song_count, count, count),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun PlaylistRow(
    playlist: Playlist,
    onClick: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit
) {
    val haptics = LocalHapticFeedback.current
    Card(
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onClick() },
                    onLongPress = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        onRename()
                    }
                )
            }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Icon(AccordioIcons.partitura(), null, tint = Color.Unspecified)
            Spacer(Modifier.size(12.dp))
            Text(
                playlist.name,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onRename) { Icon(AccordioIcons.editar(), stringResource(R.string.rename), tint = Color.Unspecified) }
            IconButton(onClick = onDelete) { Icon(AccordioIcons.borrar(), stringResource(R.string.delete), tint = Color.Unspecified) }
        }
    }
}

@Composable
fun TextDialog(
    title: String,
    initial: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var value by remember { mutableStateOf(initial) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = value,
                onValueChange = { value = it },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(value) }) { Text(stringResource(R.string.save)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        }
    )
}
