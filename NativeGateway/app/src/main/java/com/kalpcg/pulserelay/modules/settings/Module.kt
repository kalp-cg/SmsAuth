package com.kalpcg.pulserelay.modules.settings

import androidx.preference.PreferenceManager
import com.kalpcg.pulserelay.helpers.SettingsHelper
import com.kalpcg.pulserelay.modules.encryption.EncryptionSettings
import com.kalpcg.pulserelay.modules.gateway.GatewaySettings
import com.kalpcg.pulserelay.modules.localserver.LocalServerSettings
import com.kalpcg.pulserelay.modules.logs.LogsSettings
import com.kalpcg.pulserelay.modules.messages.MessagesSettings
import com.kalpcg.pulserelay.modules.ping.PingSettings
import com.kalpcg.pulserelay.modules.receiver.StateStorage
import com.kalpcg.pulserelay.modules.webhooks.TemporaryStorage
import com.kalpcg.pulserelay.modules.webhooks.WebhooksSettings
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module


val settingsModule = module {
    singleOf(::SettingsService)
    factory { PreferenceManager.getDefaultSharedPreferences(get()) }
    factory { SettingsHelper(get()) }

    factory {
        EncryptionSettings(
            PreferencesStorage(get(), "encryption")
        )
    }
    factory {
        GatewaySettings(
            PreferencesStorage(get(), "gateway")
        )
    }
    factory {
        MessagesSettings(
            PreferencesStorage(get(), "messages")
        )
    }
    factory {
        LocalServerSettings(
            PreferencesStorage(get(), "localserver")
        )
    }
    factory {
        PingSettings(
            PreferencesStorage(get(), "ping")
        )
    }
    factory {
        LogsSettings(
            PreferencesStorage(get(), "logs")
        )
    }
    factory {
        WebhooksSettings(
            PreferencesStorage(get(), "webhooks")
        )
    }
    single {
        TemporaryStorage(
            PreferencesStorage(get(), "webhooks")
        )
    }
    single {
        StateStorage(
            PreferencesStorage(get(), "receiver")
        )
    }
}