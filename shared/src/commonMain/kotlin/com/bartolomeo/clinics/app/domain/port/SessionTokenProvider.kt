package com.bartolomeo.clinics.app.domain.port

// Puerto de salida: el cliente HTTP solo pide el token, no sabe dónde se guarda.
interface SessionTokenProvider {
    suspend fun token(): String?
}
