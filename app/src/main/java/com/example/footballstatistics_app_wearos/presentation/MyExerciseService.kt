package com.example.footballstatistics_app_wearos.presentation

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.activity.result.launch
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.getSystemService
import androidx.health.services.client.ExerciseClient
import androidx.health.services.client.ExerciseUpdateCallback
import androidx.health.services.client.HealthServices
import androidx.health.services.client.clearUpdateCallback
import androidx.health.services.client.data.Availability
import androidx.health.services.client.data.DataType
import androidx.health.services.client.data.ExerciseConfig
import androidx.health.services.client.data.ExerciseLapSummary
import androidx.health.services.client.data.ExerciseType
import androidx.health.services.client.data.ExerciseUpdate
import androidx.health.services.client.data.LocationAvailability
import androidx.health.services.client.endExercise
import androidx.health.services.client.getCapabilities
import androidx.health.services.client.pauseExercise
import androidx.health.services.client.resumeExercise
import androidx.health.services.client.startExercise
import com.example.footballstatistics_app_wearos.R
import com.example.footballstatistics_app_wearos.presentation.data.AppDatabase
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.room.Room
import com.example.footballstatistics_app_wearos.presentation.data.LocationDataEntity
import com.example.footballstatistics_app_wearos.presentation.data.MatchEntity
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import java.time.Instant

class MyExerciseService : LifecycleService() {
    val TAG = "MyExerciseService"

    private lateinit var exerciseClient: ExerciseClient
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var database: AppDatabase
    private var exerciseStarted = false
    private var isExercisePaused = false

    companion object {
        const val ACTION_PAUSE = "com.example.footballstatistics_app_wearos.ACTION_PAUSE"
        const val ACTION_RESUME = "com.example.footballstatistics_app_wearos.ACTION_RESUME"
        private const val LOCATION_PERMISSION_REQUEST_CODE = 333
    }

    private val binder = LocalBinder()

    inner class LocalBinder : Binder() {
        fun getService(): MyExerciseService = this@MyExerciseService
    }

    override fun onBind(intent: Intent): IBinder {
        super.onBind(intent)
        return binder
    }

    fun handleLocationUpdate(location: Location) {
        Log.d("MyExerciseService", "Location received: ${location.latitude}, ${location.longitude}")
        storeLocationData(location)
    }

    private val exerciseUpdateCallback = object : ExerciseUpdateCallback {
        override fun onExerciseUpdateReceived(update: ExerciseUpdate) {
            val latestMetrics = update.latestMetrics
            Log.d(TAG, "Exercise Update Metrics: $latestMetrics")
            Log.d(TAG, "Exercise Metrics dataType: ${latestMetrics.dataTypes}")
            Log.d(TAG, "Exercise Metrics dataPoints: ${latestMetrics.sampleDataPoints}")
            Log.d(TAG, "Exercise Metrics statisticalDataPoints: ${latestMetrics.statisticalDataPoints}")
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

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            for (location in locationResult.locations) {
                handleLocationUpdate(location)
            }
        }
    }

    override fun onCreate() {
        Log.d("MyExerciseService", "Service Created")
        super.onCreate()
        exerciseClient = HealthServices.getClient(this).exerciseClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "app_database"
        ).build()
        createNotificationChannel()
    }

    private fun requestLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000)
                .setWaitForAccurateLocation(false)
                .setMinUpdateIntervalMillis(500)
                .setMaxUpdateDelayMillis(1000)
                .build()

            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        Log.d("MyExerciseService", "Service Started (onStartCommand())")
        startForeground(1, createNotification())
        if (!exerciseStarted) {
            Log.d("MyExerciseService", "Starting exercise...")
            startExercise()
            requestLocationUpdates()
            exerciseStarted = true
        }
        return START_STICKY // Corrected line
    }

    private fun startExercise() {
        Log.d("MyExerciseService", "Starting exercise... (startExercise())")
        lifecycleScope.launch {
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

    private fun storeLocationData(location: Location) {
        Log.d("MyExerciseService", "storeLocationData called")
        lifecycleScope.launch {
            try {
                // Check if the database has been initialized:
                if (!this@MyExerciseService::database.isInitialized) {
                    Log.e("MyExerciseService", "Database was not initialized properly!")
                    return@launch
                }
                val matchId = database.matchDao().getMatchId()
                if (matchId != null) {
                    val locationDataEntity = LocationDataEntity(
                        latitude = location.latitude,
                        longitude = location.longitude,
                        timestamp = Instant.ofEpochMilli(location.time).toEpochMilli(),
                        matchId = matchId
                    )
                    database.locationDataDao().insertLocationData(locationDataEntity)
                    Log.d("MyExerciseService", "successfully saved location data")
                } else {
                    Log.e("MyExerciseService", "No matchId was found")
                }

            } catch (e: Exception) {
                Log.e("MyExerciseService", "Error while accessing database", e)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopExercise()
        fusedLocationClient.removeLocationUpdates(locationCallback) // Stop updates

    }

    private fun stopExercise() {
        lifecycleScope.launch {
            try {
                exerciseClient.endExercise()
                exerciseClient.clearUpdateCallback(exerciseUpdateCallback)
                Log.d("MyExerciseService", "Exercise ended successfully")
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException){
                    Log.w("MyExerciseService", "Exercise ending was cancelled", e)
                } else {
                    Log.e("MyExerciseService", "Error ending exercise", e)
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
                    Log.d("MyExerciseService", "Exercise paused successfully")
                }
            } catch (e: Exception) {
                Log.e("MyExerciseService", "Error pausing exercise", e)
            }
        }
    }

    fun resumeExercise() {
        lifecycleScope.launch {
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