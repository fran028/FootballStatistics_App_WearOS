package com.example.footballstatistics_app_wearos.presentation.pages

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.runtime.LaunchedEffect
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
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.ScalingLazyListState
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.Text
import com.example.footballstatistics_app_wearos.R
import com.example.footballstatistics_app_wearos.presentation.MyApplication
import com.example.footballstatistics_app_wearos.presentation.TransferDataService
import com.example.footballstatistics_app_wearos.presentation.black
import com.example.footballstatistics_app_wearos.presentation.blue
import com.example.footballstatistics_app_wearos.presentation.components.ChipButton
import com.example.footballstatistics_app_wearos.presentation.green
import com.example.footballstatistics_app_wearos.presentation.theme.LeagueGothic
import com.example.footballstatistics_app_wearos.presentation.presentation.TransferEvent
import com.example.footballstatistics_app_wearos.presentation.presentation.TransferState
import com.example.footballstatistics_app_wearos.presentation.presentation.UploadViewModel
import com.example.footballstatistics_app_wearos.presentation.red
import com.example.footballstatistics_app_wearos.presentation.white
import com.example.footballstatistics_app_wearos.presentation.yellow

@Composable
fun UploadPage(
    navController: NavController,
    listState: ScalingLazyListState = rememberScalingLazyListState()
) {
    Log.d("Upload", "UploadPage")
    val context = LocalContext.current
    val container = (context.applicationContext as MyApplication).container
    val viewModel: UploadViewModel = container.uploadViewModel

    var transferState by remember { mutableStateOf(TransferState.NOT_STARTED) }
    var progress by remember { mutableIntStateOf(0) }

    var hasBluetoothConnectPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) Manifest.permission.BLUETOOTH_CONNECT else Manifest.permission.BLUETOOTH
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    var hasBluetoothScanPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) Manifest.permission.BLUETOOTH_SCAN else Manifest.permission.BLUETOOTH
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    var hasForegroundServicePermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.FOREGROUND_SERVICE
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val bluetoothConnectPermissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            hasBluetoothConnectPermission = isGranted
        }
    val bluetoothScanPermissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            hasBluetoothScanPermission = isGranted
        }
    val foregroundServicePermissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            hasForegroundServicePermission = isGranted
        }
    LaunchedEffect(key1 = Unit) {
        if (!hasBluetoothConnectPermission) {
            bluetoothConnectPermissionLauncher.launch(if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) Manifest.permission.BLUETOOTH_CONNECT else Manifest.permission.BLUETOOTH)
        }
        if (!hasBluetoothScanPermission) {
            bluetoothScanPermissionLauncher.launch(if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) Manifest.permission.BLUETOOTH_SCAN else Manifest.permission.BLUETOOTH)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && !hasForegroundServicePermission) {
            foregroundServicePermissionLauncher.launch(Manifest.permission.FOREGROUND_SERVICE)
        }
        if (hasBluetoothConnectPermission && hasBluetoothScanPermission) {
            val serviceIntent = Intent(context, TransferDataService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }
        }
    }

    LaunchedEffect(viewModel.transferEvents) {
        viewModel.transferEvents.collect { event ->
            transferState = event.state
            progress = event.progress
        }
    }
    ScalingLazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(black)
            .padding(horizontal = 10.dp),
        state = listState,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        item {
            Spacer(modifier = Modifier.height(40.dp))
        }

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
            Spacer(modifier = Modifier.height(20.dp))
        }

        item {
            when (transferState) {
                TransferState.NOT_STARTED -> {
                    ChipButton(
                        text = "Transfer Not Started",
                        color = white,
                        onClick = { },
                        navController = navController,
                        icon = R.drawable.uploading
                    )
                }

                TransferState.IN_PROGRESS -> {
                    ChipButton(
                        text = "Transfer In Progress: $progress%",
                        color = blue,
                        onClick = {
                        },
                        navController = navController,
                        icon = R.drawable.uploading
                    )
                }

                TransferState.COMPLETED -> {
                    ChipButton(
                        text = "Transfer Complete",
                        color = green,
                        onClick = {  },
                        navController = navController,
                        icon = R.drawable.complete
                    )
                }

                TransferState.FAILED -> {
                    ChipButton(
                        text = "Transfer Failed",
                        color = red,
                        onClick = {
                        },
                        navController = navController,
                        icon = R.drawable.uploading
                    )
                }
            }
        }
        if(transferState == TransferState.COMPLETED){
            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
            item{
            ChipButton(
                text = "Back to Menu",
                color = yellow,
                onClick = { navController.navigate("Menu") },
                navController = navController,
                icon = R.drawable.left
            )
            }
        }
    }
}