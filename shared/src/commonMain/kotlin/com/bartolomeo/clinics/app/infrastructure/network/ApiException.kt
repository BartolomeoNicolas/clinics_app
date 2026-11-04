package com.bartolomeo.clinics.app.infrastructure.network

import com.bartolomeo.clinics.app.domain.error.AppError

// Es lo único que el cliente lanza ante un fallo: la UI decide por el AppError,
// no por el texto del mensaje.
class ApiException(val error: AppError) : Exception("Error de la API: $error")
