package com.example.footballstatistics_app_wearos.presentation

//import android.Manifest
//import android.content.pm.PackageManager
//import android.os.Bundle
//import android.util.Log
//import androidx.activity.ComponentActivity
//import androidx.activity.compose.setContent
//import androidx.activity.result.contract.ActivityResultContracts
//import androidx.compose.foundation.layout.Column
//import androidx.compose.material3.Button
//import androidx.compose.material3.Text
//import androidx.compose.runtime.*
//import androidx.core.content.ContextCompat
//import androidx.health.services.client.HealthServices
//import androidx.health.services.client.data.DataType
//import androidx.health.services.client.data.PassiveMonitoringConfig
//import androidx.health.services.client.data.SampleDataPoint
//import androidx.health.services.client.data.Steps
//import androidx.health.services.client.passive.PassiveListener
//import kotlinx.coroutines.*
//
//
//class HealthServiceTest : ComponentActivity() {
//
//    private val TAG = "HealthApp"
//    private lateinit var healthServicesClient: HealthServices
//    private val coroutineScope = CoroutineScope(Dispatchers.Default)
//
//    private var stepsCount by mutableStateOf(0L)
//    private var isMonitoringActive by mutableStateOf(false)
//
//    private val requestPermissionLauncher =
//        registerForActivityResult(
//            ActivityResultContracts.RequestMultiplePermissions()
//        ) { permissions ->
//            when {
//                permissions.entries.all { it.value } -> {
//                    startPassiveMonitoring()
//                }
//                else -> {
//                    Log.i(TAG, "Permissions Denied")
//                }
//            }
//        }
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        healthServicesClient = HealthServices.getClient(this)
//
//        setContent {
//            ActiveDataScreen(stepsCount, isMonitoringActive,
//                onStartMonitoringClick = { requestPermissions() },
//                onStopMonitoringClick = { stopPassiveMonitoring() }
//            )
//        }
//    }
//
//    private fun requestPermissions() {
//        val permissions = arrayOf(
//            Manifest.permission.ACTIVITY_RECOGNITION,
//            Manifest.permission.BODY_SENSORS
//        )
//
//        when {
//            permissions.all {
//                ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
//            } -> {
//                startPassiveMonitoring()
//            }
//            shouldShowRequestPermissionRationale(Manifest.permission.ACTIVITY_RECOGNITION) ||
//                    shouldShowRequestPermissionRationale(Manifest.permission.BODY_SENSORS) -> {
//                Log.i(TAG, "Show rationale for permissions")
//            }
//            else -> {
//                requestPermissionLauncher.launch(permissions)
//            }
//        }
//    }
//
//    private fun startPassiveMonitoring() {
//        if (isMonitoringActive) return
//
//        val passiveListener = object : PassiveListener {
//            override fun onNewDataPoints(dataPoints: List<SampleDataPoint>) {
//                for (dataPoint in dataPoints) {
//                    if (dataPoint.dataType == DataType.STEPS) {
//                        val steps = dataPoint.value as Steps
//                        stepsCount += steps.count
//                        Log.d(TAG, "Steps: ${steps.count}")
//                    }
//                }
//            }
//        }
//
//        val config = PassiveMonitoringConfig.Builder()
//            .setDataType(DataType.STEPS)
//            .setPassiveListener(passiveListener)
//            .build()
//
//        coroutineScope.launch {
//            try {
//                healthServicesClient.passiveClient.startPassiveMonitoring(config)
//                isMonitoringActive = true
//                Log.i(TAG, "Passive monitoring started")
//            } catch (e: Exception) {
//                Log.e(TAG, "Error starting passive monitoring", e)
//            }
//        }
//    }
//
//    private fun stopPassiveMonitoring() {
//        if (!isMonitoringActive) return
//
//        coroutineScope.launch {
//            try {
//                healthServicesClient.passiveClient.stopPassiveMonitoring(DataType.STEPS)
//                isMonitoringActive = false
//                Log.i(TAG, "Passive monitoring stopped")
//            } catch (e: Exception) {
//                Log.e(TAG, "Error stopping passive monitoring", e)
//            }
//        }
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//        stopPassiveMonitoring()
//        coroutineScope.cancel()
//    }
//}
//
//@Composable
//fun ActiveDataScreen(steps: Long, isMonitoring: Boolean, onStartMonitoringClick: () -> Unit, onStopMonitoringClick: () -> Unit) {
//    Column {
//        Text("Steps: $steps")
//        Button(onClick = onStartMonitoringClick, enabled = !isMonitoring) {
//            Text("Start Monitoring")
//        }
//        Button(onClick = onStopMonitoringClick, enabled = isMonitoring) {
//            Text("Stop Monitoring")
//        }
//    }
//}