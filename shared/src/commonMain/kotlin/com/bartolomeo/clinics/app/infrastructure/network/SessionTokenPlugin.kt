package com.bartolomeo.clinics.app.infrastructure.network

import com.bartolomeo.clinics.app.domain.ports.out.SessionTokenProvider
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders

// Interceptor del JWT de sesión. Sin token todavía no se manda la cabecera, que
// es lo que permite consumir los endpoints públicos.
internal fun sessionTokenPlugin(tokenProvider: SessionTokenProvider) =
    createClientPlugin("SessionToken") {
        onRequest { request, _ ->
            tokenProvider.token()?.let { token ->
                request.header(HttpHeaders.Authorization, "Bearer $token")
            }
        }
    }
