package com.bartolomeo.clinics.app.application.usecase

import com.bartolomeo.clinics.app.domain.model.RegisterCommand
import com.bartolomeo.clinics.app.domain.model.User
import com.bartolomeo.clinics.app.domain.ports.out.AuthApiPort
import com.bartolomeo.clinics.app.domain.validation.AuthValidation

class RegisterUseCase(private val authApi: AuthApiPort) {

    suspend operator fun invoke(raw: RegisterCommand): User {
        // La normalizacion vive aca y no en la pantalla porque es ella la que
        // decide que viaja al servidor (los campos de la seccion 3.2): recortar
        // los bordes hace que se valide exactamente lo que se manda, y un
        // opcionales que quedo vacio sale como null, que es lo que el backend
        // entiende por "no lo envie". Estando aca queda cubierto por los tests.
        val command = raw.copy(
            login = raw.login.trim(),
            firstName = raw.firstName.trim(),
            lastName = raw.lastName.trim(),
            email = raw.email.trim(),
            imageUrl = raw.imageUrl?.trim()?.ifBlank { null },
            langKey = raw.langKey?.trim()?.ifBlank { null },
        )

        val rejected = AuthValidation.register(command)
        if (rejected.isNotEmpty()) throw FieldValidationException(rejected)

        return authApi.register(command)
    }
}
