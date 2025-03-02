package com.example.footballstatistics_app_wearos.presentation

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.health.services.client.HealthServices
import androidx.health.services.client.HealthServicesClient
import androidx.health.services.client.data.DataType
import androidx.health.services.client.data.ExerciseConfig
import androidx.health.services.client.data.ExerciseType
import androidx.health.services.client.endExercise
import androidx.health.services.client.getCapabilities
import androidx.health.services.client.startExercise
import androidx.privacysandbox.tools.core.generator.build
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class WorkoutService : Service() {

    private lateinit var healthServicesClient: HealthServicesClient
    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)
    private var exerciseClient = HealthServices.getClient(this).exerciseClient
    private var isWorkoutActive = false
    private val notificationId = 1
    private val channelId = "WorkoutServiceChannel"


    override fun onCreate() {
        super.onCreate()
        healthServicesClient = HealthServices.getClient(this)
        exerciseClient = HealthServices.getClient(this).exerciseClient
        createNotificationChannel()
    }
    @SuppressLint("ForegroundServiceType")
    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        if (isWorkoutActive) {
            exerciseClient.endExercise()
        }
        stopSelf()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(notificationId, createNotification())
        startWorkout()
        return START_STICKY
    }

    private fun startWorkout() {
        serviceScope.launch {
            try {
                val capabilities = exerciseClient.getCapabilities()
                val supportedExerciseTypes = capabilities.supportedExerciseTypes
                val exerciseType = ExerciseType.RUNNING
                if (supportedExerciseTypes.contains(exerciseType)) {
                    val dataTypes = setOf(
                        DataType.HEART_RATE_BPM,
                        DataType.STEPS_TOTAL,
                        DataType.DISTANCE_METERS,
                        DataType.CALORIES_TOTAL
                    )
                    val exerciseConfig = ExerciseConfig(
                        exerciseType = exerciseType,
                        dataTypes = dataTypes,
                        isAutoPauseAndResumeEnabled = false,
                        isGpsEnabled = true,
                        isAutoEndEnabled = false
                    )
                    exerciseClient.startExercise(exerciseConfig)
                    isWorkoutActive = true
                    exerciseClient.setExerciseUpdateListener { exerciseUpdate ->
                        Log.d("WorkoutService", "Exercise Update: $exerciseUpdate")
                        // Handle exercise updates here
                    }

                } else {
                    Log.e("WorkoutService", "Exercise type $exerciseType not supported")
                }
            } catch (e: Exception) {
                Log.e("WorkoutService", "Error starting workout", e)
            }
        }
    }

    private fun createNotification(): Notification {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Workout Service")
            .setContentText("Tracking your workout...")
            .setSmallIcon(R.drawable.soccer_icon)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                channelId,
                "Workout Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceJob.cancel()
        if (isWorkoutActive) {
            exerciseClient.endExercise()
        }
        stopSelf()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}