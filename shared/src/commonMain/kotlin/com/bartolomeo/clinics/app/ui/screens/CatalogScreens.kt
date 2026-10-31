package com.bartolomeo.clinics.app.ui.screens

import androidx.compose.runtime.Composable
import com.bartolomeo.clinics.app.ui.components.NavAction
import com.bartolomeo.clinics.app.ui.components.Placeholder

@Composable
fun SearchScreen(
    onOpenProfessional: (String) -> Unit,
    onSyncStatus: () -> Unit,
) {
    Placeholder(
        title = "Buscar turnos",
        body = "Falta la búsqueda con filtros de especialidad, profesional y día sobre la copia local del catálogo.",
        actions = listOf(
            NavAction("Ver profesional de ejemplo") { onOpenProfessional("prof-1") },
            NavAction("Estado de sincronización", onSyncStatus),
        ),
    )
}

@Composable
fun ProfessionalDetailScreen(
    professionalId: String,
    onSeeAvailability: () -> Unit,
    onBack: () -> Unit,
) {
    Placeholder(
        title = "Detalle del profesional",
        body = "Falta la ficha del profesional $professionalId con su especialidad y su agenda.",
        actions = listOf(
            NavAction("Ver turnos", onSeeAvailability),
            NavAction("Volver", onBack),
        ),
    )
}

@Composable
fun AvailabilityScreen(
    professionalId: String,
    onSlotSelected: (String) -> Unit,
    onBack: () -> Unit,
) {
    Placeholder(
        title = "Disponibilidad",
        body = "Falta la grilla de turnos libres del profesional $professionalId.",
        actions = listOf(
            NavAction("Reservar un turno") { onSlotSelected("slot-1") },
            NavAction("Volver", onBack),
        ),
    )
}
