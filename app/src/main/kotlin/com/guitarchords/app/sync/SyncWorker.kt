package com.guitarchords.app.sync

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.guitarchords.app.GuitarChordsApp
import java.util.concurrent.TimeUnit

/**
 * Sincroniza en segundo plano.
 *
 * Es la pieza que cumple el "si no hay conexión se guarda y al reconectar se
 * vuelca": WorkManager persiste la petición en disco y la ejecuta cuando haya
 * red, aunque la app esté cerrada o el móvil se haya reiniciado por medio. Los
 * cambios ya están a salvo en Room desde el momento de editarlos; esto solo se
 * encarga de que acaben saliendo.
 */
class SyncWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val app = applicationContext as? GuitarChordsApp ?: return Result.success()
        return when (val salida = app.syncEngine.sync()) {
            is SyncOutcome.Skipped -> Result.success()
            is SyncOutcome.Done -> {
                // Los acordes van por su propio camino (un blob aparte), pero se
                // aprovecha la misma ventana de red.
                runCatching { app.chordSync.sync() }
                // Si alguien editó MIENTRAS corría esta pasada, el cambio podría
                // quedarse esperando a la siguiente periódica: se pide otra
                // vuelta en lugar de darlo por hecho.
                if (app.repo.hasPendingChanges()) Result.retry() else Result.success()
            }
            is SyncOutcome.Failed -> when (salida.failure) {
                // Sin red o servidor caído: reintento con espera creciente.
                is SyncFailure.Network -> Result.retry()
                // La sesión ya no vale: reintentar no arregla nada y gastaría
                // batería para siempre. Hace falta que alguien vuelva a entrar.
                is SyncFailure.Unauthorized -> Result.failure()
                is SyncFailure.Other -> Result.retry()
            }
        }
    }

    companion object {
        private const val UNICO = "accordio-sync"
        private const val PERIODICO = "accordio-sync-periodico"

        /*
         * Los nombres de antes del renombrado. WorkManager identifica el trabajo
         * periódico POR NOMBRE y lo guarda en disco: si no se cancelan, en los
         * móviles que ya tenían la app seguiría corriendo el periódico viejo
         * ADEMÁS del nuevo, sincronizando el doble para siempre.
         */
        private const val UNICO_ANTIGUO = "vivace-sync"
        private const val PERIODICO_ANTIGUO = "vivace-sync-periodico"

        private val CON_RED = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        /**
         * Pide una sincronización cuanto antes.
         *
         * Va con [ExistingWorkPolicy.KEEP] y unos segundos de espera: editar una
         * partitura dispara varios guardados seguidos, y como la pasada lee
         * TODO lo pendiente cuando arranca, una sola cubre la ráfaga entera.
         *
         * KEEP y no REPLACE a propósito: REPLACE cancela también el trabajo que
         * esté ejecutándose, así que un guardado a destiempo abortaría la
         * sincronización en curso. El hueco que deja KEEP —editar justo después
         * de que la pasada leyera lo pendiente— lo cierra `doWork`, que pide
         * otra vuelta si al terminar sigue quedando algo.
         */
        fun schedule(context: Context, delaySeconds: Long = 5) {
            val peticion = OneTimeWorkRequestBuilder<SyncWorker>()
                .setConstraints(CON_RED)
                .setInitialDelay(delaySeconds, TimeUnit.SECONDS)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
                .build()
            WorkManager.getInstance(context)
                .enqueueUniqueWork(UNICO, ExistingWorkPolicy.KEEP, peticion)
        }

        /**
         * Repaso en segundo plano, con la app cerrada: trae lo editado en la web
         * para que esté al abrirla, aunque sea sin red en ese momento.
         *
         * Cada hora (eran 6: un cambio hecho por la mañana no aparecía hasta la
         * tarde). Con la app abierta manda la pasada de MainActivity, que es
         * mucho más frecuente.
         *
         * UPDATE y no KEEP: con KEEP, en los móviles que ya tenían programado el
         * de 6 horas seguiría ese para siempre, porque WorkManager lo guarda en
         * disco por nombre y no mira si el intervalo ha cambiado.
         */
        fun schedulePeriodic(context: Context) {
            // Antes de programar el nuestro, se retira el del nombre viejo.
            WorkManager.getInstance(context).cancelUniqueWork(PERIODICO_ANTIGUO)
            val peticion = PeriodicWorkRequestBuilder<SyncWorker>(1, TimeUnit.HOURS)
                .setConstraints(CON_RED)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 5, TimeUnit.MINUTES)
                .build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                PERIODICO, ExistingPeriodicWorkPolicy.UPDATE, peticion
            )
        }

        /** Al cerrar sesión no queda nada que subir en nombre de nadie. */
        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(UNICO)
            WorkManager.getInstance(context).cancelUniqueWork(PERIODICO)
            WorkManager.getInstance(context).cancelUniqueWork(UNICO_ANTIGUO)
            WorkManager.getInstance(context).cancelUniqueWork(PERIODICO_ANTIGUO)
        }
    }
}
