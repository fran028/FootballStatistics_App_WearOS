package com.example.footballstatistics_app_wearos.presentation.pages

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
import androidx.navigation.NavController
import com.example.footballstatistics_app_wearos.R
import com.example.footballstatistics_app_wearos.presentation.black
import com.example.footballstatistics_app_wearos.presentation.components.ChipButton
import com.example.footballstatistics_app_wearos.presentation.data.CurrentMatch
import com.example.footballstatistics_app_wearos.presentation.data.Match
import com.example.footballstatistics_app_wearos.presentation.data.matchDataStore
import com.example.footballstatistics_app_wearos.presentation.green
import com.example.footballstatistics_app_wearos.presentation.yellow
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.layout.ScalingLazyColumn
import com.google.android.horologist.compose.layout.ScalingLazyColumnDefaults
import com.google.android.horologist.compose.layout.rememberResponsiveColumnState
import kotlinx.coroutines.launch

@OptIn(ExperimentalHorologistApi::class)
@Composable
fun MenuPage(modifier: Modifier = Modifier, navController: NavController) {

    var createdMatch by remember { mutableStateOf<Match?>(null) }

    val currentMatch = CurrentMatch.match

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(key1 = Unit) {
        scope.launch {
            context.matchDataStore.data.collect {
                CurrentMatch.setMatch(it)
            }
        }
    }

    val columnState = rememberResponsiveColumnState(
        contentPadding = ScalingLazyColumnDefaults.padding(
            first = ScalingLazyColumnDefaults.ItemType.Text,
            last = ScalingLazyColumnDefaults.ItemType.Chip
        )
    )

    ScalingLazyColumn(modifier = Modifier
        .fillMaxSize()
        .background(black)
        .padding(5.dp), columnState = columnState) {
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
            Spacer(modifier = Modifier.height(10.dp))
        }
        item {
            ChipButton(
                text = "Start Match",
                onClick = {
                    createdMatch = Match(
                        date = "00:00",
                        total_time = "00:00",
                        away_corner_location = "",
                        home_corner_location = "",
                        kickoff_location = "",
                        start_location = "",
                        matchStatus = "Not Started",
                        activityData = "",
                        iniTime = "",
                        endTime = "",
                        end_location = "",
                    )
                    CurrentMatch.setMatch(createdMatch!!)
                    scope.launch {
                        context.matchDataStore.updateData {
                            createdMatch!!
                        }
                    }
                    navController.navigate("Activity") },
                color = green,
                icon = R.drawable.soccer,
                navController =  navController
            )
        }
        if(currentMatch != null){
            item {
                Spacer(modifier = Modifier.height(5.dp))
            }
            item {
                ChipButton(text = "Last Match", onClick = {navController.navigate("Activity_Result")}, color = yellow, icon = R.drawable.strategy, navController =  navController)
            }
        }
    }
}
