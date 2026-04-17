package com.kalpcg.pulserelay.modules.logs

import com.kalpcg.pulserelay.modules.logs.repositories.LogsRepository
import com.kalpcg.pulserelay.modules.logs.vm.LogsViewModel
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val logsModule = module {
    singleOf(::LogsRepository)
    singleOf(::LogsService)
    viewModelOf(::LogsViewModel)
}

val NAME = "logs"