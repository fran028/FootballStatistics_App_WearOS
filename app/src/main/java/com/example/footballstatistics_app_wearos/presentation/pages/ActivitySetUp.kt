package com.example.footballstatistics_app_wearos.presentation.pages

import android.content.Intent
import androidx.activity.result.launch
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.wear.compose.material.ChipDefaults.chipColors
import androidx.wear.compose.material.Text
import com.example.footballstatistics_app_wearos.R
import com.example.footballstatistics_app_wearos.presentation.MyExerciseService
import com.example.footballstatistics_app_wearos.presentation.black
import com.example.footballstatistics_app_wearos.presentation.blue
import com.example.footballstatistics_app_wearos.presentation.components.ChipButton
import com.example.footballstatistics_app_wearos.presentation.data.AppDatabase
import com.example.footballstatistics_app_wearos.presentation.data.MatchEntity
import com.example.footballstatistics_app_wearos.presentation.green
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

@OptIn(ExperimentalHorologistApi::class)
@Composable
fun ActivitySetUpPage(modifier: Modifier = Modifier, navController: NavController) {


    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val database = AppDatabase.getDatabase(context)

    var startMatchButton by remember { mutableStateOf(false) }
    var isThereAnyMatch by remember { mutableStateOf(false) }
    var isLocationSet by remember { mutableStateOf(false) }
    var isKickOffLocationSet by remember { mutableStateOf(false) }
    var isHomeCornerLocationSet by remember { mutableStateOf(false) }
    var isAwayCornerLocationSet by remember { mutableStateOf(false) }
    var matchId by remember { mutableStateOf(0) }
    val (currentLocation, hasLocationPermission) = rememberLocationState(context)


    LaunchedEffect(key1 = Unit) {
        scope.launch {
            isThereAnyMatch = database.matchDao().isThereAnyMatch()
            if (isThereAnyMatch) {
                isLocationSet = database.matchDao().isLocationSet()
                isKickOffLocationSet = database.matchDao().isKickoffSet()
                isHomeCornerLocationSet = database.matchDao().isHomeCornersSet()
                isAwayCornerLocationSet = database.matchDao().isAwayCornersSet()
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
    if (isThereAnyMatch) {
        if(isLocationSet){
            startMatchButton = true
        } else {
            startMatchButton = false
        }

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
                    text = "Away Corner",
                    onClick = { navController.navigate("Activity_Calibrate/Away Corner") },
                    color = blue,
                    icon = R.drawable.corner,
                    navController = navController,
                    filled = if (isAwayCornerLocationSet) false else true
                )
            }
            item {
                ChipButton(
                    text = "Home Corner",
                    onClick = { navController.navigate("Activity_Calibrate/Home Corner") },
                    color = blue,
                    icon = R.drawable.corner,
                    navController = navController,
                    filled = if (isHomeCornerLocationSet) false else true
                )
            }
            item {
                ChipButton(
                    text = "Kick Off",
                    onClick = { navController.navigate("Activity_Calibrate/Kick off") },
                    color = blue,
                    icon = R.drawable.kickoff,
                    navController = navController,
                    filled = if (isKickOffLocationSet) false else true
                )
            }
            item {
                Spacer(modifier = Modifier.height(2.dp))
            }
            item {
                ChipButton(
                    text = "Start Match",
                    onClick = {
                        if (currentLocation != null) {
                            scope.launch {
                                val currentMatch = database.matchDao().getMatchById(matchId)
                                if (currentMatch != null) {
                                    currentMatch?.start_location = "${currentLocation!!.latitude},${currentLocation!!.longitude}"
                                    database.matchDao().updateMatch(currentMatch!!)
                                }
                            }
                        }
                        navController.navigate("Countdown")},
                    color = green,
                    icon = R.drawable.soccer,
                    navController = navController,
                    disabled = startMatchButton
                )
            }
        }
    } else {
        navController.navigate("Menu")
    }
}