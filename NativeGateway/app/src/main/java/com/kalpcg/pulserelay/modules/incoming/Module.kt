package com.kalpcg.pulserelay.modules.incoming

import com.kalpcg.pulserelay.modules.incoming.repositories.IncomingMessagesRepository
import com.kalpcg.pulserelay.modules.incoming.vm.IncomingMessagesListViewModel
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val incomingModule = module {
    singleOf(::IncomingMessagesRepository)
    singleOf(::IncomingMessagesService)
    viewModelOf(::IncomingMessagesListViewModel)
}

const val MODULE_NAME = "incoming"
