package com.guitarchords.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.content.IntentCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.guitarchords.app.sync.SyncWorker
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.navigation.compose.rememberNavController
import com.guitarchords.app.data.ZipManager
import com.guitarchords.app.ui.nav.NavGraph
import com.guitarchords.app.ui.nav.Route
import com.guitarchords.app.ui.theme.GuitarChordsTheme
import com.guitarchords.app.ui.theme.ThemeController
import com.guitarchords.app.ui.theme.ThemeMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {

    private val pendingImport = MutableStateFlow<Uri?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        handleIntent(intent)
        sincronizarMientrasSeVe()

        setContent {
            val themeMode by ThemeController.mode.collectAsState()
            val dark = when (themeMode) {
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
            }
            /*
             * Iconos de las barras del sistema. La barra de ESTADO queda sobre la
             * barra superior de la app, que es el teal macizo del kit en los dos
             * temas: ahí los iconos van siempre claros, o se pierden contra el
             * teal en modo claro. La de navegación sí sigue al tema, que se apoya
             * en el fondo de la pantalla.
             */
            LaunchedEffect(dark) {
                WindowCompat.getInsetsController(window, window.decorView).apply {
                    isAppearanceLightStatusBars = false
                    isAppearanceLightNavigationBars = !dark
                }
            }
            // dynamic=false: usamos la paleta propia (acordes/texto con contraste
            // garantizado en claro y oscuro), no los colores del fondo de pantalla.
            GuitarChordsTheme(dark = dark, dynamic = false) {
                val nav = rememberNavController()
                val scope = rememberCoroutineScope()
                val pending by pendingImport.collectAsState()
                val app = applicationContext as GuitarChordsApp

                LaunchedEffect(pending) {
                    val uri = pending ?: return@LaunchedEffect
                    scope.launch {
                        val newId = withContext(Dispatchers.IO) {
                            val exp = ZipManager.importFromUri(this@MainActivity, uri)
                            exp?.let { app.repo.importPlaylist(it) }
                        }
                        pendingImport.value = null
                        if (newId != null) {
                            nav.navigate(Route.playlistDetail(newId))
                        }
                    }
                }

                /*
                 * A todo el ancho, también en tablet y en horizontal. Antes el
                 * contenido se topaba en 840 dp y se centraba, y en una tablet
                 * tumbada quedaban dos franjas vacías a los lados: justo el
                 * ancho que una partitura necesita para no partir los versos.
                 */
                Surface(modifier = Modifier.fillMaxSize()) {
                    NavGraph(nav)
                }
            }
        }
    }

    /**
     * Trae lo que haya cambiado en el servidor sin tener que pedirlo.
     *
     * Hasta ahora solo se sincronizaba al arrancar el proceso, al guardar algo y
     * al recuperar la red; lo editado en la web no llegaba hasta que alguien
     * pulsaba «Sincronizar» (o hasta el repaso periódico, horas después). Ahora
     * cada vez que la app vuelve a primer plano se pide una pasada, y mientras
     * siga a la vista, otra cada pocos minutos.
     *
     * Va por WorkManager con KEEP, como todas: si ya hay una en marcha, esta no
     * hace nada. Y la pasada solo baja lo que cambió desde el último cursor, así
     * que repetirla con la app abierta cuesta una petición pequeña.
     */
    private fun sincronizarMientrasSeVe() {
        val app = application as GuitarChordsApp
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                while (true) {
                    if (app.syncPrefs.isLoggedIn) SyncWorker.schedule(app, delaySeconds = 0)
                    delay(INTERVALO_EN_PRIMER_PLANO_MS)
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        intent ?: return
        val uri: Uri? = when (intent.action) {
            Intent.ACTION_VIEW -> intent.data
            // IntentCompat evita el getParcelableExtra deprecado en API 33+.
            Intent.ACTION_SEND -> IntentCompat.getParcelableExtra(
                intent, Intent.EXTRA_STREAM, Uri::class.java
            )
            else -> null
        }
        if (uri != null) pendingImport.value = uri
    }

    private companion object {
        const val INTERVALO_EN_PRIMER_PLANO_MS = 5 * 60 * 1000L
    }
}
