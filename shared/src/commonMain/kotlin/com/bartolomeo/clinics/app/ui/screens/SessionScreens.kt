package com.bartolomeo.clinics.app.ui.screens

import androidx.compose.runtime.Composable
import com.bartolomeo.clinics.app.ui.components.NavAction
import com.bartolomeo.clinics.app.ui.components.Placeholder

@Composable
fun LoginScreen(
    onLogin: () -> Unit,
    onRegister: () -> Unit,
) {
    Placeholder(
        title = "Iniciar sesión",
        body = "Falta el formulario de email y contraseña. El backend emite el JWT que la app usa durante toda la sesión.",
        actions = listOf(
            NavAction("Entrar", onLogin),
            NavAction("Crear cuenta", onRegister),
        ),
    )
}

@Composable
fun RegisterScreen(
    onRegistered: () -> Unit,
) {
    Placeholder(
        title = "Crear cuenta",
        body = "Falta el registro con los mismos límites de validación que exige el backend.",
        actions = listOf(NavAction("Registrarme", onRegistered)),
    )
}
