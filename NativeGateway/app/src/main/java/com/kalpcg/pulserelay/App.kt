package com.kalpcg.pulserelay

import android.app.Application
import healthModule
import com.kalpcg.pulserelay.data.dbModule
import com.kalpcg.pulserelay.modules.connection.connectionModule
import com.kalpcg.pulserelay.modules.encryption.encryptionModule
import com.kalpcg.pulserelay.modules.events.eventBusModule
import com.kalpcg.pulserelay.modules.gateway.GatewayService
import com.kalpcg.pulserelay.modules.incoming.incomingModule
import com.kalpcg.pulserelay.modules.localserver.localserverModule
import com.kalpcg.pulserelay.modules.logs.logsModule
import com.kalpcg.pulserelay.modules.messages.messagesModule
import com.kalpcg.pulserelay.modules.notifications.notificationsModule
import com.kalpcg.pulserelay.modules.orchestrator.OrchestratorService
import com.kalpcg.pulserelay.modules.orchestrator.orchestratorModule
import com.kalpcg.pulserelay.modules.ping.pingModule
import com.kalpcg.pulserelay.modules.receiver.receiverModule
import com.kalpcg.pulserelay.modules.settings.settingsModule
import com.kalpcg.pulserelay.modules.webhooks.webhooksModule
import com.kalpcg.pulserelay.receivers.EventsReceiver
import org.koin.android.ext.android.get
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class App: Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger()
            androidContext(this@App)
            modules(
                eventBusModule,
                settingsModule,
                dbModule,
                logsModule,
                notificationsModule,
                messagesModule,
                incomingModule,
                receiverModule,
                encryptionModule,
                com.kalpcg.pulserelay.modules.gateway.gatewayModule,
                healthModule,
                webhooksModule,
                localserverModule,
                pingModule,
                connectionModule,
                orchestratorModule,
            )
        }

        Thread.setDefaultUncaughtExceptionHandler(
            GlobalExceptionHandler(
                Thread.getDefaultUncaughtExceptionHandler()!!,
                get()
            )
        )

        instance = this

        EventsReceiver.register(this)

        get<OrchestratorService>().start(this, true)
    }

    val gatewayService: GatewayService by inject()

    companion object {
        lateinit var instance: App
            private set
    }
}
