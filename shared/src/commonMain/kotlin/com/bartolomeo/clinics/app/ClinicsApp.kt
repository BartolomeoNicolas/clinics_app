package com.bartolomeo.clinics.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.bartolomeo.clinics.app.navigation.ClinicsNavGraph
import com.bartolomeo.clinics.app.ui.theme.ClinicsTheme

@Composable
fun ClinicsApp(
    config: AppConfig,
) {
    ClinicsTheme {
        val navController = rememberNavController()

        // El Scaffold absorbe las insets de las barras del sistema: el ComposeView
        // cubre toda la ventana, así que sin esto el contenido arranca en y=0.
        Scaffold { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
            ) {
                ClinicsNavGraph(
                    navController = navController,
                    config = config,
                )
            }
        }
    }
}
