package com.bartolomeo.clinics.app.domain.model

// El usuario que devuelve el backend. §3.2: el id, el estado de activacion y las
// autoridades los pone el servidor, quien se registra no puede elegirlos.
data class User(
    val id: Long,
    val login: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val imageUrl: String?,
    val langKey: String?,
    val activated: Boolean,
    val authorities: List<String>,
)
