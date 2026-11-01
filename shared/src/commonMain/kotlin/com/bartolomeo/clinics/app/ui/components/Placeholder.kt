package com.bartolomeo.clinics.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

data class NavAction(
    val label: String,
    val onClick: () -> Unit,
)

// Andamiaje de las pantallas todavia sin implementar: todas se ven igual para que se
// distinga bien que es contenido real y que es marcador de posicion. El titulo es
// opcional porque SearchScreen ya trae el suyo con la barra de sesion encima.
@Composable
fun Placeholder(
    body: String,
    modifier: Modifier = Modifier,
    title: String = "",
    actions: List<NavAction> = emptyList(),
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
    ) {
        if (title.isNotEmpty()) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
            )
        }

        Text(
            text = body,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )

        actions.forEach { action ->
            OutlinedButton(onClick = action.onClick) {
                Text(action.label)
            }
        }
    }
}
