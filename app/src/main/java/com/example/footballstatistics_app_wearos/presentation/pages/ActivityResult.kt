package com.example.footballstatistics_app_wearos.presentation.pages

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.wear.compose.material.Text
import com.example.footballstatistics_app_wearos.R
import com.example.footballstatistics_app_wearos.presentation.black
import com.example.footballstatistics_app_wearos.presentation.blue
import com.example.footballstatistics_app_wearos.presentation.components.ChipButton
import com.example.footballstatistics_app_wearos.presentation.components.LoadingScreen
import com.example.footballstatistics_app_wearos.presentation.data.AppDatabase
import com.example.footballstatistics_app_wearos.presentation.data.MatchEntity
import com.example.footballstatistics_app_wearos.presentation.gray
import com.example.footballstatistics_app_wearos.presentation.presentation.ActivityResultViewModel
import com.example.footballstatistics_app_wearos.presentation.presentation.ActivityResultViewModelFactory
import com.example.footballstatistics_app_wearos.presentation.red
import com.example.footballstatistics_app_wearos.presentation.theme.LeagueGothic
import com.example.footballstatistics_app_wearos.presentation.white
import com.example.footballstatistics_app_wearos.presentation.yellow
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.layout.ScalingLazyColumn
import com.google.android.horologist.compose.layout.ScalingLazyColumnDefaults
import com.google.android.horologist.compose.layout.rememberResponsiveColumnState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


@OptIn(ExperimentalHorologistApi::class)
@Composable
fun ActivityResultPage(modifier: Modifier = Modifier, navController: NavController) {

    val columnState = rememberResponsiveColumnState(
        contentPadding = ScalingLazyColumnDefaults.padding(
            first = ScalingLazyColumnDefaults.ItemType.Text,
            last = ScalingLazyColumnDefaults.ItemType.Chip
        )
    )

    var showDeleteDialog by remember { mutableStateOf(false) }
    var showUploadDialog by remember { mutableStateOf(false) }
    var matchData by remember { mutableStateOf(null as MatchEntity?) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val database = AppDatabase.getDatabase(context)
    val viewModel: ActivityResultViewModel = viewModel(factory = ActivityResultViewModelFactory(database))

    if (viewModel.isLoading) {
        Log.d("ActivitySetUpPage", "Loading...")
        LoadingScreen(columnState)
    } else {
        if(viewModel.currentMatch == null ){
            navController.navigate("Menu")
        }
        matchData = viewModel.currentMatch

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
            val iniTime = matchData?.iniTime
            val endTime = matchData?.endTime
            val timer = matchData?.total_time
            item {
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
            item {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = (timer) ?: "0",
                        fontFamily = LeagueGothic,
                        color = white,
                        fontSize = 40.sp
                    )
                    Text(
                        text = "Activity Time",
                        fontFamily = LeagueGothic,
                        color = gray,
                        fontSize = 16.sp,
                    )
                }
            }
            item {
                Spacer(modifier = Modifier.height(8.dp))
            }
            item {
                ChipButton(
                    text = "Upload Match",
                    onClick = {  showUploadDialog = true},
                    color = blue,
                    icon = R.drawable.uploading,
                    navController = navController
                )
            }
            item {
                Spacer(modifier = Modifier.height(4.dp))
            }
            item {
                ChipButton(
                    text = "Delete Match",
                    onClick = { showDeleteDialog = true },
                    color = red,
                    icon = R.drawable.trash,
                    navController = navController
                )
            }
            item {
                Spacer(modifier = Modifier.height(4.dp))
            }
            item {
                ChipButton(
                    text = "Back to Menu",
                    onClick = { navController.navigate("Menu") },
                    color = yellow,
                    icon = R.drawable.left,
                    navController = navController
                )
            }

        }
        if (showDeleteDialog) {
            Dialog(
                onDismissRequest = { showDeleteDialog = false },
                content = {
                    ScalingLazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(black)
                            .padding(5.dp),
                        columnState = columnState
                    ) {
                        item {
                            ChipButton(
                                text = "Delete",
                                onClick = {
                                    scope.launch {
                                        try {
                                            withContext(Dispatchers.IO) {
                                                val matchesInMemory = database.matchDao().getAllMatches()
                                                for (match in matchesInMemory) {
                                                    database.matchDao().deleteMatchById(match.id)
                                                    database.locationDataDao().deleteLocationDataByMatchId(match.id)
                                                }
                                                Log.d("MenuPage", "Match deleted")
                                            }
                                            Log.d("MenuPage", "Navigate to Menu")
                                            showDeleteDialog = false
                                            navController.navigate("Menu")

                                        } catch (e: Exception) {
                                            Log.e("MenuPage", "Error when deleteing match", e)
                                        }
                                    }
                                },
                                color = red,
                                icon = R.drawable.trash,
                                navController = navController
                            )
                        }
                        item {
                            Spacer(modifier = Modifier.size(8.dp))
                        }
                        item {
                            ChipButton(
                                text = "Cancel",
                                onClick = { showDeleteDialog = false },
                                color = yellow,
                                icon = R.drawable.left,
                                navController = navController
                            )
                        }
                    }

                }
            )
        }
        if (showUploadDialog) {
            Dialog(
                onDismissRequest = { showUploadDialog = false },
                content = {
                    ScalingLazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(black)
                            .padding(5.dp),
                        columnState = columnState
                    ) {
                        item {
                            ChipButton(
                                text = "Upload",
                                onClick = {
                                    navController.navigate("Upload_Match")
                                    showUploadDialog = false
                                },
                                color = blue,
                                icon = R.drawable.uploading,
                                navController = navController
                            )
                        }
                        item {
                            Spacer(modifier = Modifier.size(8.dp))
                        }
                        item {
                            ChipButton(
                                text = "Cancel",
                                onClick = { showUploadDialog = false },
                                color = yellow,
                                icon = R.drawable.left,
                                navController = navController
                            )
                        }
                    }

                }
            )
        }
    }
}