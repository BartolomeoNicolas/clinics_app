package com.bartolomeo.clinics.app.infrastructure.network

import com.bartolomeo.clinics.app.domain.ports.out.SessionTokenProvider

// Punto de entrada publico de la red. Quien compone la app pide un ApiClient y no
// ve el motor, el cliente de Ktor ni sus plugins: si mañana cambia el motor o se
// agrega una plataforma, no se toca el composition root.
fun createApiClient(tokenProvider: SessionTokenProvider): ApiClient =
    ApiClient(createHttpClient(platformHttpEngine(), tokenProvider))
