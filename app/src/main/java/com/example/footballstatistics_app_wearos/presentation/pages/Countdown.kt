package com.example.footballstatistics_app_wearos.presentation.pages

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.footballstatistics_app_wearos.R
import com.example.footballstatistics_app_wearos.presentation.black
import com.example.footballstatistics_app_wearos.presentation.green
import com.example.footballstatistics_app_wearos.presentation.red
import com.example.footballstatistics_app_wearos.presentation.theme.LeagueGothic
import com.example.footballstatistics_app_wearos.presentation.white
import com.example.footballstatistics_app_wearos.presentation.yellow
import kotlinx.coroutines.delay

@Composable
fun CountdownPage(navController: NavController) {
    var countdownValue by remember { mutableStateOf(3) }
    var color by remember { mutableStateOf(green) }
    LaunchedEffect(Unit) {
        while (countdownValue > 0) {
            when (countdownValue) {
                3 -> color = red
                2 -> color = yellow
                1 -> color = green
                else -> color = green
            }
            delay(1000)
            countdownValue--
        }
        navController.navigate("Activity_Tracker")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(black),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .border(8.dp, color, CircleShape)
                .background(black, CircleShape)
                .padding(32.dp),

            contentAlignment = Alignment.Center
        ) {
            if (countdownValue <= 0){
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .background(color, CircleShape)
                        .padding(16.dp),

                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.whistle),
                        contentDescription = null,
                        modifier = Modifier.size(50.dp)
                    )
                }
            } else {
                Text(
                    text = countdownValue.toString(),
                    fontFamily = LeagueGothic,
                    fontSize = 64.sp,
                    color = color
                )
            }
        }
    }
}