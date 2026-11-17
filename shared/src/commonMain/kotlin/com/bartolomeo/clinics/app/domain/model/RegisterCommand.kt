package com.bartolomeo.clinics.app.domain.model

// Los mismos campos que RegisterRequest.java del backend. `imageUrl` y `langKey`
// son opcionales ahi, y lo son aca.
data class RegisterCommand(
    val login: String,
    val password: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val imageUrl: String? = null,
    val langKey: String? = null,
)
