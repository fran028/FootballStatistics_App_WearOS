package com.example.footballstatistics_app_wearos.presentation

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.runtime.Composable
import androidx.core.content.ContextCompat
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
import com.example.footballstatistics_app_wearos.presentation.pages.UploadPage


class MainActivity : ComponentActivity() {

    private val permissions = arrayOf(
        Manifest.permission.BODY_SENSORS,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACTIVITY_RECOGNITION,
        Manifest.permission.FOREGROUND_SERVICE
    )

    private val permissionRequestCode = 123
    private var permissionsGranted = false

    private lateinit var requestPermissionLauncher: ActivityResultLauncher<Array<String>>

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setTheme(android.R.style.Theme_DeviceDefault)

        requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            permissionsGranted = permissions.all { it.value }
            setContent {
                if (permissionsGranted) {
                    MainScreen()
                }
            }
        }
        permissionsGranted = checkPermissions()
        if (!permissionsGranted) {
            requestPermissions()
        } else {
            setContent {
                MainScreen()
            }
        }
    }

    private fun checkPermissions(): Boolean {
        return permissions.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestPermissions() {
        val permissionsToRequest = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }.toTypedArray()
        if (permissionsToRequest.isNotEmpty()) {
            requestPermissionLauncher.launch(permissionsToRequest)
        }
    }


}

@Composable
fun MainScreen() {
    val navController = rememberSwipeDismissableNavController()
    SwipeDismissableNavHost(
        navController = navController,
        startDestination = "Menu"
    ) {
        composable("Menu") {
            MenuPage(navController = navController)
        }
        navigation(
            startDestination = "Activity_SetUp",
            route = "Activity"
        ) {
            composable("Activity_SetUp") {
                ActivitySetUpPage(navController = navController)
            }
            composable("Activity_Calibrate/{location}") {
                val location = it.arguments?.getString("location")
                ActivityCalibratePage(
                    navController = navController,
                    location = location.toString()
                )
            }
            composable("Countdown") {
                CountdownPage(navController = navController)
            }
            composable("Activity_Tracker") {
                ActivityTrackerPage(navController = navController)
            }
        }
        navigation(
            startDestination = "Activity_Result",
            route = "Result"
        ) {
            composable("Activity_Result") {
                ActivityResultPage(navController = navController)
            }
            composable("Upload_Match") {
                UploadPage(navController = navController)
            }
        }
    }
}
