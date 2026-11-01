package com.bartolomeo.clinics.app.application.usecase

import com.bartolomeo.clinics.app.domain.ports.out.SessionStore

// Arranque de la app: si hay un token guardado que no vencio, se entra directo al
// flujo y no se vuelve a pedir el login.
class RestoreSessionUseCase(private val sessionStore: SessionStore) {

    suspend operator fun invoke(): Boolean = sessionStore.token() != null
}
