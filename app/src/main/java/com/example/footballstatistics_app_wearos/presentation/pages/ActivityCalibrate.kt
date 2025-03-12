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
import androidx.compose.runtime.LaunchedEffect
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
import androidx.navigation.NavController
import androidx.wear.compose.material.Text
import com.example.footballstatistics_app_wearos.R
import com.example.footballstatistics_app_wearos.presentation.black
import com.example.footballstatistics_app_wearos.presentation.components.ChipButton
import com.example.footballstatistics_app_wearos.presentation.data.AppDatabase
import com.example.footballstatistics_app_wearos.presentation.gray
import com.example.footballstatistics_app_wearos.presentation.rememberLocationState
import com.example.footballstatistics_app_wearos.presentation.theme.LeagueGothic
import com.example.footballstatistics_app_wearos.presentation.white
import com.example.footballstatistics_app_wearos.presentation.yellow
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.layout.ScalingLazyColumn
import com.google.android.horologist.compose.layout.ScalingLazyColumnDefaults
import com.google.android.horologist.compose.layout.rememberResponsiveColumnState
import kotlinx.coroutines.launch

@OptIn(ExperimentalHorologistApi::class)
@Composable
fun ActivityCalibratePage(modifier: Modifier = Modifier, navController: NavController, location: String) {
    Log.d("ActivityCalibrate", "ActivityCalibratePage")

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val database = AppDatabase.getDatabase(context)

    var isThereAnyMatch by remember { mutableStateOf(false) }
    var matchId by remember { mutableStateOf(0) }
    val (currentLocation, hasLocationPermission) = rememberLocationState(context)
    Log.d("ActivityCalibrate", "Location: $currentLocation")
    var locationCheck by remember { mutableStateOf(false) }
    if(currentLocation != null){
        locationCheck = true
    }
    val buttonColor = if (locationCheck) yellow else gray
    LaunchedEffect(key1 = Unit) {
        scope.launch {
            isThereAnyMatch = database.matchDao().isThereAnyMatch()
            if (isThereAnyMatch) {
                matchId = database.matchDao().getMatchId()
            }
        }
    }

    val columnState = rememberResponsiveColumnState(
        contentPadding = ScalingLazyColumnDefaults.padding(
            first = ScalingLazyColumnDefaults.ItemType.Text,
            last = ScalingLazyColumnDefaults.ItemType.Chip
        )
    )

    ScalingLazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(black)
            .padding(5.dp),
        columnState = columnState
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
            Spacer(modifier = Modifier.height(8.dp))
        }
        item {
            if (currentLocation != null) {
                Text(
                    text = "${currentLocation.latitude}, ${currentLocation.longitude}",
                    fontFamily = LeagueGothic,
                    fontSize = 8.sp,
                    color = white
                )
            } else {
                Text(
                    text = "Location not available",
                    fontFamily = LeagueGothic,
                    fontSize = 8.sp,
                    color = white
                )
            }
        }
        item {
            ChipButton(
                text = "Set Location",
                onClick = {
                    if(locationCheck){
                        val currentCoordinates = "${currentLocation!!.latitude}, ${currentLocation.longitude}"
                        scope.launch {
                            val currentMatch = database.matchDao().getMatchById(matchId)
                            if (currentMatch != null) {
                                when (location) {
                                    "Home Corner" -> {
                                        currentMatch.home_corner_location = currentCoordinates
                                        database.matchDao().updateMatch(currentMatch)
                                    }
                                    "Away Corner" -> {
                                        currentMatch.away_corner_location = currentCoordinates
                                        database.matchDao().updateMatch(currentMatch)
                                    }
                                    "Kick off" -> {
                                        currentMatch.kickoff_location = currentCoordinates
                                        database.matchDao().updateMatch(currentMatch)
                                    }
                                }
                            }
                        }
                        navController.navigate("Activity_SetUp")
                    }
                },
                color = buttonColor,
                icon = R.drawable.location,
                navController = navController,
                disabled = !hasLocationPermission || currentLocation == null
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