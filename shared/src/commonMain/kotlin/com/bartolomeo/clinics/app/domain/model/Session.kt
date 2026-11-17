package com.bartolomeo.clinics.app.domain.model

// Lo que deja autenticarse. El token no se muestra en pantalla ni se registra en
// ningun log: solo se guarda cifrado y se manda por cabecera.
//
// El login no conserva el usuario: los datos de la cuenta los trae /api/me cuando
// alguna pantalla los necesite, y asi la sesion sigue siendo un par de valores.
data class Session(
    val token: String,
    val expiresAtEpochMillis: Long?,
)
