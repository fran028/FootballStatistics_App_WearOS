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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.ScalingLazyListState
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import com.example.footballstatistics_app_wearos.R
import com.example.footballstatistics_app_wearos.presentation.FootballStatisticsApplication
import com.example.footballstatistics_app_wearos.presentation.TransferDataService
import com.example.footballstatistics_app_wearos.presentation.black
import com.example.footballstatistics_app_wearos.presentation.blue
import com.example.footballstatistics_app_wearos.presentation.components.ChipButton
import com.example.footballstatistics_app_wearos.presentation.green
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
    Log.d("Upload", "UploadPage Composed")
    val context = LocalContext.current
    val container = (context.applicationContext as FootballStatisticsApplication).container
    val viewModel: UploadViewModel = container.uploadViewModel

    val transferEvent by viewModel.transferEvent.collectAsStateWithLifecycle()
    val transferState = transferEvent.state
    val progress = transferEvent.progress

    // --- SOLUTION: Add POST_NOTIFICATIONS to the list of required permissions ---
    val requiredPermissions = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Android 13+
            listOf(
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.FOREGROUND_SERVICE,
                Manifest.permission.POST_NOTIFICATIONS // Add this permission
            )
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) { // Android 12
            listOf(
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.FOREGROUND_SERVICE
            )
        } else { // Older versions
            listOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.FOREGROUND_SERVICE
            )
        }
    }

    var hasAllPermissions by remember {
        mutableStateOf(
            requiredPermissions.all {
                ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
            }
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissionsMap ->
        hasAllPermissions = permissionsMap.values.all { it }
        if (hasAllPermissions) {
            Log.d("Upload", "All required permissions granted.")
        } else {
            Log.e("Upload", "Not all permissions were granted. Data transfer may fail.")
        }
    }

    // This effect runs once to check/request permissions.
    LaunchedEffect(key1 = Unit) {
        if (!hasAllPermissions) {
            Log.d("Upload", "Requesting permissions: $requiredPermissions")
            permissionLauncher.launch(requiredPermissions.toTypedArray())
        }
    }

    // This effect starts the service *only if* permissions are granted.
    LaunchedEffect(key1 = hasAllPermissions) {
        if (hasAllPermissions && transferState == TransferState.NOT_STARTED) {
            Log.d("Upload", "Permissions are granted. Starting TransferDataService.")
            val serviceIntent = Intent(context, TransferDataService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }
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
                painter = painterResource(id = R.drawable.logobig),
                contentDescription = "App Logo",
                modifier = Modifier
                    .width(30.dp)
                    .height(30.dp)
            )
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }

        item {
            // UI is now fully reactive to changes in transferState and progress
            when (transferState) {
                TransferState.NOT_STARTED -> {
                    ChipButton(
                        text = if (!hasAllPermissions) "Grant Permissions" else "Preparing Transfer...",
                        color = white,
                        onClick = {
                            // If permissions were denied, allow the user to try again
                            if (!hasAllPermissions) {
                                permissionLauncher.launch(requiredPermissions.toTypedArray())
                            }
                        },
                        navController = navController,
                        icon = R.drawable.uploading
                    )
                }
                // ... (rest of your UI code remains the same)
                TransferState.IN_PROGRESS -> {
                    ChipButton(
                        text = "Transfer In Progress: $progress%",
                        color = blue,
                        onClick = {},
                        navController = navController,
                        icon = R.drawable.uploading
                    )
                }

                TransferState.COMPLETED -> {
                    ChipButton(
                        text = "Transfer Complete",
                        color = green,
                        onClick = { },
                        navController = navController,
                        icon = R.drawable.complete
                    )
                }

                TransferState.FAILED -> {
                    ChipButton(
                        text = "Transfer Failed",
                        color = red,
                        onClick = {
                            // Allow user to retry starting the service if it failed
                            if (hasAllPermissions) {
                                Log.d("Upload", "Retry: Starting TransferDataService.")
                                val serviceIntent = Intent(context, TransferDataService::class.java)
                                context.startService(serviceIntent)
                            } else {
                                // Prompt for permissions again if they are the cause of failure
                                permissionLauncher.launch(requiredPermissions.toTypedArray())
                            }
                        },
                        navController = navController,
                        icon = R.drawable.uploading
                    )
                }
            }
        }
        if (transferState == TransferState.COMPLETED) {
            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
            item {
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
