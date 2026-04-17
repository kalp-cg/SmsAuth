package com.kalpcg.pulserelay.helpers

import com.kalpcg.pulserelay.BuildConfig

object BuildHelper {
    val isInsecureVersion =
        BuildConfig.BUILD_TYPE == "insecure" || BuildConfig.BUILD_TYPE == "debugInsecure"
}