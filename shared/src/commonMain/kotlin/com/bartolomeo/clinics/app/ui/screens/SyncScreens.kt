package com.bartolomeo.clinics.app.ui.screens

import androidx.compose.runtime.Composable
import com.bartolomeo.clinics.app.AppConfig
import com.bartolomeo.clinics.app.ui.components.NavAction
import com.bartolomeo.clinics.app.ui.components.Placeholder

@Composable
fun SyncStatusScreen(
    config: AppConfig,
    onMyBookings: () -> Unit,
    onBack: () -> Unit,
) {
    Placeholder(
        title = "Estado de sincronización",
        body = "Falta consultar la versión del snapshot del catálogo en ${config.syncBaseUrl}. " +
            "La URL no está escrita en el código: llega desde el BuildConfig del destino.",
        actions = listOf(
            NavAction("Mis reservas", onMyBookings),
            NavAction("Volver", onBack),
        ),
    )
}
