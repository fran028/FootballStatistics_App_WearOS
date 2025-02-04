package com.example.footballstatistics_app_wearos.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.wear.compose.material.ChipDefaults.chipColors
import androidx.wear.compose.material.Text
import com.example.footballstatistics_app_wearos.R
import com.example.footballstatistics_app_wearos.presentation.black
import com.example.footballstatistics_app_wearos.presentation.red
import com.example.footballstatistics_app_wearos.presentation.theme.LeagueGothic
import com.example.footballstatistics_app_wearos.presentation.white
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.material.Chip

@OptIn(ExperimentalHorologistApi::class)
@Composable
fun ChipButton(text: String, onClick: () -> Unit, color: Color, icon: Int, navController: NavController){
    Chip(
        label = {
            Text(
                text = text,
                fontFamily = LeagueGothic,
                fontSize = 20.sp
            )
        },
        onClick =  onClick ,
        colors = chipColors(backgroundColor = black, contentColor = white),
        shape = RoundedCornerShape(25.dp),
        modifier = Modifier
            .fillMaxSize()
            .border(
                width = 2.dp,
                color = color,
                shape = RoundedCornerShape(25.dp)
            )
            .padding(0.dp),
        icon = {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(color, CircleShape),
                contentAlignment = Alignment.Center) {
                Image(
                    painter = painterResource(id = icon),
                    contentDescription = "Stop Icon",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    )
}