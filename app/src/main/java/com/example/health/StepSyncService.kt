package com.example.health

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.example.data.repository.FitnessDataRepository
import com.example.data.repository.SocialCompetitionRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * Android background service to sync Health Connect step count data on demand or upon intent.
 */
class StepSyncService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var syncService: HealthConnectSyncService

    override fun onCreate() {
        super.onCreate()
        val fitnessRepo = FitnessDataRepository(applicationContext)
        val competitionRepo = SocialCompetitionRepository(applicationContext)
        syncService = HealthConnectSyncService(applicationContext, fitnessRepo, competitionRepo)
        Log.i(TAG, "StepSyncService created")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action ?: ACTION_SYNC_STEPS
        Log.d(TAG, "StepSyncService onStartCommand received action: $action")

        when (action) {
            ACTION_SYNC_STEPS, ACTION_PERIODIC_SYNC -> {
                serviceScope.launch {
                    try {
                        val success = syncService.syncUserActivity()
                        Log.i(TAG, "StepSyncService execution completed. Result: $success")
                    } catch (e: Exception) {
                        Log.e(TAG, "StepSyncService execution failed: ${e.message}", e)
                    } finally {
                        stopSelf(startId)
                    }
                }
            }
            else -> {
                stopSelf(startId)
            }
        }

        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        Log.i(TAG, "StepSyncService destroyed")
    }

    companion object {
        private const val TAG = "StepSyncService"
        const val ACTION_SYNC_STEPS = "com.example.action.SYNC_STEPS"
        const val ACTION_PERIODIC_SYNC = "com.example.action.PERIODIC_SYNC"

        /**
         * Helper method to trigger the step sync service programmatically.
         */
        fun startSync(context: Context) {
            val intent = Intent(context, StepSyncService::class.java).apply {
                action = ACTION_SYNC_STEPS
            }
            try {
                context.startService(intent)
            } catch (e: Exception) {
                Log.w(TAG, "Could not start service directly: ${e.message}")
            }
        }
    }
}
