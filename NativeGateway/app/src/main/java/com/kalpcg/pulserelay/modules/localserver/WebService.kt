package com.kalpcg.pulserelay.modules.localserver

import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.wifi.WifiManager
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.ktor.http.HttpStatusCode
import io.ktor.http.toHttpDate
import io.ktor.serialization.gson.gson
import io.ktor.server.application.call
import io.ktor.server.application.createApplicationPlugin
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.UserIdPrincipal
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.basic
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.plugins.NotFoundException
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.header
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import io.ktor.util.date.GMTDate
import com.kalpcg.pulserelay.R
import com.kalpcg.pulserelay.domain.HealthResponse
import com.kalpcg.pulserelay.extensions.configure
import com.kalpcg.pulserelay.modules.health.HealthService
import com.kalpcg.pulserelay.modules.health.domain.Status
import com.kalpcg.pulserelay.modules.localserver.auth.AuthScopes
import com.kalpcg.pulserelay.modules.localserver.auth.JwtService
import com.kalpcg.pulserelay.modules.localserver.auth.requireScope
import com.kalpcg.pulserelay.modules.localserver.domain.Device
import com.kalpcg.pulserelay.modules.localserver.routes.AuthRoutes
import com.kalpcg.pulserelay.modules.localserver.routes.DocsRoutes
import com.kalpcg.pulserelay.modules.localserver.routes.LogsRoutes
import com.kalpcg.pulserelay.modules.localserver.routes.MessagesRoutes
import com.kalpcg.pulserelay.modules.localserver.routes.WebhooksRoutes
import com.kalpcg.pulserelay.modules.notifications.NotificationsService
import org.koin.android.ext.android.get
import org.koin.android.ext.android.inject
import java.util.Date
import kotlin.concurrent.thread

class WebService : Service() {

    private val settings: LocalServerSettings by inject()
    private val notificationsService: NotificationsService by inject()
    private val healthService: HealthService by inject()
    private val jwtService: JwtService by lazy { JwtService(get(), get(), get()) }

