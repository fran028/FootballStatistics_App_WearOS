package com.example.footballstatistics_app_wearos.presentation

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.os.SystemClock
import android.util.Log
import androidx.compose.foundation.layout.size
import androidx.compose.ui.geometry.isEmpty
import androidx.core.app.NotificationCompat
import androidx.health.services.client.ExerciseClient
import androidx.health.services.client.ExerciseUpdateCallback
import androidx.health.services.client.HealthServices
import androidx.health.services.client.data.Availability
import androidx.health.services.client.data.DataType
import androidx.health.services.client.data.ExerciseConfig
import androidx.health.services.client.data.ExerciseLapSummary
import androidx.health.services.client.data.ExerciseType
import androidx.health.services.client.data.ExerciseUpdate
import androidx.health.services.client.data.LocationAvailability
import androidx.health.services.client.data.WarmUpConfig
import androidx.health.services.client.endExercise
import androidx.health.services.client.getCapabilities
import androidx.health.services.client.pauseExercise
import androidx.health.services.client.prepareExercise
import androidx.health.services.client.resumeExercise
import androidx.health.services.client.startExercise
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.example.footballstatistics_app_wearos.presentation.data.AppDatabase
import com.example.footballstatistics_app_wearos.presentation.data.LocationDataEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Duration

class MyExerciseService : LifecycleService() {

    private val binder = LocalBinder()
    private lateinit var exerciseClient: ExerciseClient
    private var exerciseInProgress = false
    private val locationPoints = mutableListOf<Triple<Double, Double, Long>>()
    private var exerciseJob: Job? = null

    private val locationDataDao by lazy {
        AppDatabase.getDatabase(this).locationDataDao()
    }

    private val matchDao by lazy {
        AppDatabase.getDatabase(this).matchDao()
    }

    companion object {
        const val TAG = "MyExerciseService"
        const val ACTION_PAUSE = "com.example.footballstatistics_app_wearos.ACTION_PAUSE"
        const val ACTION_RESUME = "com.example.footballstatistics_app_wearos.ACTION_RESUME"
        private const val NOTIFICATION_CHANNEL_ID = "MyExerciseServiceChannel"
        private const val NOTIFICATION_ID = 1
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate()")
        exerciseClient = HealthServices.getClient(this).exerciseClient
        // Call startForeground immediately to prevent the crash
        setForeground()
    }

