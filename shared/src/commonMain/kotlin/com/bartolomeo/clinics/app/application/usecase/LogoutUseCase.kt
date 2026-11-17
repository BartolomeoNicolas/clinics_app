package com.bartolomeo.clinics.app.application.usecase

import com.bartolomeo.clinics.app.domain.ports.out.SessionStore

// Borrar el token es el cierre de sesion: despues de esto ninguna llamada puede
// mandar la cabecera, porque el interceptor vuelve a leer un store vacio.
class LogoutUseCase(private val sessionStore: SessionStore) {

    suspend operator fun invoke() {
        sessionStore.clear()
    }
}
