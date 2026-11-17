package com.bartolomeo.clinics.app.application.usecase

import com.bartolomeo.clinics.app.domain.model.LoginCommand
import com.bartolomeo.clinics.app.domain.model.Session
import com.bartolomeo.clinics.app.domain.ports.out.AuthApiPort
import com.bartolomeo.clinics.app.domain.ports.out.SessionStore
import com.bartolomeo.clinics.app.domain.validation.AuthValidation

// Ademas de autenticar, es quien persiste el token: sin eso, cerrar y abrir la app
// dejaria al usuario afuera cada vez.
class LoginUseCase(
    private val authApi: AuthApiPort,
    private val sessionStore: SessionStore,
) {

    suspend operator fun invoke(raw: LoginCommand): Session {
        // Se recorta antes de validar por la misma razon que en el registro: lo que
        // se valida tiene que ser lo que se manda. La contrasena no se recorta, que
        // es como la trata el backend (solo se fija que no este en blanco).
        val command = raw.copy(login = raw.login.trim())
        val rejected = AuthValidation.login(command)
        if (rejected.isNotEmpty()) throw FieldValidationException(rejected)

        val session = authApi.login(command)
        sessionStore.save(session.token, session.expiresAtEpochMillis)

        return session
    }
}
