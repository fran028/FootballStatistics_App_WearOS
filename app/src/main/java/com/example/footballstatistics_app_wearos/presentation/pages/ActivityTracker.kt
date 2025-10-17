package com.example.footballstatistics_app_wearos.presentation.pages

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
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
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
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
import com.example.footballstatistics_app_wearos.presentation.MyExerciseService
import com.example.footballstatistics_app_wearos.presentation.TimerState
import com.example.footballstatistics_app_wearos.presentation.components.ChipButton
import com.example.footballstatistics_app_wearos.presentation.data.AppDatabase
import com.example.footballstatistics_app_wearos.presentation.presentation.StopWatchViewModel
import com.example.footballstatistics_app_wearos.presentation.rememberLocationState
import com.example.footballstatistics_app_wearos.presentation.theme.LeagueGothic
import com.example.footballstatistics_app_wearos.presentation.black
import com.example.footballstatistics_app_wearos.presentation.blue
import com.example.footballstatistics_app_wearos.presentation.green
import com.example.footballstatistics_app_wearos.presentation.red
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

    val (hasLocationPermission, locationState) = rememberLocationState(context)
    val currentLocation by locationState

    var showDialog by remember { mutableStateOf(false) }

    val today: LocalDate = LocalDate.now()
    val formatterDate = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    val formattedDate: String = today.format(formatterDate)

    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    // --- FIX: Change startTime to a var ---
    var startTime by remember { mutableStateOf("") }

    var currentTime by remember { mutableStateOf(LocalTime.now()) }
    // This LaunchedEffect is for the live clock display
    LaunchedEffect(Unit) {
        while (true) {
            delay(10000)
            currentTime = LocalTime.now()
        }
    }
    val formattedTime = currentTime.format(timeFormatter)

    val viewModel = viewModel<StopWatchViewModel>()
    val timerState by viewModel.timerState.collectAsStateWithLifecycle()
    val stopWatchText by viewModel.stopWatchText.collectAsStateWithLifecycle()

    // --- FIX: Capture the startTime when the ViewModel is ready ---
    LaunchedEffect(key1 = viewModel) {
        startTime = LocalTime.now().format(timeFormatter)
    }

    // --- SERVICE MANAGEMENT LOGIC ---
    var exerciseService by remember { mutableStateOf<MyExerciseService?>(null) }
    val connection = remember {
        object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                exerciseService = (service as MyExerciseService.LocalBinder).getService()
                Log.d("ActivityTrackerPage", "Service connected")
            }
            override fun onServiceDisconnected(name: ComponentName?) {
                exerciseService = null
                Log.d("ActivityTrackerPage", "Service disconnected")
            }
        }
    }

    DisposableEffect(Unit) {
        Intent(context, MyExerciseService::class.java).also { intent ->
            context.startForegroundService(intent)
            context.bindService(intent, connection, Context.BIND_AUTO_CREATE)
            Log.d("ActivityTrackerPage", "Service bound")
        }
        onDispose {
            context.unbindService(connection)
            Log.d("ActivityTrackerPage", "Service unbound")
        }
    }
    // --- END OF SERVICE MANAGEMENT LOGIC ---

    LaunchedEffect(key1 = Unit) {
        scope.launch {
            database.matchDao().isThereAnyMatch()?.let {
                isThereAnyMatch = it
                if (isThereAnyMatch) {
                    matchId = database.matchDao().getMatchId()
                }
            }
        }
    }

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
            Row(modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly) {

                Chip(
                    label = { Text(text = "", fontFamily = LeagueGothic, fontSize = 16.sp) },
                    onClick = { showDialog = true },
                    colors = chipColors(backgroundColor = red, contentColor = black),
                    shape = RoundedCornerShape(25.dp),
                    modifier = Modifier
                        .width(50.dp)
                        .padding(bottom = 8.dp),
                    icon = { Image(painter = painterResource(id = R.drawable.stop), contentDescription = "Stop Icon", modifier = Modifier.size(24.dp)) }
                )
                if (timerState != TimerState.PAUSED) {
                    Chip(
                        label = { Text(text = "", fontFamily = LeagueGothic, fontSize = 16.sp) },
                        onClick = {
                            viewModel.toggleIsRunning()
                            val intent = Intent(context, MyExerciseService::class.java)
                            Log.d("ActivityTrackerPage", "Pause button clicked")
                            intent.action = MyExerciseService.ACTION_PAUSE
                            context.startService(intent)
                        },
                        colors = chipColors(backgroundColor = blue, contentColor = black),
                        shape = RoundedCornerShape(25.dp),
                        modifier = Modifier
                            .width(50.dp)
                            .padding(bottom = 8.dp),
                        icon = { Image(painter = painterResource(id = R.drawable.pause), contentDescription = "Pause Icon", modifier = Modifier.size(24.dp)) }
                    )
                } else {
                    Chip(
                        label = { Text(text = "", fontFamily = LeagueGothic, fontSize = 16.sp) },
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
                        icon = { Image(painter = painterResource(id = R.drawable.play), contentDescription = "Play Icon", modifier = Modifier.size(24.dp)) }
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
                                Log.d("ActivityTrackerPage", "End button clicked from dialog")
                                scope.launch {
                                    // --- FIX: Save data BEFORE you navigate ---

                                    val currentMatch = database.matchDao().getMatchById(matchId)
                                    if (currentMatch != null) {
                                        // --- NEW: Capture end time and update all fields ---
                                        val endTime = LocalTime.now().format(timeFormatter)

                                        currentMatch.date = formattedDate
                                        currentMatch.total_time = stopWatchText
                                        currentMatch.iniTime = startTime
                                        currentMatch.endTime = endTime

                                        Log.d("ActivityTrackerPage", "Saving match data for match ID: $matchId...")
                                        database.matchDao().updateMatch(currentMatch) // Assuming your update function is named `update`
                                        Log.d("ActivityTrackerPage", "Match data saved.")
                                    } else {
                                        Log.e("ActivityTrackerPage", "Could not find match with ID $matchId to save.")
                                    }

                                    exerciseService?.stopExercise()
                                    navController.navigate("Activity_Result")
                                }
                            },
                            color = red,
                            icon = R.drawable.stop,
                            filled = false,
                            navController = navController
                        )
                    }
                    item {
                        Spacer(Modifier.size(8.dp))
                        ChipButton(
                            text = "Cancel",
                            onClick = { showDialog = false },
                            color = blue,
                            icon = R.drawable.play,
                            filled = false,
                            navController = navController
                        )
                    }
                }
            }
        )
    }
}
