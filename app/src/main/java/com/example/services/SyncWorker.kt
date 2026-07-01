package com.example.services

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.room.Room
import com.example.data.local.AppDatabase

class SyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        // En un entorno de producción, aquí se inyectaría la DB usando Hilt.
        val db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "resq-net-db"
        ).build()

        val dao = db.resqReportDao()
        val unsynced = dao.getUnsyncedReports()

        if (unsynced.isEmpty()) {
            return Result.success()
        }

        return try {
            // Aquí simularíamos la subida de datos al servidor central de Syntropy.
            // val api = ApiAggregator.syntropyCentralApi...
            
            // Si la subida es exitosa, marcamos como sincronizados:
            val ids = unsynced.map { it.id }
            dao.markAsSynced(ids)
            
            Result.success()
        } catch (e: Exception) {
            // Reintentamos si no hay internet (Backoff policy lo manejará)
            Result.retry()
        }
    }
}
