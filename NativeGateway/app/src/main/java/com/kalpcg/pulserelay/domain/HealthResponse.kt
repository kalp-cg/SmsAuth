package com.kalpcg.pulserelay.domain

import com.kalpcg.pulserelay.BuildConfig
import com.kalpcg.pulserelay.modules.health.domain.CheckResult
import com.kalpcg.pulserelay.modules.health.domain.HealthResult
import com.kalpcg.pulserelay.modules.health.domain.Status

class HealthResponse(
    healthResult: HealthResult,

    val version: String = BuildConfig.VERSION_NAME,
    val releaseId: Int = BuildConfig.VERSION_CODE,
) {
    val status: Status = healthResult.status
    val checks: Map<String, CheckResult> = healthResult.checks
}