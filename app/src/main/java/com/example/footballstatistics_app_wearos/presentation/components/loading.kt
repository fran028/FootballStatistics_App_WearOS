package com.example.footballstatistics_app_wearos.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.ChipDefaults.chipColors
import androidx.wear.compose.material.Text
import com.example.footballstatistics_app_wearos.presentation.black
import com.example.footballstatistics_app_wearos.presentation.theme.LeagueGothic
import com.example.footballstatistics_app_wearos.presentation.white
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.material.Chip
import androidx.wear.compose.material.CircularProgressIndicator
import com.example.footballstatistics_app_wearos.R
import com.google.android.horologist.compose.layout.ScalingLazyColumn
import com.google.android.horologist.compose.layout.ScalingLazyColumnState

@OptIn(ExperimentalHorologistApi::class)
@Composable
fun LoadingScreen(columnState: ScalingLazyColumnState) {
    var bgcolor = black
    var fontColor = white
    var borderColor = white

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
            Chip(
                label = {
                    Text(
                        text = "Loading",
                        fontFamily = LeagueGothic,
                        fontSize = 20.sp,
                        modifier = Modifier.padding(0.dp)
                    )
                },
                onClick = { },
                enabled = false,
                colors = chipColors(backgroundColor = bgcolor, contentColor = fontColor),
                shape = RoundedCornerShape(25.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .border(
                        width = 2.dp,
                        color = borderColor,
                        shape = RoundedCornerShape(25.dp)
                    ),
                icon = {
                    Box(
                        modifier = Modifier
                            .height(40.dp)
                            .width(40.dp)
                            .background(borderColor, CircleShape)
                            .padding(end = 0.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .width(50.dp)
                                .height(50.dp),
                            indicatorColor = black
                        )
                    }
                },
                contentPadding = PaddingValues(0.dp),
            )
        }
    }
}