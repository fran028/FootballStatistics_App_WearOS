package com.example.footballstatistics_app_wearos.presentation.pages

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.wear.compose.material.Text
import com.example.footballstatistics_app_wearos.R
import com.example.footballstatistics_app_wearos.presentation.presentation.ActivitySetUpViewModel
import com.example.footballstatistics_app_wearos.presentation.black
import com.example.footballstatistics_app_wearos.presentation.blue
import com.example.footballstatistics_app_wearos.presentation.components.ChipButton
import com.example.footballstatistics_app_wearos.presentation.components.LoadingScreen
import com.example.footballstatistics_app_wearos.presentation.data.AppDatabase
import com.example.footballstatistics_app_wearos.presentation.green
import com.example.footballstatistics_app_wearos.presentation.presentation.ActivitySetUpViewModelFactory
import com.example.footballstatistics_app_wearos.presentation.rememberLocationState
import com.example.footballstatistics_app_wearos.presentation.theme.LeagueGothic
import com.example.footballstatistics_app_wearos.presentation.white
import com.example.footballstatistics_app_wearos.presentation.yellow
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.layout.ScalingLazyColumn
import com.google.android.horologist.compose.layout.ScalingLazyColumnDefaults
import com.google.android.horologist.compose.layout.rememberResponsiveColumnState
import com.google.android.horologist.compose.material.Chip
import kotlinx.coroutines.launch
import org.w3c.dom.Text
import java.util.Date

@OptIn(ExperimentalHorologistApi::class)
@Composable
fun ActivitySetUpPage(modifier: Modifier = Modifier, navController: NavController) {

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val database = AppDatabase.getDatabase(context)
    val viewModel: ActivitySetUpViewModel = viewModel(factory = ActivitySetUpViewModelFactory(database))

    var startMatchButton by remember { mutableStateOf(false) }
    val (currentLocation, hasLocationPermission) = rememberLocationState(context)

    val columnState = rememberResponsiveColumnState(
        contentPadding = ScalingLazyColumnDefaults.padding(
            first = ScalingLazyColumnDefaults.ItemType.Text,
            last = ScalingLazyColumnDefaults.ItemType.Chip
        )
    )
    if (viewModel.isLoading) {
        Log.d("ActivitySetUpPage", "Loading...")
        LoadingScreen(columnState)
    } else {
        Log.d("ActivitySetUpPage", "Match found with ID: ${viewModel.matchId}")
        Log.d("ActivitySetUpPage", "Location set: ${viewModel.isLocationSet}")

        ScalingLazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(black)
                .padding(5.dp), columnState = columnState
        ) {
            item {
                Image(
                    painter = painterResource(id = R.drawable.logobig), // Replace with your image resource
                    contentDescription = "App Logo",
                    modifier = Modifier
                        .width(30.dp)
                        .height(30.dp)// Adjust modifier as needed
                )
            }
            item {
                Spacer(modifier = Modifier.height(8.dp))
            }
            item {
                ChipButton(
                    text = "Start Match",
                    onClick = {
                        if (currentLocation != null) {
                            scope.launch {
                                val currentMatch = database.matchDao().getMatchById(viewModel.matchId)
                                if (currentMatch != null) {
                                    currentMatch.start_location = "${currentLocation.latitude},${currentLocation.longitude}"
                                    currentMatch.iniTime = Date().toString()
                                    database.matchDao().updateMatch(currentMatch)
                                }
                            }
                        }
                        navController.navigate("Countdown")},
                    color = green,
                    icon = R.drawable.soccer,
                    navController = navController,
                    disabled = !viewModel.isLocationSet
                )
            }
            item {
                Spacer(modifier = Modifier.height(2.dp))
            }
            item {
                if(!viewModel.isLocationSet) {
                    Text(
                        text = "To start first set opposite corners and center location",
                        fontFamily = LeagueGothic,
                        fontSize = 16.sp,
                        color = white
                    )
                } else {
                    Text(
                        text = "Locations Set",
                        fontFamily = LeagueGothic,
                        fontSize = 24.sp,
                        color = green
                    )
                }
            }
            item{
                Spacer(modifier = Modifier.height(2.dp))
            }
            item {
                ChipButton(
                    text = "Away Corner",
                    onClick = { navController.navigate("Activity_Calibrate/Away Corner") },
                    color = blue,
                    icon = R.drawable.corner,
                    navController = navController,
                    filled = viewModel.isAwayCornerLocationSet
                )
            }
            item {
                ChipButton(
                    text = "Home Corner",
                    onClick = { navController.navigate("Activity_Calibrate/Home Corner") },
                    color = blue,
                    icon = R.drawable.corner,
                    navController = navController,
                    filled = viewModel.isHomeCornerLocationSet
                )
            }
            item {
                ChipButton(
                    text = "Kick Off",
                    onClick = { navController.navigate("Activity_Calibrate/Kick off") },
                    color = blue,
                    icon = R.drawable.kickoff,
                    navController = navController,
                    filled = viewModel.isKickOffLocationSet
                )
            }

        }
    }
}