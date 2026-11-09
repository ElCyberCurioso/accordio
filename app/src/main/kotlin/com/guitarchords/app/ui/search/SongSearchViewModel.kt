package com.guitarchords.app.ui.search

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.guitarchords.app.GuitarChordsApp
import com.guitarchords.app.data.Playlist
import com.guitarchords.app.data.Repository
import com.guitarchords.app.data.Song
import com.guitarchords.app.data.SongSort
import com.guitarchords.app.data.SongSortPrefs
import com.guitarchords.app.data.applySort
import com.guitarchords.app.data.filterByGenre
import com.guitarchords.app.data.filterByQuery
import com.guitarchords.app.data.genres
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SongHit(val song: Song, val playlistName: String)

@OptIn(ExperimentalCoroutinesApi::class, kotlinx.coroutines.FlowPreview::class)
class SongSearchViewModel(app: Application) : AndroidViewModel(app) {
    private val repo: Repository = (app as GuitarChordsApp).repo

    private val _query = MutableStateFlow("")
    val query = _query

    private val playlistsFlow = repo.playlists()

    private val sortPrefs = SongSortPrefs(app)
    private val sortFlow = MutableStateFlow(sortPrefs.sort)
    val sort = sortFlow.asStateFlow()

    private val genreFlow = MutableStateFlow<String?>(null)
    val genre = genreFlow.asStateFlow()

    /** Todas las partituras vivas: las de cada lista y las de «Sin lista». */
    private val allSongs = repo.allSongs()

    /** Las categorías de todo el cancionero, para el filtro. */
    val genres = allSongs.map { it.genres() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /*
     * La misma búsqueda que dentro de una lista (filterByQuery: título, artista y
     * categoría, sin tildes ni mayúsculas) pero sobre TODAS las listas. Antes
     * esta pantalla buscaba en SQL y además en la letra, y daba otros
     * resultados que el buscador de dentro de una lista.
     *
     * Con una categoría elegida y sin texto se listan todas las de esa
     * categoría: es la forma de ver «todo lo de Rumba» sin tener que escribir.
     */
    val results = combine(
        _query.debounce(150), genreFlow, allSongs, playlistsFlow, sortFlow
    ) { q, g, songs, playlists, sortMode ->
        if (q.isBlank() && g == null) emptyList()
        else {
            val byId: Map<Long, Playlist> = playlists.associateBy { it.id }
            songs.filterByGenre(g).filterByQuery(q).applySort(sortMode)
                .map { SongHit(it, it.playlistId?.let { pid -> byId[pid]?.name } ?: "") }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun setGenre(g: String?) { genreFlow.value = g }

    fun setSort(sortMode: SongSort) {
        sortFlow.value = sortMode
        sortPrefs.sort = sortMode
    }

    val playlists = playlistsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun setQuery(v: String) { _query.value = v }

    fun deleteSelected(ids: Set<Long>) = viewModelScope.launch { repo.deleteSongs(ids) }

    /** Deshacer el envío a la papelera (Snackbar "Deshacer"). */
    fun undoTrash(ids: Collection<Long>) = viewModelScope.launch { repo.restoreFromTrash(ids) }
    fun moveSelected(ids: Set<Long>, target: Long?) = viewModelScope.launch { repo.moveSongs(ids, target) }
    fun favoriteSelected(ids: Set<Long>, fav: Boolean) = viewModelScope.launch { repo.setFavoriteFor(ids, fav) }
}
