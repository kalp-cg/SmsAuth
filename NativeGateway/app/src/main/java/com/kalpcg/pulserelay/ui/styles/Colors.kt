package com.kalpcg.pulserelay.ui.styles

import android.graphics.Color

val com.kalpcg.pulserelay.domain.ProcessingState.color: Int
    get() = when (this) {
        com.kalpcg.pulserelay.domain.ProcessingState.Pending -> Color.parseColor("#FFBB86FC")
        com.kalpcg.pulserelay.domain.ProcessingState.Processed -> Color.parseColor("#FF6200EE")
        com.kalpcg.pulserelay.domain.ProcessingState.Sent -> Color.parseColor("#FF3700B3")
        com.kalpcg.pulserelay.domain.ProcessingState.Delivered -> Color.parseColor("#FF03DAC5")
        com.kalpcg.pulserelay.domain.ProcessingState.Failed -> Color.parseColor("#FF018786")
    }