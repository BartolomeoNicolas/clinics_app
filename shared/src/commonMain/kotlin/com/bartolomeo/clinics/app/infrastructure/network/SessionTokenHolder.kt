package com.bartolomeo.clinics.app.infrastructure.network

import com.bartolomeo.clinics.app.domain.port.SessionTokenProvider

// Adaptador en memoria, mientras el guardado seguro de Android no esté listo.
// El interceptor consume el puerto, así que cambiarlo no toca al cliente.
class SessionTokenHolder : SessionTokenProvider {

    private var current: String? = null

    override suspend fun token(): String? = current

    fun set(value: String?) {
        current = value
    }
}
