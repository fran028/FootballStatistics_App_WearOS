package com.example.footballstatistics_app_wearos.presentation.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class UploadViewModel : ViewModel() {

    private val _transferEvents = MutableSharedFlow<TransferEvent>()
    val transferEvents = _transferEvents.asSharedFlow()

    fun sendTransferEvent(event: TransferEvent) {
        viewModelScope.launch {
            _transferEvents.emit(event)
        }
    }
}