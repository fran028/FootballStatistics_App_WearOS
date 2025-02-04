package com.example.footballstatistics_app_wearos.presentation.pages

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
import androidx.compose.ui.Modifier
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
import com.example.footballstatistics_app_wearos.presentation.green
import com.example.footballstatistics_app_wearos.presentation.theme.LeagueGothic
import com.example.footballstatistics_app_wearos.presentation.white
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.layout.ScalingLazyColumn
import com.google.android.horologist.compose.layout.ScalingLazyColumnDefaults
import com.google.android.horologist.compose.layout.rememberResponsiveColumnState
import com.google.android.horologist.compose.material.Chip

@OptIn(ExperimentalHorologistApi::class)
@Composable
fun ActivitySetUpPage(modifier: Modifier = Modifier, navController: NavController) {

    val columnState = rememberResponsiveColumnState(
        contentPadding = ScalingLazyColumnDefaults.padding(
            first = ScalingLazyColumnDefaults.ItemType.Text,
            last = ScalingLazyColumnDefaults.ItemType.Chip
        )
    )

    ScalingLazyColumn(modifier = Modifier.fillMaxSize()
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
                text = "Away Corner",
                onClick = {navController.navigate("Activity_Calibrate/Away Corner")},
                color = blue,
                icon = R.drawable.corner,
                navController =  navController
            )
        }
        item {
            ChipButton(
                text = "Home Corner",
                onClick = {navController.navigate("Activity_Calibrate/Home Corner")},
                color = blue,
                icon = R.drawable.corner,
                navController =  navController
            )
        }
        item {
            ChipButton(
                text = "Kick Off",
                onClick = {navController.navigate("Activity_Calibrate/Kick off")},
                color = blue,
                icon = R.drawable.kickoff,
                navController =  navController
            )
        }
        item {
            Spacer(modifier = Modifier.height(2.dp))
        }
        item {
            ChipButton(
                text = "Start Match",
                onClick = {navController.navigate("Activity_Tracker")},
                color = green,
                icon = R.drawable.soccer,
                navController =  navController
            )
        }
    }
}