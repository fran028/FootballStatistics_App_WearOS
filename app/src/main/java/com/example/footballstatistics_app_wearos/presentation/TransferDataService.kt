package com.example.footballstatistics_app_wearos.presentation

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import com.example.footballstatistics_app_wearos.R
import com.example.footballstatistics_app_wearos.presentation.data.AppDatabase
import com.example.footballstatistics_app_wearos.presentation.presentation.TransferEvent
import com.example.footballstatistics_app_wearos.presentation.presentation.TransferState
import com.example.footballstatistics_app_wearos.presentation.presentation.UploadViewModel
import com.google.android.gms.tasks.Task
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.DataItem
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull

class TransferDataService : LifecycleService() {
    private val TAG = "TransferDataService"
    private val CHANNEL_ID = "TransferDataServiceChannel"
    private val NOTIFICATION_ID = 1001

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var dataClient: DataClient
    private lateinit var database: AppDatabase
    private lateinit var viewModel: UploadViewModel

    override fun onBind(intent: Intent): IBinder {
        super.onBind(intent)
        // onBind is required for LifecycleService, but we don't need a binder for this service.
        throw UnsupportedOperationException("Not yet implemented")
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service created")
        dataClient = Wearable.getDataClient(this)
        val container = (application as FootballStatisticsApplication).container
        database = container.database
        viewModel = container.uploadViewModel
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        Log.d(TAG, "Service started")
        viewModel.sendTransferEvent(TransferEvent(TransferState.IN_PROGRESS, 0))

        // --- SOLUTION: Call startForeground() IMMEDIATELY ---
        createNotificationChannel()
        val notification = createNotification("Preparing to transfer...")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
        // --- The 5-second rule is now satisfied ---


        // --- Now, check for permissions ---
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                Log.e(TAG, "POST_NOTIFICATIONS permission not granted. Stopping.")
                viewModel.sendTransferEvent(TransferEvent(TransferState.FAILED, 0))
                stopSelf()
                return START_NOT_STICKY
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (checkSelfPermission(Manifest.permission.FOREGROUND_SERVICE) != PackageManager.PERMISSION_GRANTED) {
                Log.e(TAG, "Foreground service permission not granted. Stopping.")
                viewModel.sendTransferEvent(TransferEvent(TransferState.FAILED, 0))
                stopSelf()
                return START_NOT_STICKY
            }
        }

        // --- Start Data Transfer Coroutine ---
        serviceScope.launch {
            try {
                // 1. Verify connection first
                val isConnected = isPhoneConnected()
                if (!isConnected) {
                    throw IllegalStateException("Phone is not connected.")
                }

                // 2. Perform the data transfer and wait for it to complete
                sendMatchData()

                // 3. Send final completion signal to phone
                sendTransferCompleteSignal()

                // 4. Update UI to COMPLETED and stop service
                viewModel.sendTransferEvent(TransferEvent(TransferState.COMPLETED, 100))
                Log.d(TAG, "Data transfer process completed successfully.")
                stopSelf()

            } catch (e: Exception) {
                Log.e(TAG, "Error during data transfer", e)
                viewModel.sendTransferEvent(TransferEvent(TransferState.FAILED, 0))
                stopSelf()
            }
        }

        return START_STICKY
    }

    private suspend fun sendMatchData() {
        Log.d(TAG, "Fetching match data from database.")

        // Fetch the single match to be transferred.
        val matchToTransfer = database.matchDao().getMatch()
        if (matchToTransfer == null) {
            Log.w(TAG, "No match found in the database to transfer.")
            // Consider this a success if there's nothing to send.
            return
        }

        val locationData = database.locationDataDao().getLocationsForMatch(matchToTransfer.id)
        Log.d(TAG, "Found match ${matchToTransfer.id} with ${locationData.size} location points.")

        // --- Send Match Data ---
        updateNotification("Sending match details...")
        viewModel.sendTransferEvent(TransferEvent(TransferState.IN_PROGRESS, 25))
        val matchJson = Gson().toJson(matchToTransfer)
        val matchDataMap = PutDataMapRequest.create("/match_data")
        matchDataMap.dataMap.putString("match_data", matchJson)
        sendData(matchDataMap).await()
        Log.d(TAG, "Match data sent successfully.")

        // --- Send Location Data ---
        updateNotification("Sending location data...")
        viewModel.sendTransferEvent(TransferEvent(TransferState.IN_PROGRESS, 50))
        // Convert location objects to a simpler String format for efficiency
        val locationStrings = locationData.map { "${it.latitude},${it.longitude},${it.timestamp}" }
        val locationDataMap = PutDataMapRequest.create("/location_data")
        locationDataMap.dataMap.putStringArrayList("location_data", ArrayList(locationStrings))
        sendData(locationDataMap).await()
        Log.d(TAG, "Location data sent successfully.")
    }

    private suspend fun sendTransferCompleteSignal() {
        Log.d(TAG, "Sending transfer complete signal.")
        updateNotification("Finishing transfer...")
        viewModel.sendTransferEvent(TransferEvent(TransferState.IN_PROGRESS, 95))
        val completionRequest = PutDataMapRequest.create("/transfer_complete")
        sendData(completionRequest).await()
    }

    // A suspend function that wraps the async DataClient call
    private fun sendData(dataMapRequest: PutDataMapRequest): Task<DataItem> {
        val request = dataMapRequest.setUrgent().asPutDataRequest()
        return dataClient.putDataItem(request)
    }

    private suspend fun isPhoneConnected(): Boolean {
        return try {
            val connectedNodes = withTimeoutOrNull(5000) { // 5-second timeout
                Wearable.getNodeClient(this@TransferDataService).connectedNodes.await()
            }
            if (connectedNodes.isNullOrEmpty()) {
                Log.w(TAG, "No connected nodes found.")
                return false
            }
            Log.d(TAG, "Phone is connected: ${connectedNodes.first().displayName}")
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to check for connected phone.", e)
            false
        }
    }

    private fun updateNotification(contentText: String) {
        val notification = createNotification(contentText)
        getSystemService(NotificationManager::class.java).notify(NOTIFICATION_ID, notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Transfer Data Service",
                NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java).createNotificationChannel(serviceChannel)
        }
    }

    private fun createNotification(contentText: String): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Data Transfer")
            .setContentText(contentText)
            .setSmallIcon(android.R.drawable.ic_menu_upload) // Using a standard system icon
            .setOngoing(true)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Service destroyed")
        serviceScope.cancel()
    }
}
