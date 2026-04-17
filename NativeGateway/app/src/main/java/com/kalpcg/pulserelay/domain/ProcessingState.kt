package com.kalpcg.pulserelay.domain

enum class ProcessingState {
    Pending,
    Processed,
    Sent,
    Delivered,
    Failed
}