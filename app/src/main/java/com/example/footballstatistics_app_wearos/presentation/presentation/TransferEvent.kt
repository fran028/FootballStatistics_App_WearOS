package com.example.footballstatistics_app_wearos.presentation.presentation

enum class TransferState {
    NOT_STARTED, IN_PROGRESS, COMPLETED, FAILED
}

data class TransferEvent(
    val state: TransferState,
    val progress: Int = 0
)