package com.example.footballstatistics_app_wearos.presentation.pages

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import com.example.footballstatistics_app_wearos.presentation.red
import com.example.footballstatistics_app_wearos.presentation.theme.LeagueGothic
import com.example.footballstatistics_app_wearos.presentation.theme.fontFamily
import com.example.footballstatistics_app_wearos.presentation.white
import com.example.footballstatistics_app_wearos.presentation.yellow
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.layout.ScalingLazyColumn
import com.google.android.horologist.compose.layout.ScalingLazyColumnDefaults
import com.google.android.horologist.compose.layout.rememberResponsiveColumnState
import com.google.android.horologist.compose.material.Chip

@OptIn(ExperimentalHorologistApi::class)
@Composable
fun ActivityTrackerPage(modifier: Modifier = Modifier, navController: NavController) {

    val columnState = rememberResponsiveColumnState(
        contentPadding = ScalingLazyColumnDefaults.padding(
            first = ScalingLazyColumnDefaults.ItemType.Text,
            last = ScalingLazyColumnDefaults.ItemType.Chip
        )
    )

    val timer = "00:00:00"
    val time = "00:00"

    ScalingLazyColumn(modifier = Modifier.fillMaxSize()
        .background(black)
        .padding(5.dp), columnState = columnState) {
        item {
            Text(
                text = time,
                fontFamily = LeagueGothic,
                fontSize = 28.sp,
                color = yellow
            )
        }
        item {
            Text(
                text = timer,
                fontFamily = LeagueGothic,
                fontSize = 40.sp,
                color = white
            )
        }
        item {
            //Chip("Stop", onClick = {navController.navigate("Activity_Result")})
            Row(modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly) {

                Chip(
                    label = {
                        Text(
                            text = "",
                            fontFamily = LeagueGothic,
                            fontSize = 16.sp
                        )
                    },
                    onClick = { navController.navigate("Activity_Result") },
                    colors = chipColors(backgroundColor = red, contentColor = black),
                    shape = RoundedCornerShape(25.dp),
                    modifier = Modifier
                        .width(50.dp)
                        .padding(bottom = 8.dp),
                    icon = {
                        Image(
                            painter = painterResource(id = R.drawable.stop),
                            contentDescription = "Stop Icon",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                )
                Chip(
                    label = {
                        Text(
                            text = "",
                            fontFamily = LeagueGothic,
                            fontSize = 16.sp
                        )
                    },
                    onClick = { navController.navigate("Activity_Result") },
                    colors = chipColors(backgroundColor = blue, contentColor = black),
                    shape = RoundedCornerShape(25.dp),
                    modifier = Modifier
                        .width(50.dp)
                        .padding(bottom = 8.dp),
                    icon = {
                        Image(
                            painter = painterResource(id = R.drawable.pause),
                            contentDescription = "Stop Icon",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                )
            }
        }
    }
}