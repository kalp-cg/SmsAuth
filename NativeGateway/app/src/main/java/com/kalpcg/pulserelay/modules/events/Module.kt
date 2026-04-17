package com.kalpcg.pulserelay.modules.events

import org.koin.dsl.module

val eventBusModule = module {
    single { EventBus() }
}