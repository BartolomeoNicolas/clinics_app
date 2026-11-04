package com.bartolomeo.clinics.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.bartolomeo.clinics.app.navigation.ClinicsNavGraph
import com.bartolomeo.clinics.app.navigation.Routes
import com.bartolomeo.clinics.app.ui.auth.AuthViewModel
import com.bartolomeo.clinics.app.ui.components.Loading
import com.bartolomeo.clinics.app.ui.theme.ClinicsTheme

@Composable
fun ClinicsApp(
    config: AppConfig,
    authViewModel: AuthViewModel,
) {
    ClinicsTheme {
        val authState by authViewModel.state.collectAsState()

        // El Scaffold absorbe las insets de las barras del sistema: el ComposeView
        // cubre toda la ventana, asi que sin esto el contenido arranca en y=0.
        Scaffold { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
            ) {
                // Todavia no se sabe si hay sesion guardada, asi que no se elige la
                // pantalla de arranque a ciegas: esa decision espera el resultado.
                if (authState.booted) {
                    val navController = rememberNavController()

                    ClinicsNavGraph(
                        navController = navController,
                        config = config,
                        authViewModel = authViewModel,
                        startDestination = if (authState.hasSession) Routes.SEARCH else Routes.LOGIN,
                    )
                } else {
                    Loading()
                }
            }
        }
    }
}
