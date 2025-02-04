package com.example.footballstatistics_app_wearos.presentation

import android.os.Bundle
import android.view.Menu
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.health.services.client.HealthServices
import androidx.health.services.client.data.ExerciseType
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavHost
import androidx.navigation.navigation
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.ScalingLazyColumnDefaults
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import androidx.wear.tooling.preview.devices.WearDevices
import com.example.footballstatistics_app_wearos.R
import com.example.footballstatistics_app_wearos.presentation.pages.ActivityCalibratePage
import com.example.footballstatistics_app_wearos.presentation.pages.ActivityResultPage
import com.example.footballstatistics_app_wearos.presentation.pages.ActivitySetUpPage
import com.example.footballstatistics_app_wearos.presentation.pages.ActivityTrackerPage
import com.example.footballstatistics_app_wearos.presentation.pages.MenuPage
import com.example.footballstatistics_app_wearos.presentation.theme.FootballStatistics_App_WearOSTheme
import kotlinx.serialization.Serializable


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        super.onCreate(savedInstanceState)

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
}




@Composable
inline fun <reified T : ViewModel> NavBackStackEntry.sharedViewModel(navController: NavController) : T{
    val navGraphRoute = destination.parent?.route ?: return viewModel()
    val parentEntry = remember(this){
        navController.getBackStackEntry(navGraphRoute)
    }
    return viewModel(parentEntry)
}


val healthClient = HealthServices.getClient(this /*context*/)
val exerciseClient = healthClient.exerciseClient
lifecycleScope.launch {
    val capabilities = exerciseClient.getCapabilitiesAsync().await()
    if (ExerciseType.RUNNING in capabilities.supportedExerciseTypes) {
        val runningCapabilities =
            capabilities.getExerciseTypeCapabilities(ExerciseType.RUNNING)
    }
}

