package com.example.footballstatistics_app_wearos.presentation.pages

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavController
import androidx.wear.compose.material.Text
import com.example.footballstatistics_app_wearos.R
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.layout.ScalingLazyColumn
import com.google.android.horologist.compose.layout.ScalingLazyColumnDefaults
import com.google.android.horologist.compose.layout.rememberResponsiveColumnState
import com.google.android.horologist.compose.material.Chip

@OptIn(ExperimentalHorologistApi::class)
@Composable
fun ActivityCalibratePage(modifier: Modifier = Modifier, navController: NavController, location: String) {

    val columnState = rememberResponsiveColumnState(
        contentPadding = ScalingLazyColumnDefaults.padding(
            first = ScalingLazyColumnDefaults.ItemType.Text,
            last = ScalingLazyColumnDefaults.ItemType.Chip
        )
    )

    ScalingLazyColumn(modifier = Modifier.fillMaxSize(), columnState = columnState) {
        item {
            Image(
                painter = painterResource(id = R.drawable.logo), // Replace with your image resource
                contentDescription = "App Logo",
                modifier = Modifier.fillMaxSize() // Adjust modifier as needed
            )
        }
        item {
            Chip("Set Location", onClick = {navController.navigate("Activity_SetUp")})
        }
        item {
            Text(text = location)
        }
    }
}