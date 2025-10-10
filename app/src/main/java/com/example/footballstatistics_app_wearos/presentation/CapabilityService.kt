package com.example.footballstatistics_app_wearos.presentation

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
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

class CapabilityService : Service() {

    private val capabilityClient: CapabilityClient by lazy { Wearable.getCapabilityClient(this) }
    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)

    private val capabilityName = "football_app_capability"
    private val notificationChannelId = "capability_service_channel"
    private val notificationId = 101

    override fun onCreate() {
        super.onCreate()
        Log.d("CapabilityService", "Service created.")
        createNotificationChannel()
    }

    // This is the entry point for a started service.
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("CapabilityService", "onStartCommand received. Advertising capability.")

        val notification = NotificationCompat.Builder(this, notificationChannelId)
            .setContentTitle("App Connection")
            .setContentText("Checking connection with the phone...")
            .setSmallIcon(android.R.drawable.ic_dialog_info) // Replace with your own icon
            .setOngoing(true)
            .build()

        // Promote the service to the foreground before doing work.
        startForeground(notificationId, notification)

        // Advertise the capability and then stop the service.
        advertiseCapabilityThenStop()

        // We use START_NOT_STICKY because this service's job is short-lived.
        // If the system kills it, we don't need it to be automatically restarted.
        return START_NOT_STICKY
    }

    private fun advertiseCapabilityThenStop() {
        serviceScope.launch {
            try {
                capabilityClient
                    .addLocalCapability(capabilityName)
                    .await()
                Log.d("CapabilityService", "✅ Successfully advertised capability: $capabilityName")
            } catch (e: Exception) {
                // This is where the DUPLICATE_CAPABILITY exception is caught.
                // Even if it's a duplicate, the desired state (advertised) is true, so this is not a fatal error.
                Log.e("CapabilityService", "Failed to advertise capability (maybe a duplicate):", e)
            } finally {
                // This is the crucial part: stop the service after the work is done.
                Log.d("CapabilityService", "Work finished. Stopping service.")
                stopSelf()
            }
        }
    }

    // onBind is required for a Service, but we don't need to bind, so we return null.
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                notificationChannelId,
                "App Connection",
                NotificationManager.IMPORTANCE_LOW // Use LOW importance for this type of notification
            )
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        // onDestroy is now primarily for cleanup if the system kills the service.
        // The capability removal logic should be tied to the app's lifecycle,
        // for example, when the main activity is destroyed.
        // Leaving this here is fine, but it might not be called if stopSelf() completes.
        serviceScope.launch {
            try {
                capabilityClient
                    .removeLocalCapability(capabilityName)
                    .await()
                Log.d("CapabilityService", "Removed local capability on destroy.")
            } catch (e: Exception) {
                Log.e("CapabilityService", "Failed to remove capability on destroy", e)
            }
        }
        serviceJob.cancel()
        super.onDestroy()
    }
}
