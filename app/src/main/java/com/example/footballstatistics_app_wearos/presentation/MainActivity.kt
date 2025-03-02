package com.example.footballstatistics_app_wearos.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.navigation
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.example.footballstatistics_app_wearos.presentation.pages.ActivityCalibratePage
import com.example.footballstatistics_app_wearos.presentation.pages.ActivityResultPage
import com.example.footballstatistics_app_wearos.presentation.pages.ActivitySetUpPage
import com.example.footballstatistics_app_wearos.presentation.pages.ActivityTrackerPage
import com.example.footballstatistics_app_wearos.presentation.pages.CountdownPage
import com.example.footballstatistics_app_wearos.presentation.pages.MenuPage
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat


class MainActivity : ComponentActivity() {

    private val permissions = arrayOf(
        Manifest.permission.BODY_SENSORS,
        Manifest.permission.ACTIVITY_RECOGNITION,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.FOREGROUND_SERVICE,
        Manifest.permission.WAKE_LOCK,
        Manifest.permission.HIGH_SAMPLING_RATE_SENSORS
    )
    private val requestCode = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        super.onCreate(savedInstanceState)
        requestPermissions()
        setTheme(android.R.style.Theme_DeviceDefault)

        setContent {

            val navController = rememberSwipeDismissableNavController()
            SwipeDismissableNavHost(
                navController = navController,
                startDestination = "Menu"
            ) {
                composable("Menu"){
                    MenuPage(navController = navController)
                }
                navigation(
                    startDestination = "Activity_SetUp",
                    route = "Activity"
                ){
                    composable("Activity_SetUp"){
                        ActivitySetUpPage(navController = navController)
                    }
                    composable("Activity_Calibrate/{location}"){
                        val location = it.arguments?.getString("location")
                        ActivityCalibratePage(navController = navController, location = location.toString())
                    }
                    composable("Countdown"){
                        CountdownPage(navController = navController)
                    }
                    composable("Activity_Tracker"){
                        ActivityTrackerPage(navController = navController)
                    }
                }
                composable("Activity_Result"){
                    ActivityResultPage(navController = navController)
                }
            }
        }
    }

    private fun requestPermissions() {
        val permissionsToRequest = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsToRequest.toTypedArray(), requestCode)
        } else {
            Log.d("MainActivity", "Permissions already granted")
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == this.requestCode) {
            if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                startWorkoutService()
            } else {
                Log.e("MainActivity", "Permissions not granted")
            }
        }
    }

    private fun startWorkoutService() {
        val intent = Intent(this, WorkoutService::class.java)
        startService(intent)
    }
}




@Composable
inline fun <reified T : ViewModel> NavBackStackEntry.sharedViewModel(navController: NavController) : T{
    val navGraphRoute = destination.parent?.route ?: return viewModel()
    val parentEntry = remember(this){
        navController.getBackStackEntry(navGraphRoute)
    }
    return viewModel(parentEntry)
}

/*
val healthClient = HealthServices.getClient(this /*context*/)
val exerciseClient = healthClient.exerciseClient
lifecycleScope.launch {
    val capabilities = exerciseClient.getCapabilitiesAsync().await()
    if (ExerciseType.RUNNING in capabilities.supportedExerciseTypes) {
        val runningCapabilities =
            capabilities.getExerciseTypeCapabilities(ExerciseType.RUNNING)
    }
}
*/
