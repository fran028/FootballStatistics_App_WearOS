package com.example.footballstatistics_app_wearos.presentation

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.footballstatistics_app_wearos.R
import com.example.footballstatistics_app_wearos.presentation.data.AppDatabase
import com.example.footballstatistics_app_wearos.presentation.data.LocationDataEntity
import com.example.footballstatistics_app_wearos.presentation.data.MatchEntity
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.ObjectOutputStream
import java.util.concurrent.TimeUnit

class TransferDataService : Service() {

    private lateinit var dataClient: DataClient
    private var isSendingData = false
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private lateinit var database: AppDatabase

    companion object {
        const val CHANNEL_ID = "DataChannel"
        const val NOTIFICATION_ID = 1
        const val DATA_PATH = "/all_data"
    }

    override fun onCreate() {
        super.onCreate()
        dataClient = Wearable.getDataClient(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotificationChannel()
        val notification = createNotification()
        startForeground(NOTIFICATION_ID, notification)
        isSendingData = true
        startSendingData()
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        isSendingData = false
    }

    private fun startSendingData() {
        Thread {
            while (isSendingData) {
                var matchData: List<MatchEntity> = emptyList()
                var locationData: List<LocationDataEntity> = emptyList()
                coroutineScope.launch {
                    matchData = getMatchData()
                    locationData = getLocationData()
                    sendAllDataToPhone(dataClient, matchData, locationData)
                    Log.d("Smartwatch", "Sending all data")
                }
                TimeUnit.SECONDS.sleep(5)
            }
        }.start()
    }

    private fun sendAllDataToPhone(
        dataClient: DataClient,
        matchData: List<MatchEntity>,
        locationData: List<LocationDataEntity>,
    ) {
        val serializedData = serializeData(matchData, locationData)

        val dataMapRequest = PutDataMapRequest.create(DATA_PATH)
        dataMapRequest.dataMap.putByteArray("all_data", serializedData)

        val putDataRequest = dataMapRequest.asPutDataRequest()
        putDataRequest.setUrgent()
        dataClient.putDataItem(putDataRequest)
            .addOnSuccessListener {
                Log.d("Smartwatch", "All data sent successfully")
            }
            .addOnFailureListener { e ->
                Log.e("Smartwatch", "Error sending all data", e)
            }
    }

    private suspend fun getMatchData(): List<MatchEntity> = withContext(Dispatchers.IO) {
        database.matchDao().getAllMatches()
    }
    private suspend fun getLocationData(): List<LocationDataEntity> = withContext(Dispatchers.IO) {
        database.locationDataDao().getAllLocationData()
    }

    // Serialize data using ObjectOutputStream
    private fun serializeData(
        matchData: List<MatchEntity>,
        locationData: List<LocationDataEntity>,
    ): ByteArray {
        val byteArrayOutputStream = ByteArrayOutputStream()
        val objectOutputStream = ObjectOutputStream(byteArrayOutputStream)
        objectOutputStream.writeObject(matchData)
        objectOutputStream.writeObject(locationData)
        objectOutputStream.close()
        return byteArrayOutputStream.toByteArray()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Data Channel",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    private fun createNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Data Service")
            .setContentText("Collecting and sending data")
            .setSmallIcon(R.drawable.logobig) // Replace with your icon
            .setContentIntent(pendingIntent)
            .build()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}