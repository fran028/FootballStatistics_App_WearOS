package com.example.footballstatistics_app_wearos.presentation

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.health.services.client.ExerciseClient
import androidx.health.services.client.ExerciseUpdateCallback
import androidx.health.services.client.HealthServices
import androidx.health.services.client.clearUpdateCallback
import androidx.health.services.client.data.Availability
import androidx.health.services.client.data.BatchingMode
import androidx.health.services.client.data.DataType
import androidx.health.services.client.data.ExerciseConfig
import androidx.health.services.client.data.ExerciseLapSummary
import androidx.health.services.client.data.ExerciseType
import androidx.health.services.client.data.ExerciseUpdate
import androidx.health.services.client.data.LocationAvailability
import androidx.health.services.client.data.LocationData
import androidx.health.services.client.data.SampleDataPoint
import androidx.health.services.client.endExercise
import androidx.health.services.client.getCapabilities
import androidx.health.services.client.pauseExercise
import androidx.health.services.client.resumeExercise
import androidx.health.services.client.startExercise
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import com.example.footballstatistics_app_wearos.R
import com.example.footballstatistics_app_wearos.presentation.data.AppDatabase
import com.example.footballstatistics_app_wearos.presentation.data.LocationDataEntity
import kotlinx.coroutines.launch
import java.time.Duration

class MyExerciseService : LifecycleService() {
    val TAG = "MyExerciseService"

    private lateinit var exerciseClient: ExerciseClient
    private lateinit var database: AppDatabase
    private var exerciseStarted = false
    private var isExercisePaused = false

    companion object {
        const val ACTION_PAUSE = "com.example.footballstatistics_app_wearos.ACTION_PAUSE"
        const val ACTION_RESUME = "com.example.footballstatistics_app_wearos.ACTION_RESUME"
    }

    private val binder = LocalBinder()

    inner class LocalBinder : Binder() {
        fun getService(): MyExerciseService = this@MyExerciseService
    }

    override fun onBind(intent: Intent): IBinder {
        super.onBind(intent)
        return binder
    }

    private val exerciseUpdateCallback = object : ExerciseUpdateCallback {
        override fun onExerciseUpdateReceived(update: ExerciseUpdate) {
            val locationDataPoints = update.latestMetrics.getData(DataType.LOCATION)
            if (locationDataPoints.isNotEmpty()) {
                val lastDataPoint = locationDataPoints.last()
                Log.d(
                    TAG,
                    "Received ${locationDataPoints.size} location points. Last one at: ${lastDataPoint.value.latitude}, ${lastDataPoint.value.longitude}"
                )
                handleLocationBatch(locationDataPoints)
            }
        }

        override fun onLapSummaryReceived(lapSummary: ExerciseLapSummary) {
            Log.d(TAG, "Lap Summary: $lapSummary")
        }

        override fun onRegistered() {
            Log.d(TAG, "Registered")
        }

        override fun onRegistrationFailed(throwable: Throwable) {
            Log.e(TAG, "Registration Failed", throwable)
        }

        override fun onAvailabilityChanged(dataType: DataType<*, *>, availability: Availability) {
            if (dataType == DataType.LOCATION) {
                val locationAvailability = availability as LocationAvailability
                Log.d(TAG, "Location Availability: $locationAvailability")
            }
        }
    }

    override fun onCreate() {
        Log.d(TAG, "Service Created")
        super.onCreate()
        exerciseClient = HealthServices.getClient(this).exerciseClient
        database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "app_database"
        ).build()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        Log.d(TAG, "Service Started (onStartCommand())")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                1,
                createNotification(),
                ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
            )
        } else {
            startForeground(1, createNotification())
        }

        if (!exerciseStarted) {
            Log.d(TAG, "Starting exercise...")
            startExercise()
            exerciseStarted = true
        }
        return START_STICKY
    }

    private fun startExercise() {
        Log.d(TAG, "Starting exercise... (startExercise())")
        lifecycleScope.launch {
            try {
                val capabilities = exerciseClient.getCapabilities()
                if (ExerciseType.RUNNING in capabilities.supportedExerciseTypes) {
                    val dataTypes = setOf(DataType.LOCATION)

                    // DEFINITIVE FIX: Use the static getRate method to define a sampling rate.
                    // 2Hz = 500ms interval.
                    val batchingModeOverrides = setOf(
                        BatchingMode.getRate(
                            DataType.LOCATION,
                            Duration.ofMillis(500)
                        )
                    )

                    // Use the builder pattern for ExerciseConfig and add the batching override.
                    val config = ExerciseConfig.Builder(ExerciseType.RUNNING)
                        .setDataTypes(dataTypes)
                        .setBatchingModeOverrides(batchingModeOverrides)
                        .setIsAutoPauseAndResumeEnabled(false)
                        .setIsGpsEnabled(true)
                        .build()

                    // Set the callback BEFORE starting the exercise.
                    exerciseClient.setUpdateCallback(exerciseUpdateCallback)
                    exerciseClient.startExercise(config)
                    Log.d(TAG, "Exercise started successfully, requesting LOCATION data at 2Hz.")
                } else {
                    Log.e(TAG, "Running exercise type not supported")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error starting exercise", e)
            }
        }
    }




    private fun handleLocationBatch(locations: List<SampleDataPoint<LocationData>>) {
        lifecycleScope.launch {
            try {
                if (!this@MyExerciseService::database.isInitialized) {
                    Log.e(TAG, "Database not initialized. Skipping location save.")
                    return@launch
                }

                val matchId = database.matchDao().getMatchId()
                if (matchId != null) {
                    val locationEntities = locations.map { dataPoint ->
                        LocationDataEntity(
                            latitude = dataPoint.value.latitude,
                            longitude = dataPoint.value.longitude,
                            timestamp = dataPoint.timeDurationFromBoot.toMillis(),
                            matchId = matchId,
                            //accuracy = dataPoint.value.accuracy?.horizontalAccuracyMeters
                        )
                    }

                    database.locationDataDao().insertAll(locationEntities)
                    Log.d(TAG, "Successfully saved a batch of ${locationEntities.size} location points.")
                } else {
                    Log.e(TAG, "No matchId found. Skipping location save.")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error while saving location batch to database", e)
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        stopExercise()
    }

    private fun stopExercise() {
        lifecycleScope.launch {
            try {
                exerciseClient.endExercise()
                exerciseClient.clearUpdateCallback(exerciseUpdateCallback)
                Log.d(TAG, "Exercise ended successfully")
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) {
                    Log.w(TAG, "Exercise ending was cancelled", e)
                } else {
                    Log.e(TAG, "Error ending exercise", e)
                }
            }
        }
    }

    fun pauseExercise() {
        lifecycleScope.launch {
            try {
                if (!isExercisePaused) {
                    exerciseClient.pauseExercise()
                    isExercisePaused = true
                    Log.d(TAG, "Exercise paused successfully")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error pausing exercise", e)
            }
        }
    }

    fun resumeExercise() {
        lifecycleScope.launch {
            try {
                if (isExercisePaused) {
                    exerciseClient.resumeExercise()
                    isExercisePaused = false
                    Log.d(TAG, "Exercise resumed successfully")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error resuming exercise", e)
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "exercise_channel",
                "Exercise Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, "exercise_channel")
            .setContentTitle("Exercise Tracking")
            .setContentText("Tracking your exercise...")
            .setSmallIcon(R.drawable.logobig) // Replace with your icon
            .setOngoing(true)
            .build()
    }
}
