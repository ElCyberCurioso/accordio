package com.guitarchords.app.ui.song

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.guitarchords.app.GuitarChordsApp
import com.guitarchords.app.data.Playlist
import com.guitarchords.app.data.Repository
import com.guitarchords.app.data.Song
import com.guitarchords.app.R
import com.guitarchords.app.sync.AccordioClient
import com.guitarchords.app.sync.SongPdf
import com.guitarchords.app.sync.SongTextFormat
import com.guitarchords.app.sync.SyncPrefs
import com.guitarchords.app.sync.UnauthorizedException
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SongEditorViewModel(app: Application) : AndroidViewModel(app) {
    private val repo: Repository = (app as GuitarChordsApp).repo

    private val _song = MutableStateFlow(Song(playlistId = null, title = ""))
    val song = _song.asStateFlow()

    val playlists = repo.playlists().stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList<Playlist>()
    )

    private var loaded = false

    fun load(songId: Long, playlistId: Long) {
        if (loaded) return
        loaded = true
        if (songId == 0L) {
            val pid = if (playlistId <= 0L) null else playlistId
            _song.value = Song(playlistId = pid, title = "")
            return
        }
        viewModelScope.launch {
            repo.songOnce(songId)?.let { _song.value = it }
        }
    }

    fun updateTitle(v: String) { _song.value = _song.value.copy(title = v) }
    fun updateArtist(v: String) { _song.value = _song.value.copy(artist = v) }
    fun updateGenre(v: String) { _song.value = _song.value.copy(genre = v) }
    fun updateSourceUrl(v: String) { _song.value = _song.value.copy(sourceUrl = v.trim()) }
    fun updateContent(v: String) { _song.value = _song.value.copy(content = v) }
    fun updatePlaylist(id: Long?) { _song.value = _song.value.copy(playlistId = id) }
    fun updateCapo(v: Int) { _song.value = _song.value.copy(capo = v.coerceIn(0, 12)) }

    /* ---- partitura en PDF ----
     *
     * A diferencia del resto de la ficha, el PDF no espera a «Guardar» ni a la
     * sincronización: el fichero no se guarda en el teléfono, así que se sube
     * (o se quita) en el momento, con conexión. Por eso solo se ofrece cuando
     * la partitura ya existe en la cuenta: hasta entonces no hay a qué colgarlo.
     */
    private val prefs = SyncPrefs(app)

    private val _pdfBusy = MutableStateFlow(false)
    val pdfBusy = _pdfBusy.asStateFlow()

    /** Último aviso sobre el PDF, como id de cadena; null = nada que decir. */
    private val _pdfMessage = MutableStateFlow<Int?>(null)
    val pdfMessage = _pdfMessage.asStateFlow()
    fun pdfMessageShown() { _pdfMessage.value = null }

    /** ¿Se puede adjuntar ya? Hace falta sesión y que la partitura esté subida. */
    fun canAttachPdf(song: Song): Boolean =
        song.id != 0L && song.remoteId != null && prefs.isLoggedIn

    fun attachPdf(uri: Uri) = cambiarPdf(R.string.pdf_upload_failed) { remoteId, client ->
        val bytes = withContext(Dispatchers.IO) {
            getApplication<Application>().contentResolver.openInputStream(uri)
                ?.use { SongPdf.leerAcotado(it) }
        } ?: throw IOException("no se pudo leer el fichero")
        if (bytes.size > SongPdf.MAX_BYTES) { _pdfMessage.value = R.string.pdf_too_big; return@cambiarPdf null }
        if (!SongPdf.pareceUnPdf(bytes)) { _pdfMessage.value = R.string.pdf_not_pdf; return@cambiarPdf null }
        client.uploadPdf(remoteId, bytes).also { _pdfMessage.value = R.string.pdf_uploaded }
    }

    fun removePdf() = cambiarPdf(R.string.pdf_upload_failed) { remoteId, client ->
        client.deletePdf(remoteId).also {
            SongPdf.olvidar(getApplication(), remoteId)
            _pdfMessage.value = R.string.pdf_removed
        }
    }

    private fun cambiarPdf(
        siFalla: Int,
        operacion: suspend (String, AccordioClient) -> com.guitarchords.app.sync.RemoteSong?
    ) = viewModelScope.launch {
        if (_pdfBusy.value) return@launch
        val remoteId = _song.value.remoteId ?: return@launch
        _pdfBusy.value = true
        try {
            val remota = operacion(remoteId, AccordioClient(prefs.baseUrl, prefs.authToken)) ?: return@launch
            val guardada = repo.songOnce(_song.value.id) ?: return@launch
            val rev = repo.applyPdfResult(guardada, remota)
            // La copia que edita esta pantalla también: «Guardar» escribe la
            // ficha entera, y con el dato viejo desharía lo que se acaba de hacer.
            _song.value = _song.value.copy(hasPdf = remota.hasPdf, remoteRev = rev)
        } catch (e: UnauthorizedException) {
            _pdfMessage.value = R.string.pdf_session
        } catch (e: IOException) {
            _pdfMessage.value = siFalla
        } finally {
            _pdfBusy.value = false
        }
    }

    fun save(onDone: (Long) -> Unit) = viewModelScope.launch {
        val current = _song.value.copy(
            updatedAt = System.currentTimeMillis(),
            title = _song.value.title.ifBlank { SongTextFormat.UNTITLED }
        )
        val id = repo.upsertSong(current)
        onDone(id)
    }
}
