package com.example.footballstatistics_app_wearos.presentation

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.footballstatistics_app_wearos.R
import com.example.footballstatistics_app_wearos.presentation.data.AppDatabase
import com.example.footballstatistics_app_wearos.presentation.presentation.TransferEvent
import com.example.footballstatistics_app_wearos.presentation.presentation.TransferState
import com.example.footballstatistics_app_wearos.presentation.presentation.UploadViewModel
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class TransferDataService : Service() {
    private val CHANNEL_ID = "TransferDataServiceChannel"
    private val NOTIFICATION_ID = 1002
    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var dataClient: DataClient
    private lateinit var database: AppDatabase
    private lateinit var viewModel: UploadViewModel

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        Log.d("TransferDataService", "Service created")
        dataClient = Wearable.getDataClient(this)
        val container = (application as MyApplication).container
        database = container.database
        viewModel = container.uploadViewModel
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("TransferDataService", "Service started")
        viewModel.sendTransferEvent(TransferEvent(TransferState.IN_PROGRESS,0))

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            if (checkSelfPermission(Manifest.permission.FOREGROUND_SERVICE_CONNECTED_DEVICE) == PackageManager.PERMISSION_GRANTED) {
                createNotificationChannel()
                val notification = createNotification()

                startForeground(
                    NOTIFICATION_ID,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE
                )
            } else {
                stopSelf()
                viewModel.sendTransferEvent(TransferEvent(TransferState.FAILED))
            }
        } else {
            createNotificationChannel()
            val notification = createNotification()
            startForeground(NOTIFICATION_ID, notification)
        }
        coroutineScope.launch {
            try {
                sendDataToPhone()
                viewModel.sendTransferEvent(TransferEvent(TransferState.COMPLETED))
                stopSelf()
            } catch (e: Exception) {
                viewModel.sendTransferEvent(TransferEvent(TransferState.FAILED))
                Log.e("TransferDataService", "Error sending data", e)
            }
        }

        return START_STICKY
    }
    private suspend fun sendDataToPhone() {
        val matches = database.matchDao().getAllMatches()
        val locationData = database.locationDataDao().getAllLocationData()

        val gson = Gson()
        val matchesJson = gson.toJson(matches)
        val locationDataJson = gson.toJson(locationData)

        viewModel.sendTransferEvent(TransferEvent(TransferState.IN_PROGRESS,50))

        val request = PutDataMapRequest.create("/transfer_data").run {
            dataMap.putString("matches", matchesJson)
            dataMap.putString("location_data", locationDataJson)
            setUrgent()
            asPutDataRequest()
        }
        try {
            dataClient.putDataItem(request)
            viewModel.sendTransferEvent(TransferEvent(TransferState.IN_PROGRESS,100))
        }catch (e: Exception) {
            Log.e("TransferDataService", "Error sending data", e)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Transfer Data Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }

    private fun createNotification(): Notification {
        val notificationIntent = Intent(this, TransferDataService::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Transfer Data Service")
            .setContentText("Transferring data...")
            .setSmallIcon(R.mipmap.logoapp)
            .setContentIntent(pendingIntent)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("TransferDataService", "Service destroyed")
        coroutineScope.cancel()
    }
}