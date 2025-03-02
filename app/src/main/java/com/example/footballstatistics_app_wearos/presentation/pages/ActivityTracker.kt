package com.example.footballstatistics_app_wearos.presentation.pages

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.health.services.client.ExerciseClient
import androidx.health.services.client.HealthServices
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.ChipDefaults.chipColors
import androidx.wear.compose.material.Text
import com.example.footballstatistics_app_wearos.R
import com.example.footballstatistics_app_wearos.presentation.StopWatchViewModel
import com.example.footballstatistics_app_wearos.presentation.TimerState
import com.example.footballstatistics_app_wearos.presentation.black
import com.example.footballstatistics_app_wearos.presentation.blue
import com.example.footballstatistics_app_wearos.presentation.components.ChipButton
import com.example.footballstatistics_app_wearos.presentation.data.CurrentMatch
import com.example.footballstatistics_app_wearos.presentation.data.matchDataStore
import com.example.footballstatistics_app_wearos.presentation.green
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.health.connect.datatypes.ExerciseRoute
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.add
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.health.services.client.data.DataType
import androidx.health.services.client.data.ExerciseCapabilities
import androidx.health.services.client.data.ExerciseEvent
import androidx.health.services.client.data.ExerciseState
import androidx.health.services.client.data.ExerciseUpdate
import androidx.health.services.client.endExercise
import com.example.footballstatistics_app_wearos.presentation.WorkoutService
import kotlin.text.toDouble

@OptIn(ExperimentalHorologistApi::class)
@Composable
fun ActivityTrackerPage(modifier: Modifier = Modifier, navController: NavController) {

    val columnState = rememberResponsiveColumnState(
        contentPadding = ScalingLazyColumnDefaults.padding(
            first = ScalingLazyColumnDefaults.ItemType.Text,
            last = ScalingLazyColumnDefaults.ItemType.Chip
        )
    )

    val currentMatch = CurrentMatch.match

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(key1 = Unit) {
        scope.launch {
            context.matchDataStore.data.collect {
                CurrentMatch.setMatch(it)
            }
        }
    }

    var showDialog by remember { mutableStateOf(false) }

    var iniTime = LocalTime.now()

    val today: LocalDate = LocalDate.now()
    val formatterDate = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    val formattedDate: String = today.format(formatterDate)

    var currentTime by remember { mutableStateOf(LocalTime.now()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(10000)
            currentTime = LocalTime.now()
        }
    }

    // Format the time
    val formatter = DateTimeFormatter.ofPattern("HH:mm")
    val formattedTime = currentTime.format(formatter)

    val formatedInitime = iniTime.format(formatter)

    val viewModel = viewModel<StopWatchViewModel>()
    val timerState by viewModel.timerState.collectAsStateWithLifecycle()
    val stopWatchText by viewModel.stopWatchText.collectAsStateWithLifecycle()



    ScalingLazyColumn(modifier = Modifier
        .fillMaxSize()
        .background(black)
        .padding(5.dp), columnState = columnState) {
        item {
            Text(
                text = formattedTime,
                fontFamily = LeagueGothic,
                fontSize = 28.sp,
                color = yellow
            )
        }
        item {
            Text(
                text = stopWatchText,
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
                    onClick = { showDialog = true },
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
                if (timerState != TimerState.PAUSED) {
                    Chip(
                        label = {
                            Text(
                                text = "",
                                fontFamily = LeagueGothic,
                                fontSize = 16.sp
                            )
                        },
                        onClick = { viewModel.toggleIsRunning() },
                        colors = chipColors(backgroundColor = blue, contentColor = black),
                        shape = RoundedCornerShape(25.dp),
                        modifier = Modifier
                            .width(50.dp)
                            .padding(bottom = 8.dp),
                        icon = {
                            Image(
                                painter = painterResource(id = R.drawable.pause),
                                contentDescription = "Pause Icon",
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    )
                } else {
                    Chip(
                        label = {
                            Text(
                                text = "",
                                fontFamily = LeagueGothic,
                                fontSize = 16.sp
                            )
                        },
                        onClick = { viewModel.toggleIsRunning()},
                        colors = chipColors(backgroundColor = green, contentColor = black),
                        shape = RoundedCornerShape(25.dp),
                        modifier = Modifier
                            .width(50.dp)
                            .padding(bottom = 8.dp),
                        icon = {
                            Image(
                                painter = painterResource(id = R.drawable.play),
                                contentDescription = "Play Icon",
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    )
                }
            }
        }
    }
    if(showDialog){
        Dialog(
            onDismissRequest = { showDialog = false },
            content = {
                ScalingLazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(black)
                        .padding(5.dp),
                    columnState = columnState,
                ) {
                    item {
                        ChipButton(
                            text = "End Match",
                            onClick = {

                                currentMatch?.date = formattedDate
                                currentMatch?.total_time= stopWatchText
                                currentMatch?.iniTime= formatedInitime
                                currentMatch?.endTime= formattedTime
                                currentMatch?.end_location= "Location"

                                scope.launch {
                                    context.matchDataStore.updateData {
                                        currentMatch!!
                                    }
                                }
                                val intent = Intent(context, WorkoutService::class.java)
                                context.stopService(intent)
                                viewModel.resetTimer()
                                navController.navigate("Activity_Result")
                                showDialog = false
                            },
                            color = red,
                            icon = R.drawable.stop,
                            navController = navController
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.size(8.dp))
                    }
                    item {
                        ChipButton(
                            text = "Continue Match",
                            onClick = { showDialog = false },
                            color = blue,
                            icon = R.drawable.play,
                            navController = navController
                        )
                    }
                }

            }
        )
    }
}