package com.example.footballstatistics_app_wearos.presentation.pages

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.ChipDefaults.chipColors
import androidx.wear.compose.material.Text
import com.example.footballstatistics_app_wearos.R
import com.example.footballstatistics_app_wearos.presentation.black
import com.example.footballstatistics_app_wearos.presentation.blue
import com.example.footballstatistics_app_wearos.presentation.components.ChipButton
import com.example.footballstatistics_app_wearos.presentation.gray
import com.example.footballstatistics_app_wearos.presentation.green
import com.example.footballstatistics_app_wearos.presentation.red
import com.example.footballstatistics_app_wearos.presentation.theme.LeagueGothic
import com.example.footballstatistics_app_wearos.presentation.white
import com.example.footballstatistics_app_wearos.presentation.yellow
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.layout.ScalingLazyColumn
import com.google.android.horologist.compose.layout.ScalingLazyColumnDefaults
import com.google.android.horologist.compose.layout.rememberResponsiveColumnState
import com.google.android.horologist.compose.material.Chip

@OptIn(ExperimentalHorologistApi::class)
@Composable
fun ActivityResultPage(modifier: Modifier = Modifier, navController: NavController) {

    val columnState = rememberResponsiveColumnState(
        contentPadding = ScalingLazyColumnDefaults.padding(
            first = ScalingLazyColumnDefaults.ItemType.Text,
            last = ScalingLazyColumnDefaults.ItemType.Chip
        )
    )

    val iniTime = "00:00"
    val endTime = "00:00"
    val timer = "00:00:00"

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
        item{
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Match Result ",
                    fontFamily = LeagueGothic,
                    color = white,
                    fontSize = 24.sp
                )
                Text(
                    text = "$iniTime - $endTime",
                    fontFamily = LeagueGothic,
                    color = yellow,
                    fontSize = 16.sp,
                )
            }
        }
        item{
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Match Result ",
                    fontFamily = LeagueGothic,
                    color = white,
                    fontSize = 32.sp
                )
                Text(
                    text = "$iniTime - $endTime",
                    fontFamily = LeagueGothic,
                    color = gray,
                    fontSize = 16.sp,
                )
            }
        }
        item {
            ChipButton(
                text = "Upload Match",
                onClick = {navController.navigate("Menu")},
                color = blue,
                icon = R.drawable.uploading,
                navController =  navController
            )
        }
        item {
            ChipButton(
                text = "Delete Match",
                onClick = {navController.navigate("Menu")},
                color = red,
                icon = R.drawable.trash,
                navController =  navController
            )
        }
    }
}