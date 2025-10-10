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

// UPDATED: Added a new state for clarity
enum class GpsStatus {
    ACQUIRING,          // Still waiting for an initial usable signal
    ACQUIRING_IMPROVING,// Have a usable signal, but trying for a better one
    ACCURATE,           // Signal is excellent
    UNAVAILABLE         // Permission denied or GPS is off
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

    val (permissionGranted, locationState) = rememberLocationState(context)
    val currentLocation by locationState
    var gpsStatus by remember { mutableStateOf(GpsStatus.UNAVAILABLE) }

    // NEW: Add state to track the best accuracy found so far and if we have a usable location
    var bestAccuracy by remember { mutableStateOf(Float.MAX_VALUE) }
    var hasAcquiredGoodEnoughLocation by remember { mutableStateOf(false) }

    // This derived state will automatically update when its dependencies change.
    val isButtonEnabled by remember {
        // Enable the button as soon as we have a "good enough" location
        derivedStateOf { permissionGranted.value && hasAcquiredGoodEnoughLocation }
    }
    val buttonColor = if (isButtonEnabled) yellow else gray

    // This effect manages GPS status based on location updates and permission changes.
    LaunchedEffect(currentLocation, permissionGranted.value) {
        if (!permissionGranted.value) {
            gpsStatus = GpsStatus.UNAVAILABLE
            return@LaunchedEffect
        }

        val locationData = currentLocation
        if (locationData == null) {
            gpsStatus = GpsStatus.ACQUIRING
            return@LaunchedEffect
        }

        // --- NEW: PROGRESSIVE ACCURACY LOGIC ---

        // Update the best accuracy seen so far
        if (locationData.hasAccuracy() && locationData.accuracy < bestAccuracy) {
            bestAccuracy = locationData.accuracy
        }

        // Define our accuracy tiers
        val excellentAccuracy = 8.0f // Target for a great signal
        val goodEnoughAccuracy = 20.0f // Acceptable accuracy to enable the button

        // 1. Check if we have achieved an EXCELLENT lock
        if (bestAccuracy < excellentAccuracy) {
            gpsStatus = GpsStatus.ACCURATE
            hasAcquiredGoodEnoughLocation = true // An excellent lock is also good enough
        }
        // 2. Else, check if we have at least a GOOD ENOUGH lock
        else if (bestAccuracy < goodEnoughAccuracy) {
            gpsStatus = GpsStatus.ACQUIRING_IMPROVING
            hasAcquiredGoodEnoughLocation = true
        }
        // 3. Otherwise, we are still waiting for any usable signal
        else {
            gpsStatus = GpsStatus.ACQUIRING
            hasAcquiredGoodEnoughLocation = false
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
            // --- IMPROVEMENT: Display more detailed and encouraging GPS status ---
            when (gpsStatus) {
                GpsStatus.ACQUIRING -> Text(
                    text = "Acquiring GPS Signal...\nTarget: <20m",
                    fontFamily = LeagueGothic,
                    fontSize = 14.sp,
                    color = white,
                    textAlign = TextAlign.Center
                )
                GpsStatus.ACQUIRING_IMPROVING -> Text(
                    // The button is enabled, but we let the user know it can get better
                    text = "Location Ready. Improving...\nAccuracy: ${bestAccuracy.toInt()}m",
                    fontFamily = LeagueGothic,
                    fontSize = 14.sp,
                    color = yellow, // Use yellow to indicate "good but not perfect"
                    textAlign = TextAlign.Center
                )
                GpsStatus.ACCURATE -> Text(
                    text = "GPS Signal Locked!\nAccuracy: ${bestAccuracy.toInt()}m",
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
