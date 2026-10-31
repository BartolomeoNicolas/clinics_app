package com.bartolomeo.clinics.app.ui.screens

import androidx.compose.runtime.Composable
import com.bartolomeo.clinics.app.ui.components.NavAction
import com.bartolomeo.clinics.app.ui.components.Placeholder

@Composable
fun BookingWaitScreen(
    holdId: String,
    onRequestPhone: () -> Unit,
) {
    Placeholder(
        title = "Reservando turno",
        body = "Falta esperar el evento que pide datos adicionales para el hold $holdId.",
        actions = listOf(NavAction("Enviar mi teléfono", onRequestPhone)),
    )
}

@Composable
fun PhoneRequestScreen(
    holdId: String,
    onSubmitted: () -> Unit,
    onBack: () -> Unit,
) {
    Placeholder(
        title = "Confirmanos tu teléfono",
        body = "Falta el formulario de teléfono para confirmar el hold $holdId. El backend responde con un código estable si el proceso ya expiró.",
        actions = listOf(
            NavAction("Confirmar", onSubmitted),
            NavAction("Volver", onBack),
        ),
    )
}

@Composable
fun BookingResultScreen(
    appointmentId: String,
    onSeeBookings: () -> Unit,
) {
    Placeholder(
        title = "Turno reservado",
        body = "Falta el detalle del turno $appointmentId y la opción de cancelarlo.",
        actions = listOf(NavAction("Mis reservas", onSeeBookings)),
    )
}

@Composable
fun MyBookingsScreen(
    onOpenSyncStatus: () -> Unit,
    onBack: () -> Unit,
) {
    Placeholder(
        title = "Mis reservas",
        body = "Falta el listado de turnos de este usuario, con cancelación. Nadie ve reservas ajenas.",
        actions = listOf(
            NavAction("Estado de sincronización", onOpenSyncStatus),
            NavAction("Volver", onBack),
        ),
    )
}
