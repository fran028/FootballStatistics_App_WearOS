package com.example.footballstatistics_app_wearos.presentation

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.compose.foundation.gestures.forEach
import androidx.core.app.NotificationCompat
import androidx.health.services.client.ExerciseClient
import androidx.health.services.client.ExerciseUpdateCallback
import androidx.health.services.client.HealthServices
import androidx.health.services.client.clearUpdateCallback
import androidx.health.services.client.data.Availability
import androidx.health.services.client.data.DataPoint
import androidx.health.services.client.data.DataType
import androidx.health.services.client.data.ExerciseConfig
import androidx.health.services.client.data.ExerciseLapSummary
import androidx.health.services.client.data.ExerciseType
import androidx.health.services.client.data.ExerciseUpdate
import androidx.health.services.client.data.LocationAvailability
import androidx.health.services.client.data.LocationData
import androidx.health.services.client.endExercise
import androidx.health.services.client.getCapabilities
import androidx.health.services.client.pauseExercise
import androidx.health.services.client.resumeExercise
import androidx.health.services.client.startExercise
import com.example.footballstatistics_app_wearos.R
import com.example.footballstatistics_app_wearos.presentation.data.AppDatabase
import com.example.footballstatistics_app_wearos.presentation.data.LocationDataEntity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MyExerciseService : Service() {

    private lateinit var exerciseClient: ExerciseClient
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private var exerciseStarted = false
    private var isExercisePaused = false
    private lateinit var database: AppDatabase

    companion object {
        const val ACTION_PAUSE = "com.example.footballstatistics_app_wearos.ACTION_PAUSE"
        const val ACTION_RESUME = "com.example.footballstatistics_app_wearos.ACTION_RESUME"
    }

    private val exerciseUpdateCallback = object : ExerciseUpdateCallback {
        override fun onExerciseUpdateReceived(event: ExerciseUpdate) {
            Log.d("MyExerciseService", "Exercise Update: $event")
            val latestMetrics = event.latestMetrics
            Log.d("MyExerciseService", "Latest Metrics: $latestMetrics")
        }


        override fun onLapSummaryReceived(lapSummary: ExerciseLapSummary) {
            Log.d("MyExerciseService", "Lap Summary: $lapSummary")
        }

        override fun onRegistered() {
            Log.d("MyExerciseService", "Registered")
        }

        override fun onRegistrationFailed(throwable: Throwable) {
            Log.e("MyExerciseService", "Registration Failed", throwable)
        }

        override fun onAvailabilityChanged(dataType: DataType<*, *>, availability: Availability) {
            if (dataType == DataType.LOCATION) {
                val locationAvailability = availability as LocationAvailability
                Log.d("MyExerciseService", "Location Availability: $locationAvailability")
            }
        }
    }

    override fun onCreate() {
        Log.d("MyExerciseService", "Service Created")
        super.onCreate()
        exerciseClient = HealthServices.getClient(this).exerciseClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("MyExerciseService", "Service Started (onStartCommand())")
        startForeground(1, createNotification())
        if (!exerciseStarted) {
            Log.d("MyExerciseService", "Starting exercise...")
            startExercise()
            exerciseStarted = true
        }
        return START_STICKY
    }

    private fun startExercise() {
        Log.d("MyExerciseService", "Starting exercise... (startExercise())")
        coroutineScope.launch {
            try {
                val supportedExerciseTypes = exerciseClient.getCapabilities().supportedExerciseTypes
                if (ExerciseType.RUNNING in supportedExerciseTypes) {
                    val config = ExerciseConfig(
                        ExerciseType.RUNNING,
                        setOf(DataType.LOCATION),
                        isAutoPauseAndResumeEnabled = false,
                        isGpsEnabled = true,
                        //locationSamplingConfiguration = LocationSamplingConfiguration.continuous()
                    )
                    exerciseClient.startExercise(config)
                    exerciseClient.setUpdateCallback(exerciseUpdateCallback)
                    Log.d("MyExerciseService", "Exercise started successfully")
                } else {
                    Log.e("MyExerciseService", "Running exercise type not supported")
                }
            } catch (e: Exception) {
                Log.e("MyExerciseService", "Error starting exercise", e)
            }
        }
    }

    /*private fun storeLocationData(locationPoint: LocationPoint) {
        coroutineScope.launch {
            val locationDataEntity = LocationDataEntity(
                latitude = locationPoint.latitude,
                longitude = locationPoint.longitude,
                timestamp = locationPoint.time.toEpochMilli(),
                id = TODO(),
                matchId = database.matchDao().getMatchId()
            )
            database.locationDataDao().insertLocationData(locationDataEntity)
        }
    }*/

    override fun onDestroy() {
        super.onDestroy()
        stopExercise()
    }

    private fun stopExercise() {
        coroutineScope.launch {
            try {
                exerciseClient.endExercise()
                exerciseClient.clearUpdateCallback(exerciseUpdateCallback)
                Log.d("MyExerciseService", "Exercise ended successfully")
            } catch (e: Exception) {
                Log.e("MyExerciseService", "Error ending exercise", e)
            }
        }
    }

    fun pauseExercise() {
        coroutineScope.launch {
            try {
                if (!isExercisePaused) {
                    exerciseClient.pauseExercise()
                    isExercisePaused = true
                    Log.d("MyExerciseService", "Exercise paused successfully")
                }
            } catch (e: Exception) {
                Log.e("MyExerciseService", "Error pausing exercise", e)
            }
        }
    }

    fun resumeExercise() {
        coroutineScope.launch {
            try {
                if (isExercisePaused) {
                    exerciseClient.resumeExercise()
                    isExercisePaused = false
                    Log.d("MyExerciseService", "Exercise resumed successfully")
                }
            } catch (e: Exception) {
                Log.e("MyExerciseService", "Error resuming exercise", e)
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
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