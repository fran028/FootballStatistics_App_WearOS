package com.example.footballstatistics_app_wearos.presentation

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.core.content.ContextCompat
import com.google.android.gms.location.Granularity
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

/**
 * A composable function that remembers the location state, handling permission requests
 * and location updates.
 *
 * @param context The current context.
 * @return A Pair containing a State<Boolean> for permission status and a MutableState<Location?>
 * for the current location.
 */
@SuppressLint("MissingPermission")
@Composable
fun rememberLocationState(context: Context): Pair<State<Boolean>, MutableState<Location?>> {
    // This holds the permission status as a trackable State object.
    val permissionGrantedState: MutableState<Boolean> = remember { mutableStateOf(false) }

    val locationState: MutableState<Location?> = remember { mutableStateOf(null) }

    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    val locationCallback = remember {
        object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                // Update the state with the most recent location from the result.
                locationState.value = locationResult.lastLocation
            }
        }
    }

    // This launcher requests the ACCESS_FINE_LOCATION permission.
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted: Boolean ->
            permissionGrantedState.value = isGranted // Update the state based on user's choice.
        }
    )

    // This effect starts when permission is granted and stops when it's revoked or the
    // composable leaves the screen.
    DisposableEffect(permissionGrantedState.value) {
        if (permissionGrantedState.value) {
            // Permission is granted, so request location updates.
            val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000)
                .setWaitForAccurateLocation(true)
                .setMinUpdateIntervalMillis(1000)
                .setMaxUpdateDelayMillis(2000)
                .setGranularity(Granularity.GRANULARITY_FINE)
                .build()

            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        }

        onDispose {
            // Clean up and remove location updates when the effect is disposed.
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }

    // This effect checks the initial permission status when the composable is first launched.
    LaunchedEffect(launcher) {
        val fineLocationPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        if (fineLocationPermission == PackageManager.PERMISSION_GRANTED) {
            permissionGrantedState.value = true // Permission already granted.
        } else {
            launcher.launch(Manifest.permission.ACCESS_FINE_LOCATION) // Request permission.
        }
    }

    // Return the State objects themselves, not their values.
    return permissionGrantedState to locationState
}
