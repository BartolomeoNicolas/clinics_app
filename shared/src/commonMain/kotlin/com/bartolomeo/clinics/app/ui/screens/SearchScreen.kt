package com.bartolomeo.clinics.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bartolomeo.clinics.app.domain.error.AppError
import com.bartolomeo.clinics.app.ui.components.FormBanner
import com.bartolomeo.clinics.app.ui.components.NavAction
import com.bartolomeo.clinics.app.ui.components.Placeholder
import com.bartolomeo.clinics.app.ui.userMessage

// La busqueda de turnos recien llega en APP-M1-02. Lo que ya es real aca es la
// sesion: es la pantalla a la que se entra logueado, asi que es donde vive el
// cierre de sesion.
@Composable
fun SearchScreen(
    onLogout: () -> Unit,
    logoutError: AppError?,
    onOpenProfessional: (String) -> Unit,
    onSyncStatus: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedButton(onClick = onLogout) {
                Text("Cerrar sesión")
            }
        }

        logoutError?.let { FormBanner(it.userMessage(), isError = true) }

        Text(
            text = "Buscar turnos",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.fillMaxWidth(),
        )

        Placeholder(
            body = "Falta la búsqueda con filtros de especialidad, profesional y día sobre la copia local del catálogo.",
            modifier = Modifier.weight(1f),
            actions = listOf(
                NavAction("Ver profesional de ejemplo") { onOpenProfessional("prof-1") },
                NavAction("Estado de sincronización", onSyncStatus),
            ),
        )
    }
}
