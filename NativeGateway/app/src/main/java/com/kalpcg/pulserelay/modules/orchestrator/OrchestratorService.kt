package com.kalpcg.pulserelay.modules.orchestrator

import android.content.Context
import com.kalpcg.pulserelay.helpers.SettingsHelper
import com.kalpcg.pulserelay.modules.gateway.GatewayService
import com.kalpcg.pulserelay.modules.localserver.LocalServerService
import com.kalpcg.pulserelay.modules.logs.LogsService
import com.kalpcg.pulserelay.modules.logs.db.LogEntry
import com.kalpcg.pulserelay.modules.messages.MessagesService
import com.kalpcg.pulserelay.modules.ping.PingService
import com.kalpcg.pulserelay.modules.receiver.ReceiverService
import com.kalpcg.pulserelay.modules.webhooks.WebHooksService

class OrchestratorService(
    private val messagesSvc: MessagesService,
    private val gatewaySvc: GatewayService,
    private val localServerSvc: LocalServerService,
    private val webHooksSvc: WebHooksService,
    private val receiverService: ReceiverService,
    private val pingSvc: PingService,
    private val logsSvc: LogsService,
    private val settings: SettingsHelper,
) {
    fun start(context: Context, autostart: Boolean) {
        if (autostart && !settings.autostart) {
            return
        }

        runCatching { logsSvc.start(context) }
            .onFailure {
                logsSvc.insert(
                    LogEntry.Priority.WARN,
                    MODULE_NAME,
                    "Can't start logs service",
                    mapOf("error" to (it.message ?: it.toString()))
                )
            }

        runCatching { messagesSvc.start(context) }
            .onFailure {
                logsSvc.insert(
                    LogEntry.Priority.WARN,
                    MODULE_NAME,
                    "Can't start messages service",
                    mapOf("error" to (it.message ?: it.toString()))
                )
            }

        runCatching { webHooksSvc.start(context) }
            .onFailure {
                logsSvc.insert(
                    LogEntry.Priority.WARN,
                    MODULE_NAME,
                    "Can't start webhooks service",
                    mapOf("error" to (it.message ?: it.toString()))
                )
            }

        // gatewaySvc.start(context)

        // Custom Cloud Poller! Works independent of whether 'remote server' switch is ON.
        // CloudPoller disabled for local-only

        runCatching { localServerSvc.start(context) }
            .onFailure {
                logsSvc.insert(
                    LogEntry.Priority.WARN,
                    MODULE_NAME,
                    "Can't start local server",
                    mapOf("error" to (it.message ?: it.toString()))
                )
            }

        runCatching { pingSvc.start(context) }
            .onFailure {
                logsSvc.insert(
                    LogEntry.Priority.WARN,
                    MODULE_NAME,
                    "Can't start ping service",
                    mapOf("error" to (it.message ?: it.toString()))
                )
            }

        runCatching { receiverService.start(context) }
            .onFailure {
                logsSvc.insert(
                    LogEntry.Priority.WARN,
                    MODULE_NAME,
                    "Can't register receiver",
                    mapOf("error" to (it.message ?: it.toString()))
                )
            }
    }

    fun stop(context: Context) {
        com.kalpcg.pulserelay.CloudPoller.stop()
        receiverService.stop(context)
        pingSvc.stop(context)
        localServerSvc.stop(context)

        gatewaySvc.stop(context)
        webHooksSvc.stop(context)
        messagesSvc.stop(context)
        logsSvc.stop(context)
    }
}