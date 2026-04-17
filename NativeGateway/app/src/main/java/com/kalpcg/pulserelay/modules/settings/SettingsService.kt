package com.kalpcg.pulserelay.modules.settings

import android.content.Context
import com.kalpcg.pulserelay.R
import com.kalpcg.pulserelay.modules.encryption.EncryptionSettings
import com.kalpcg.pulserelay.modules.gateway.GatewaySettings
import com.kalpcg.pulserelay.modules.logs.LogsSettings
import com.kalpcg.pulserelay.modules.messages.MessagesSettings
import com.kalpcg.pulserelay.modules.notifications.NotificationsService
import com.kalpcg.pulserelay.modules.ping.PingSettings
import com.kalpcg.pulserelay.modules.webhooks.WebhooksSettings

class SettingsService(
    private val context: Context,
    private val notificationsService: NotificationsService,
    encryptionSettings: EncryptionSettings,
    gatewaySettings: GatewaySettings,
    messagesSettings: MessagesSettings,
    pingSettings: PingSettings,
    logsSettings: LogsSettings,
    webhooksSettings: WebhooksSettings
) {
    private val settings = mapOf(
        "encryption" to encryptionSettings,
        "gateway" to gatewaySettings,
        "messages" to messagesSettings,
        "ping" to pingSettings,
        "logs" to logsSettings,
        "webhooks" to webhooksSettings
    )

    fun getAll(): Map<String, *> {
        return settings.mapValues { (it.value as? Exporter)?.export() }
    }

    fun update(data: Map<String, *>) {
        if (data.isEmpty()) {
            return
        }

        val changed = data.map { (key, value) ->
            try {
                settings[key]?.let {
                    (it as? Importer)?.import(value as Map<String, *>)
                }
            } catch (e: IllegalArgumentException) {
                throw IllegalArgumentException("Failed to import $key: ${e.message}", e)
            }
        }

        if (changed.none { it == true }) {
            return
        }

        notificationsService.notify(
            context,
            NotificationsService.NOTIFICATION_ID_SETTINGS_CHANGED,
            context.getString(R.string.settings_changed_via_api_restart_the_app_to_apply_changes)
        )
    }
}