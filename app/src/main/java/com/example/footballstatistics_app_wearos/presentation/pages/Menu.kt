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
import androidx.navigation.NavController
import com.example.footballstatistics_app_wearos.R
import com.example.footballstatistics_app_wearos.presentation.black
import com.example.footballstatistics_app_wearos.presentation.components.ChipButton
import com.example.footballstatistics_app_wearos.presentation.data.AppDatabase
import com.example.footballstatistics_app_wearos.presentation.data.MatchEntity
import com.example.footballstatistics_app_wearos.presentation.green
import com.example.footballstatistics_app_wearos.presentation.yellow
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.layout.ScalingLazyColumn
import com.google.android.horologist.compose.layout.ScalingLazyColumnDefaults
import com.google.android.horologist.compose.layout.rememberResponsiveColumnState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalHorologistApi::class)
@Composable
fun MenuPage(modifier: Modifier = Modifier, navController: NavController) {

    var isThereAnyMatch by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val database = AppDatabase.getDatabase(context)

    LaunchedEffect(key1 = Unit) {
        scope.launch {
            isThereAnyMatch = database.matchDao().isThereAnyMatch()
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
                    scope.launch {
                        try {
                            withContext(Dispatchers.IO) {
                                val matchesInMemory = database.matchDao().getAllMatches()
                                for (match in matchesInMemory) {
                                    database.matchDao().deleteMatchById(match.id)
                                    database.locationDataDao().deleteLocationDataByMatchId(match.id)
                                }
                                val newMatch = MatchEntity(
                                    date = "",
                                    total_time = "",
                                    iniTime = "",
                                    endTime = "",
                                    away_corner_location = "",
                                    home_corner_location = "",
                                    kickoff_location = "",
                                    start_location = "",
                                    end_location = "",
                                    matchStatus = "Not Started",
                                )
                                database.matchDao().insertMatch(newMatch)
                                Log.d("MenuPage", "Match created")
                            }
                            Log.d("MenuPage", "Navigate to Activity")
                            navController.navigate("Activity")
                        } catch (e: Exception) {
                            Log.e("MenuPage", "Error when starting match", e)
                        }
                    }
                },
                color = green,
                icon = R.drawable.soccer,
                navController =  navController
            )
        }
        item {
            Spacer(modifier = Modifier.height(5.dp))
        }
        item {
            ChipButton(
                text = "Last Match",
                onClick = {navController.navigate("Result")},
                color = yellow,
                icon = R.drawable.strategy,
                navController =  navController,
                disabled = !isThereAnyMatch)
        }
}
}
