package com.example.footballstatistics_app_wearos.presentation.pages

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.ScalingLazyListState
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.Text
import com.example.footballstatistics_app_wearos.R
import com.example.footballstatistics_app_wearos.presentation.TransferDataService
import com.example.footballstatistics_app_wearos.presentation.black
import com.example.footballstatistics_app_wearos.presentation.theme.LeagueGothic
import com.example.footballstatistics_app_wearos.presentation.white
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.footballstatistics_app_wearos.presentation.blue
import com.example.footballstatistics_app_wearos.presentation.components.ChipButton

enum class TransferState {
    NOT_STARTED, IN_PROGRESS, COMPLETED, FAILED
}

@Composable
fun UploadPage(navController: NavController, listState: ScalingLazyListState = rememberScalingLazyListState()) {
    Log.d("Upload", "UploadPage")
    val context = LocalContext.current

    var transferState by remember { mutableStateOf(TransferState.NOT_STARTED) }
    var progress by remember { mutableIntStateOf(0) }

    // Create a BroadcastReceiver
    val transferReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                TransferDataService.TRANSFER_STARTED_ACTION -> {
                    Log.d("UploadPage", "Transfer started")
                    transferState = TransferState.IN_PROGRESS
                }

                TransferDataService.TRANSFER_IN_PROGRESS_ACTION -> {
                    val newProgress = intent.getIntExtra(TransferDataService.TRANSFER_PROGRESS_EXTRA, 0)
                    Log.d("UploadPage", "Transfer in progress. Progress: $newProgress%")
                    progress = newProgress
                }

                TransferDataService.TRANSFER_COMPLETE_ACTION -> {
                    Log.d("UploadPage", "Transfer complete")
                    transferState = TransferState.COMPLETED
                    navController.navigate("ActivityResult") {
                        popUpTo("Upload_Match"){
                            inclusive = true
                        }
                    }
                }
                TransferDataService.TRANSFER_FAILED_ACTION -> {
                    Log.d("UploadPage", "Transfer Failed")
                    transferState = TransferState.FAILED
                }
            }
        }
    }

    // Register the BroadcastReceiver
    DisposableEffect(Unit) {
        val filter = IntentFilter().apply {
            addAction(TransferDataService.TRANSFER_STARTED_ACTION)
            addAction(TransferDataService.TRANSFER_IN_PROGRESS_ACTION)
            addAction(TransferDataService.TRANSFER_COMPLETE_ACTION)
            addAction(TransferDataService.TRANSFER_FAILED_ACTION)
        }
        LocalBroadcastManager.getInstance(context).registerReceiver(transferReceiver, filter)

        onDispose {
            LocalBroadcastManager.getInstance(context).unregisterReceiver(transferReceiver)
        }
    }

    val serviceIntent = Intent(context, TransferDataService::class.java)
    context.startService(serviceIntent) // Start the service

    ScalingLazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(black)
            .padding(horizontal = 10.dp),
        state = listState,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        item{
            Spacer(modifier = Modifier.height(40.dp))
        }

        item {
            Image(
                painter = painterResource(id = R.drawable.logobig),
                contentDescription = "Logo big",
                modifier = Modifier
                    .width(100.dp)
                    .height(100.dp)
            )
        }

        item{
            Spacer(modifier = Modifier.height(20.dp))
        }

        item {
            when (transferState) {
                TransferState.NOT_STARTED -> {
                    Text(
                        text = "Transfer Not Started",
                        fontSize = 16.sp,
                        color = white,
                        fontFamily = LeagueGothic,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                TransferState.IN_PROGRESS -> {
                    Text(
                        text = "Transfer In Progress",
                        fontSize = 16.sp,
                        color = white,
                        fontFamily = LeagueGothic,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = "Progress: $progress%",
                        fontSize = 16.sp,
                        color = white,
                        fontFamily = LeagueGothic,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                TransferState.COMPLETED -> {
                    Text(
                        text = "Transfer Complete",
                        fontSize = 16.sp,
                        color = white,
                        fontFamily = LeagueGothic,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                TransferState.FAILED -> {
                    Text(
                        text = "Transfer Failed",
                        fontSize = 16.sp,
                        color = white,
                        fontFamily = LeagueGothic,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
        item {
            if (transferState == TransferState.COMPLETED) {
                Spacer(modifier = Modifier.height(20.dp))
                ChipButton(
                    text = "Go back",
                    onClick = {
                        navController.navigate("Result")
                    },
                    color = blue,
                    icon = R.drawable.complete,
                    navController = navController
                )
            }
        }
    }
}