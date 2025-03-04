package com.example.footballstatistics_app_wearos.presentation.pages

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.wear.compose.material.ChipDefaults.chipColors
import androidx.wear.compose.material.Text
import com.example.footballstatistics_app_wearos.R
import com.example.footballstatistics_app_wearos.presentation.StopWatchViewModel
import com.example.footballstatistics_app_wearos.presentation.TimerState
import com.example.footballstatistics_app_wearos.presentation.black
import com.example.footballstatistics_app_wearos.presentation.blue
import com.example.footballstatistics_app_wearos.presentation.components.ChipButton
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.compose.runtime.DisposableEffect
import androidx.health.services.client.impl.ipc.internal.ServiceConnection
import com.example.footballstatistics_app_wearos.presentation.MyExerciseService
import com.example.footballstatistics_app_wearos.presentation.data.AppDatabase
import com.example.footballstatistics_app_wearos.presentation.rememberLocationState

@OptIn(ExperimentalHorologistApi::class)
@Composable
fun ActivityTrackerPage(modifier: Modifier = Modifier, navController: NavController) {

    val columnState = rememberResponsiveColumnState(
        contentPadding = ScalingLazyColumnDefaults.padding(
            first = ScalingLazyColumnDefaults.ItemType.Text,
            last = ScalingLazyColumnDefaults.ItemType.Chip
        )
    )

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val database = AppDatabase.getDatabase(context)

    var isThereAnyMatch by remember { mutableStateOf(false) }
    var matchId by remember { mutableStateOf(0) }
    val (currentLocation, hasLocationPermission) = rememberLocationState(context)
    var isServiceRunning by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = Unit) {
        scope.launch {
            isThereAnyMatch = database.matchDao().isThereAnyMatch()
            if (isThereAnyMatch) {
                matchId = database.matchDao().getMatchId()
            }
        }
    }

    LaunchedEffect(key1 = true) {
        if (!isServiceRunning) {
            val startIntent = Intent(context, MyExerciseService::class.java)
            context.startForegroundService(startIntent)
            isServiceRunning = true
            Log.d("ActivityTrackerPage", "Service Executed")
        }
    }


    var showDialog by remember { mutableStateOf(false) }

    val iniTime = LocalTime.now()

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
                    onClick = {
                        val intent = Intent(context, MyExerciseService::class.java)
                        Log.d("ActivityTrackerPage", "Stop button clicked")
                        intent.action = MyExerciseService.ACTION_PAUSE
                        context.startService(intent)
                        showDialog = true
                    },
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
                        onClick = {
                            viewModel.toggleIsRunning()
                            val intent = Intent(context, MyExerciseService::class.java)
                            Log.d("ActivityTrackerPage", "Pause button clicked")
                            intent.action = MyExerciseService.ACTION_PAUSE
                            context.startForegroundService(intent) },
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
                        onClick = {
                            viewModel.toggleIsRunning()
                            val intent = Intent(context, MyExerciseService::class.java)
                            Log.d("ActivityTrackerPage", "Resume button clicked")
                            intent.action = MyExerciseService.ACTION_RESUME
                            context.startService(intent)
                        },
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
                                Log.d("ActivityTrackerPage", "End button clicked")
                                val serviceIntent = Intent(context, MyExerciseService::class.java)
                                context.stopService(serviceIntent)

                                scope.launch {
                                    val currentMatch = database.matchDao().getMatchById(matchId)
                                    val currentCoordinates = "${currentLocation?.latitude}, ${currentLocation?.longitude}"
                                    if (currentMatch != null) {
                                        currentMatch.date = formattedDate
                                        currentMatch.total_time = stopWatchText
                                        currentMatch.iniTime = formatedInitime
                                        currentMatch.endTime = formattedTime
                                        currentMatch.end_location = currentCoordinates

                                        database.matchDao().updateMatch(currentMatch)
                                    }
                                }
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
                            onClick = {
                                val intent = Intent(context, MyExerciseService::class.java)
                                intent.action = MyExerciseService.ACTION_RESUME
                                context.startService(intent)
                                showDialog = false
                            },
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