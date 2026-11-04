package com.bartolomeo.clinics.app.domain.ports.out

import com.bartolomeo.clinics.app.domain.model.LoginCommand
import com.bartolomeo.clinics.app.domain.model.RegisterCommand
import com.bartolomeo.clinics.app.domain.model.Session
import com.bartolomeo.clinics.app.domain.model.User

// El dominio declara que existen registro y login; que sean llamadas HTTP es
// asunto de la infraestructura.
interface AuthApiPort {
    suspend fun register(command: RegisterCommand): User

    suspend fun login(command: LoginCommand): Session
}
