package com.example.footballstatistics_app_wearos.presentation.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class UploadViewModel : ViewModel() {

    private val _transferEvent = MutableStateFlow(TransferEvent(TransferState.NOT_STARTED, 0))
    val transferEvent: StateFlow<TransferEvent> = _transferEvent.asStateFlow()

    fun sendTransferEvent(event: TransferEvent) {
        _transferEvent.value = event
        Log.d("Upload", "UploadPage")
    }
    fun getTransferState(): TransferState {
        return transferEvent.value.state
    }
    fun getTransferProgress(): Int {
        return transferEvent.value.progress
    }
}