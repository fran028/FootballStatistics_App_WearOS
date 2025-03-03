package com.example.footballstatistics_app_wearos.presentation

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
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


class MainActivity : ComponentActivity() {

    private val permissions = arrayOf(
        Manifest.permission.BODY_SENSORS,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACTIVITY_RECOGNITION,
        Manifest.permission.FOREGROUND_SERVICE
    )

    private val permissionRequestCode = 123

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setTheme(android.R.style.Theme_DeviceDefault)

        requestPermissions()

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
        }.toTypedArray()

        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsToRequest, permissionRequestCode)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == permissionRequestCode) {
            if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                // Permissions granted, proceed with your logic
            } else {
                // Permissions denied, handle accordingly
            }
        }
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
