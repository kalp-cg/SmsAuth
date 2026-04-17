package com.kalpcg.pulserelay.modules.localserver.routes

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import com.kalpcg.pulserelay.modules.localserver.auth.AuthScopes
import com.kalpcg.pulserelay.modules.localserver.auth.JwtService
import com.kalpcg.pulserelay.modules.localserver.auth.requireScope
import com.kalpcg.pulserelay.modules.localserver.domain.TokenRequest
import com.kalpcg.pulserelay.modules.localserver.domain.TokenResponse

class AuthRoutes(
    private val jwtService: JwtService,
) {
    fun register(routing: Route) {
        routing.apply {
            route("token") {
                tokenRoutes()
            }
        }
    }

    private fun Route.tokenRoutes() {
        post {
            if (!requireScope(AuthScopes.TokensManage)) return@post
            val request = call.receive<TokenRequest>()
            val token = jwtService.generateToken(request.scopes, request.ttl)
            call.respond(
                HttpStatusCode.Created,
                TokenResponse(
                    id = token.id,
                    tokenType = "Bearer",
                    accessToken = token.accessToken,
                    expiresAt = token.expiresAt,
                )
            )
        }
        delete("/{jti}") {
            if (!requireScope(AuthScopes.TokensManage)) return@delete
            val jti = call.parameters["jti"]?.trim()
            if (jti.isNullOrEmpty()) {
                call.respond(HttpStatusCode.BadRequest, mapOf("message" to "jti is required"))
                return@delete
            }

            jwtService.revokeToken(jti)
            call.respond(HttpStatusCode.NoContent)
        }
    }
}
