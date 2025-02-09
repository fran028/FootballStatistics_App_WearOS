package com.example.footballstatistics_app_wearos.presentation.pages

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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.wear.compose.material.ChipDefaults.chipColors
import androidx.wear.compose.material.Text
import com.example.footballstatistics_app_wearos.R
import com.example.footballstatistics_app_wearos.presentation.black
import com.example.footballstatistics_app_wearos.presentation.blue
import com.example.footballstatistics_app_wearos.presentation.components.ChipButton
import com.example.footballstatistics_app_wearos.presentation.data.CurrentMatch
import com.example.footballstatistics_app_wearos.presentation.data.matchDataStore
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
fun ActivityCalibratePage(modifier: Modifier = Modifier, navController: NavController, location: String) {

    val currentMatch = CurrentMatch.match

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val columnState = rememberResponsiveColumnState(
        contentPadding = ScalingLazyColumnDefaults.padding(
            first = ScalingLazyColumnDefaults.ItemType.Text,
            last = ScalingLazyColumnDefaults.ItemType.Chip
        )
    )

    LaunchedEffect(key1 = Unit) {
        scope.launch {
            context.matchDataStore.data.collect {
                CurrentMatch.setMatch(it)
            }
        }
    }

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
            Spacer(modifier = Modifier.height(8.dp))
        }
        item {
            ChipButton(
                text = "Set Location",
                onClick = {
                    when(location){
                        "Home Corner" -> {
                            currentMatch?.home_corner_location= "coordinates"
                        }
                        "Away Corner" -> {
                            currentMatch?.away_corner_location = "coordinates"
                        }
                        "Kick off" -> {
                            currentMatch?.kickoff_location = "coordinates"
                        }
                    }
                    scope.launch {
                        context.matchDataStore.updateData {
                            currentMatch!!
                        }
                    }
                    navController.navigate("Activity_SetUp")
                    },
                color = yellow,
                icon = R.drawable.location,
                navController =  navController
            )
        }
        item {
            Text(
                text = location,
                fontFamily = LeagueGothic,
                fontSize = 24.sp,
            )
        }
    }
}