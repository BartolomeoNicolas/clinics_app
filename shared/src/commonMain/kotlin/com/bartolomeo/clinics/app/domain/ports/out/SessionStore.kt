package com.bartolomeo.clinics.app.domain.ports.out

// Almacenamiento de la sesion. Hereda la lectura porque el cliente HTTP solo eso
// necesita (el interceptor); escribir y borrar pertenece al flujo de autenticacion.
//
// Un token vencido se devuelve como si no estuviera: no tiene sentido mandar una
// cabecera que el backend va a rechazar igual.
interface SessionStore : SessionTokenProvider {
    suspend fun save(token: String, expiresAtEpochMillis: Long?)

    suspend fun clear()
}
