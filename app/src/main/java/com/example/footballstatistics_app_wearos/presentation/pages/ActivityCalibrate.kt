package com.example.footballstatistics_app_wearos.presentation.pages

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.Text
import com.example.footballstatistics_app_wearos.R
import com.example.footballstatistics_app_wearos.presentation.components.ChipButton
import com.example.footballstatistics_app_wearos.presentation.data.AppDatabase
import com.example.footballstatistics_app_wearos.presentation.rememberLocationState
import com.example.footballstatistics_app_wearos.presentation.theme.LeagueGothic
import com.example.footballstatistics_app_wearos.presentation.black
import com.example.footballstatistics_app_wearos.presentation.gray
import com.example.footballstatistics_app_wearos.presentation.green
import com.example.footballstatistics_app_wearos.presentation.red
import com.example.footballstatistics_app_wearos.presentation.white
import com.example.footballstatistics_app_wearos.presentation.yellow
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class GpsStatus {
    ACQUIRING, // Still waiting for a good signal
    ACCURATE,  // Signal is good enough
    UNAVAILABLE // Permission denied or GPS is off
}

@OptIn(ExperimentalHorologistApi::class)
@Composable
fun ActivityCalibratePage(modifier: Modifier = Modifier, navController: NavController, location: String) {
    Log.d("ActivityCalibrate", "ActivityCalibratePage for: $location")

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val database = AppDatabase.getDatabase(context)

    var isThereAnyMatch by remember { mutableStateOf(false) }
    var matchId by remember { mutableStateOf(0) }

    // permissionGranted is now a State<Boolean>
    val (permissionGranted, locationState) = rememberLocationState(context)
    var currentLocation by locationState
    var gpsStatus by remember { mutableStateOf(GpsStatus.UNAVAILABLE) }

    // --- NEW: Add a counter to track the number of received locations ---
    var receivedLocationCount by remember { mutableStateOf(0) }

    // This derived state will automatically update when its dependencies change.
    val isButtonEnabled by remember {
        // Read the .value of the state inside derivedStateOf
        derivedStateOf { permissionGranted.value && gpsStatus == GpsStatus.ACCURATE }
    }
    val buttonColor = if (isButtonEnabled) yellow else gray

    // This effect manages GPS status based on location updates and permission changes.
    LaunchedEffect(currentLocation, permissionGranted.value) {
        if (!permissionGranted.value) { // Read the .value here
            gpsStatus = GpsStatus.UNAVAILABLE
            return@LaunchedEffect
        }

        val locationData = currentLocation
        if (locationData == null) {
            gpsStatus = GpsStatus.ACQUIRING
            return@LaunchedEffect
        }

        // --- IMPROVEMENT: Stricter filtering logic ---
        receivedLocationCount++ // Increment for each new location
        val requiredAccuracy = 15.0f // Require accuracy better than 15 meters

        // Check if accuracy is good enough
        if (locationData.hasAccuracy() && locationData.accuracy < requiredAccuracy) {
            // AND wait for a few readings to ensure the GPS has stabilized
            if (receivedLocationCount > 3) {
                gpsStatus = GpsStatus.ACCURATE
            } else {
                // We have a good reading, but wait for more to be sure.
                gpsStatus = GpsStatus.ACQUIRING
            }
        } else {
            // Accuracy is not good enough, keep acquiring.
            gpsStatus = GpsStatus.ACQUIRING
        }
    }

    LaunchedEffect(Unit) {
        scope.launch {
            delay(500) // Give a moment for DB to initialize
            database.matchDao().isThereAnyMatch()?.let {
                isThereAnyMatch = it
                if (isThereAnyMatch) {
                    matchId = database.matchDao().getMatchId()
                }
            }
        }
    }

    val listState = rememberScalingLazyListState()

    ScalingLazyColumn(
        state = listState,
        modifier = modifier
            .fillMaxSize()
            .background(black),
        contentPadding = PaddingValues(
            top = 32.dp,
            start = 8.dp,
            end = 8.dp,
            bottom = 32.dp
        ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Image(
                painter = painterResource(id = R.drawable.logobig),
                contentDescription = "App Logo",
                modifier = Modifier
                    .width(30.dp)
                    .height(30.dp)
            )
        }
        item {
            // --- IMPROVEMENT: Display more detailed GPS status to the user ---
            when (gpsStatus) {
                GpsStatus.ACQUIRING -> Text(
                    text = "Improving GPS accuracy...\nTarget: <15m, Current: ${currentLocation?.accuracy?.toInt() ?: "N/A"}m",
                    fontFamily = LeagueGothic,
                    fontSize = 14.sp,
                    color = white,
                    textAlign = TextAlign.Center
                )
                GpsStatus.ACCURATE -> Text(
                    text = "GPS Signal Locked!\nAccuracy: ${currentLocation?.accuracy?.toInt() ?: "N/A"}m",
                    fontFamily = LeagueGothic,
                    fontSize = 14.sp,
                    color = green,
                    textAlign = TextAlign.Center
                )
                GpsStatus.UNAVAILABLE -> Text(
                    text = "Location permission required.",
                    fontFamily = LeagueGothic,
                    fontSize = 14.sp,
                    color = red,
                    textAlign = TextAlign.Center
                )
            }
        }
        item {
            ChipButton(
                text = "Set Location",
                onClick = {
                    if (isButtonEnabled) {
                        currentLocation?.let { loc ->
                            val currentCoordinates = "${loc.latitude}, ${loc.longitude}"
                            scope.launch {
                                val currentMatch = database.matchDao().getMatchById(matchId)
                                if (currentMatch != null) {
                                    when (location) {
                                        "Home Corner" -> currentMatch.home_corner_location = currentCoordinates
                                        "Away Corner" -> currentMatch.away_corner_location = currentCoordinates
                                        "Kick off" -> currentMatch.kickoff_location = currentCoordinates
                                    }
                                    database.matchDao().updateMatch(currentMatch)
                                }
                            }
                            navController.navigate("Activity_SetUp")
                        }
                    }
                },
                color = buttonColor,
                icon = R.drawable.location,
                navController = navController,
                disabled = !isButtonEnabled
            )
        }
        item {
            Text(
                text = location,
                fontFamily = LeagueGothic,
                fontSize = 24.sp,
                color = white
            )
        }
    }
}