    private fun setForeground() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            "Exercise Tracking", // User-visible channel name
            NotificationManager.IMPORTANCE_DEFAULT
        )
        notificationManager.createNotificationChannel(channel)

        val notification: Notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Activity Active")
            .setContentText("Tracking your location...")
            // Use a standard Android system icon to prevent resource errors
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .setOngoing(true) // Makes the notification non-dismissible
            .build()

        startForeground(NOTIFICATION_ID, notification)
    }

    private val exerciseUpdateCallback = object : ExerciseUpdateCallback {
        override fun onExerciseUpdateReceived(update: ExerciseUpdate) {
            if (update.exerciseStateInfo.state.isEnded) {
                Log.d(TAG, "Exercise has ended. Triggering save.")
                saveAndClearData()
                return
            }

            val locationData = update.latestMetrics.getData(DataType.LOCATION)

            if (locationData.isNotEmpty()) {
                Log.d(TAG, "Received ${locationData.size} location points.")
                for (location in locationData) {
                    val lat = location.value.latitude
                    val lon = location.value.longitude

                    // Calculate the real-world timestamp for the data point
                    val elapsedRealtimeMillis = SystemClock.elapsedRealtime()
                    val durationFromBoot: Duration = location.timeDurationFromBoot
                    val time = System.currentTimeMillis() - (elapsedRealtimeMillis - durationFromBoot.toMillis())

                    locationPoints.add(Triple(lat, lon, time))
                    Log.d(TAG, "New location point: Lat=$lat, Lon=$lon, Time=$time")
                }
            }
        }

        override fun onLapSummaryReceived(lapSummary: ExerciseLapSummary) { /* Not used */ }
        override fun onRegistered() {
            Log.d(TAG, "ExerciseUpdateCallback registered successfully.")
        }

        override fun onRegistrationFailed(throwable: Throwable) {
            Log.e(TAG, "ExerciseUpdateCallback registration failed.", throwable)
        }

        override fun onAvailabilityChanged(dataType: DataType<*, *>, availability: Availability) {
            if (dataType == DataType.LOCATION) {
                Log.d(TAG, "Location availability: $availability")
                if (availability is LocationAvailability) {
                    if (availability != LocationAvailability.ACQUIRING && availability != LocationAvailability.ACQUIRED_TETHERED && availability != LocationAvailability.ACQUIRED_UNTETHERED) {
                        Log.w(TAG, "GPS signal is not acquired. Current status: $availability")
                    }
                }
            }
        }
    }

    private fun startExercise() {
        if (exerciseInProgress) {
            Log.d(TAG, "Exercise already in progress.")
            return
        }
        Log.d(TAG, "Starting exercise...")
        exerciseJob = lifecycleScope.launch {
            try {
                // Set the callback before starting
                exerciseClient.setUpdateCallback(exerciseUpdateCallback)

                val capabilities = exerciseClient.getCapabilities()
                if (ExerciseType.RUNNING in capabilities.supportedExerciseTypes) {
                    // Define the main exercise configuration
                    val config = ExerciseConfig.builder(ExerciseType.RUNNING)
                        .setDataTypes(setOf(DataType.LOCATION))
                        .setIsAutoPauseAndResumeEnabled(false)
                        .setIsGpsEnabled(true)
                        .build()


                    val warmUpConfig = WarmUpConfig(
                        exerciseType = ExerciseType.RUNNING,
                        dataTypes = setOf(DataType.LOCATION)
                    )

                    // Now, call prepareExercise with the correct WarmUpConfig object.
                    exerciseClient.prepareExercise(warmUpConfig)

                    // Start the exercise with the main ExerciseConfig object.
                    exerciseClient.startExercise(config)

                    exerciseInProgress = true
                    Log.d(TAG, "Exercise started successfully after preparation.")
                } else {
                    Log.e(TAG, "Running exercise type not supported")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error starting or preparing exercise", e)
            }
        }
    }


    private fun saveAndClearData() {
        if (locationPoints.isEmpty()) {
            Log.d(TAG, "No location points to save.")
            return
        }

        lifecycleScope.launch(Dispatchers.IO) {
            Log.d(TAG, "Saving ${locationPoints.size} location points.")
            val currentMatchId = matchDao.getMatchId()

            if (currentMatchId == null) {
                Log.e(TAG, "Cannot save locations, no active match ID found.")
                withContext(Dispatchers.Main) {
                    locationPoints.clear()
                }
                return@launch
            }

            val locationEntities = locationPoints.map { point ->
                LocationDataEntity(
                    matchId = currentMatchId,
                    latitude = point.first,
                    longitude = point.second,
                    timestamp = point.third
                )
            }

            try {
                locationDataDao.insertAll(locationEntities)
                Log.d(TAG, "Successfully inserted ${locationEntities.size} points for match ID $currentMatchId.")
            } catch (e: Exception) {
                Log.e(TAG, "Error saving location data to database", e)
            }

            withContext(Dispatchers.Main) {
                locationPoints.clear()
                Log.d(TAG, "Location points list cleared.")
            }
        }
    }

    private fun pauseExercise() {
        if (!exerciseInProgress) return
        lifecycleScope.launch {
            try {
                exerciseClient.pauseExercise()
                Log.d(TAG, "Exercise paused.")
            } catch (e: Exception) {
                Log.e(TAG, "Error pausing exercise", e)
            }
        }
    }

    private fun resumeExercise() {
        if (!exerciseInProgress) return
        lifecycleScope.launch {
            try {
                exerciseClient.resumeExercise()
                Log.d(TAG, "Exercise resumed.")
            } catch (e: Exception) {
                Log.e(TAG, "Error resuming exercise", e)
            }
        }
    }

    fun stopExercise() {
        Log.d(TAG, "stopExercise() called.")
        if (!exerciseInProgress) {
            Log.d(TAG, "No exercise in progress to stop.")
            return
        }
        lifecycleScope.launch {
            try {
                exerciseClient.endExercise()
                Log.d(TAG, "Called endExercise(). Final save will be triggered by callback.")
                exerciseInProgress = false
            } catch (e: Exception) {
                Log.e(TAG, "Error stopping exercise", e)
            } finally {
                exerciseJob?.cancel()
                // Stop the foreground service and remove the notification
                stopForeground(STOP_FOREGROUND_REMOVE)
                Log.d(TAG, "Foreground service stopped.")
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        Log.d(TAG, "onStartCommand() with action: ${intent?.action}")

        when (intent?.action) {
            ACTION_PAUSE -> pauseExercise()
            ACTION_RESUME -> resumeExercise()
        }

        return START_STICKY
    }

    inner class LocalBinder : Binder() {
        fun getService(): MyExerciseService = this@MyExerciseService
    }

    override fun onBind(intent: Intent): IBinder {
        super.onBind(intent)
        Log.d(TAG, "onBind()")
        handleBind()
        return binder
    }

    private fun handleBind() {
        if (!exerciseInProgress) {
            startExercise()
        }
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Log.d(TAG, "onUnbind() called.")
        // The client (Activity) is responsible for calling stopExercise.
        // Do not stop it here, as it's too late for the save operation.
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy()")
        if (exerciseInProgress) {
            // Service is being destroyed unexpectedly. Attempt a final stop.
            stopExercise()
        }
    }
}
