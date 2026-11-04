package com.bartolomeo.clinics.app.domain.model

data class LoginCommand(
    val login: String,
    val password: String,
)
