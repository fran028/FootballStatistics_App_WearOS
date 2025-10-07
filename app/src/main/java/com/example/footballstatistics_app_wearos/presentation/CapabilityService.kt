package com.example.footballstatistics_app_wearos.presentation

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// Change this from WearableListenerService to a regular Service
class CapabilityService : Service() {

    private val capabilityClient: CapabilityClient by lazy { Wearable.getCapabilityClient(this) }
    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)

    private val capabilityName = "football_app_capability"
    private val notificationChannelId = "capability_service_channel"
    private val notificationId = 101

    override fun onCreate() {
        super.onCreate()
        Log.d("CapabilityService", "Service created. Promoting to foreground.")

        // Create notification channel
        createNotificationChannel()

        // Create the notification
        val notification = NotificationCompat.Builder(this, notificationChannelId)
            .setContentTitle("App Connection Active")
            .setContentText("Broadcasting capability to phone.")
            // Use a small icon from your drawables, or a system default
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setOngoing(true)
            .build()

        // Promote the service to the foreground
        startForeground(notificationId, notification)

        Log.d("CapabilityService", "Advertising capability.")
        advertiseCapability()
    }

    // This is required for a regular Service, just return null
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun advertiseCapability() {
        serviceScope.launch {
            try {
                capabilityClient
                    .addLocalCapability(capabilityName)
                    .await()
                Log.d("CapabilityService", "✅ Successfully advertised capability: $capabilityName")
            } catch (e: Exception) {
                Log.e("CapabilityService", "❌ Failed to advertise capability", e)
            }
        }
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            notificationChannelId,
            "App Connection",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }

    override fun onDestroy() {
        super.onDestroy()
        // Stop the foreground service and remove the notification
        stopForeground(true)

        serviceScope.launch {
            try {
                capabilityClient
                    .removeLocalCapability(capabilityName)
                    .await()
                Log.d("CapabilityService", "Removed local capability.")
            } catch (e: Exception) {
                Log.e("CapabilityService", "Failed to remove capability", e)
            }
        }
        serviceJob.cancel()
    }
}