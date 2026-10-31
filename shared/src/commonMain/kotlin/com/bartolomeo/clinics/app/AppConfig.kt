package com.bartolomeo.clinics.app

// Los dos backend viven en puertos distintos. Los valores llegan desde
// BuildConfig: no hay ninguna URL escrita en el código de la app.
data class AppConfig(
    val bookingBaseUrl: String,
    val syncBaseUrl: String,
)
