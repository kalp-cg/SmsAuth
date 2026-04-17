package com.kalpcg.pulserelay.modules.health.domain

data class HealthResult(
    val status: Status,
    val checks: Map<String, CheckResult>
)
