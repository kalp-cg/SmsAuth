package com.kalpcg.pulserelay.modules.encryption

import org.koin.dsl.module

val encryptionModule = module {
    single {
        EncryptionService(get())
    }
}