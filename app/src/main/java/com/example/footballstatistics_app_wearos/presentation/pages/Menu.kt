package com.example.footballstatistics_app_wearos.presentation.pages

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentDataType.Companion.Date
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.wear.compose.material.ChipDefaults.chipColors
import androidx.wear.compose.material.Text
import com.example.footballstatistics_app_wearos.R
import com.example.footballstatistics_app_wearos.presentation.Match
import com.example.footballstatistics_app_wearos.presentation.black
import com.example.footballstatistics_app_wearos.presentation.components.ChipButton
import com.example.footballstatistics_app_wearos.presentation.green
import com.example.footballstatistics_app_wearos.presentation.theme.LeagueGothic
import com.example.footballstatistics_app_wearos.presentation.theme.fontFamily
import com.example.footballstatistics_app_wearos.presentation.white
import com.example.footballstatistics_app_wearos.presentation.yellow
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.layout.ScalingLazyColumn
import com.google.android.horologist.compose.layout.ScalingLazyColumnDefaults
import com.google.android.horologist.compose.layout.rememberResponsiveColumnState
import com.google.android.horologist.compose.material.Chip
import com.google.android.horologist.compose.material.ListHeaderDefaults.firstItemPadding
import com.google.android.horologist.compose.material.ResponsiveListHeader
import java.util.Date

@OptIn(ExperimentalHorologistApi::class)
@Composable
fun MenuPage(modifier: Modifier = Modifier, navController: NavController) {

    /*val match1 = Match(
        date = Date(),
        start_time = Time,
        end_time = TODO(),
        total_time = TODO(),
        match_id = TODO(),
        away_corner_location = TODO(),
        home_corner_location = TODO(),
        kickoff_location = TODO(),
        start_location = TODO(),
        matchStatus = TODO(),
        activityData = TODO(),


        /*val start_time: String,
        val end_time: String,
        var total_time: String,
        val match_id: String,
        var away_corner_location: String,
        var home_corner_location: String,
        var kickoff_location: String,
        val start_location: String,
        var matchStatus: String = "Not Started",

        val activityData: String*/
    )*/

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

                    navController.navigate("Activity") },
                color = green,
                icon = R.drawable.soccer,
                navController =  navController
            )
        }
        item {
            Spacer(modifier = Modifier.height(5.dp))
        }
        item {
            ChipButton(text = "Last Match", onClick = {navController.navigate("Activity_Result")}, color = yellow, icon = R.drawable.strategy, navController =  navController)
        }
    }
}