    private val wakeLock: PowerManager.WakeLock by lazy {
        (getSystemService(Context.POWER_SERVICE) as PowerManager).run {
            newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, this.javaClass.name)
        }
    }
    private val wifiLock: WifiManager.WifiLock by lazy {
        (getSystemService(Context.WIFI_SERVICE) as WifiManager).createWifiLock(
            WifiManager.WIFI_MODE_FULL_HIGH_PERF,
            this.javaClass.name
        )
    }

    private val server by lazy {
        embeddedServer(
            Netty,
            port = port,
            watchPaths = emptyList(),
        ) {
            install(Authentication) {
                basic("auth-basic") {
                    realm = "Access to Pulse Relay"
                    validate { credentials ->
                        when {
                            credentials.name == username
                                    && credentials.password == password -> UserIdPrincipal(
                                credentials.name
                            )

                            else -> null
                        }
                    }
                }
                jwt("auth-jwt") {
                    realm = "Access to Pulse Relay"
                    verifier { jwtService.verifier() }
                    validate { credential ->
                        val tokenId = credential.payload.id ?: return@validate null
                        if (jwtService.isTokenRevoked(tokenId)) {
                            return@validate null
                        }

                        JWTPrincipal(credential.payload)
                    }
                }
            }
            install(ContentNegotiation) {
                gson {
                    if (com.kalpcg.pulserelay.BuildConfig.DEBUG) {
                        setPrettyPrinting()
                    }
                    configure()
                }
            }
            install(StatusPages) {
                exception<Throwable> { call, cause ->
                    call.respond(
                        when (cause) {
                            is IllegalArgumentException -> HttpStatusCode.BadRequest
                            is BadRequestException -> HttpStatusCode.BadRequest
                            is NotFoundException -> HttpStatusCode.NotFound
                            else -> HttpStatusCode.InternalServerError
                        },
                        mapOf("message" to cause.description)
                    )
                }
            }
            install(createApplicationPlugin(name = "DateHeader") {
                onCall { call ->
                    call.response.header(
                        "Date",
                        GMTDate(null).toHttpDate()
                    )
                }
            })
            routing {
                get("/health") {
                    val healthResult = healthService.healthCheck()
                    call.respond(
                        when (healthResult.status) {
                            Status.FAIL -> HttpStatusCode.InternalServerError
                            Status.WARN -> HttpStatusCode.OK
                            Status.PASS -> HttpStatusCode.OK
                        },
                        HealthResponse(healthResult)
                    )
                }
                authenticate("auth-basic", "auth-jwt") {
                    get("/") {
                        call.respond(mapOf("status" to "ok", "model" to Build.MODEL))
                    }
                    route("/device") {
                        get {
                            if (!requireScope(AuthScopes.DevicesList
)) return@get
                            val firstInstallTime = packageManager.getPackageInfo(
                                packageName,
                                0
                            ).firstInstallTime
                            val deviceName = "${Build.MANUFACTURER}/${Build.PRODUCT}"
                            val device = Device(
                                requireNotNull(settings.deviceId),
                                deviceName,
                                Date(firstInstallTime),
                                Date(),
                                Date()
                            )

                            call.respond(listOf(device))
                        }
                    }
                    MessagesRoutes(applicationContext, get(), get(), get()).let {
                        route("/message") {
                            it.register(this)
                        }
                        route("/messages") {
                            it.register(this)
                        }
                    }
                    WebhooksRoutes(get(), get()).let {
                        route("/webhook") {
                            it.register(this)
                        }
                        route("/webhooks") {
                            it.register(this)
                        }
                    }

                    route("/logs") {
                        LogsRoutes(get()).register(this)
                    }
                    route("/settings") {
                        com.kalpcg.pulserelay.modules.localserver.routes.SettingsRoutes(get())
                            .register(this)
                    }
                    route("/docs") {
                        DocsRoutes(get()).register(this)
                    }
                    route("/auth") {
                        AuthRoutes(jwtService).register(this)
                    }
                }
            }
        }
    }

    private val port = settings.port
    private val username = settings.username
    private val password = settings.password

    override fun onCreate() {
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = notificationsService.makeNotification(
            this,
            NotificationsService.NOTIFICATION_ID_LOCAL_SERVICE,
            getString(
                R.string.sms_gateway_is_running_on_port,
                port
            )
        )

        startForeground(NotificationsService.NOTIFICATION_ID_LOCAL_SERVICE, notification)

        if (status.value != true) {
            thread {
                try {
                    server.start()
                    wakeLock.acquire()
                    wifiLock.acquire()

                    status.postValue(true)
                } catch (e: Exception) {
                    e.printStackTrace()
                    status.postValue(false)
                    synchronized(startLock) {
                        startRequested = false
                    }
                    stopSelf()
                }
            }
        }

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent): IBinder {
        TODO("Return the communication channel to the service.")
    }

    override fun onDestroy() {
        runCatching {
            if (wifiLock.isHeld) {
                wifiLock.release()
            }
        }
        runCatching {
            if (wakeLock.isHeld) {
                wakeLock.release()
            }
        }
        thread {
            runCatching { server.stop() }
        }

        stopForeground(true)

        status.postValue(false)
        synchronized(startLock) {
            startRequested = false
        }

        super.onDestroy()
    }

    companion object {
        private val status = MutableLiveData<Boolean>(false)
        private val startLock = Any()
        @Volatile
        private var startRequested = false
        val STATUS: LiveData<Boolean> = status

        fun start(context: Context) {
            synchronized(startLock) {
                if (status.value == true || startRequested) {
                    return
                }

                startRequested = true
            }

            val intent = Intent(context, WebService::class.java)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, WebService::class.java))
        }
    }
}

private val Throwable.description: String
    get() {
        return (localizedMessage ?: message ?: toString()) +
                (cause?.let { ": " + it.description } ?: "")
    }
