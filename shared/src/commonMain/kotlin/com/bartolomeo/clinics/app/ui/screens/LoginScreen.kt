package com.bartolomeo.clinics.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.bartolomeo.clinics.app.ui.auth.AuthUiState
import com.bartolomeo.clinics.app.ui.components.AuthField
import com.bartolomeo.clinics.app.ui.components.FormBanner
import com.bartolomeo.clinics.app.ui.userMessage

// No guarda nada: lee el formulario del estado y avisa cuando algo cambio o cuando
// hay que enviarlo. Toda la decision (normalizar, validar, llamar al backend)
// esta del lado del ViewModel y de los casos de uso, donde se puede testear.
@Composable
fun LoginScreen(
    state: AuthUiState,
    onLoginChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onGoToRegister: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically),
    ) {
        Text(
            text = "Iniciar sesión",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 8.dp),
        )

        state.notice?.let { FormBanner(it) }
        state.error?.let { FormBanner(it.userMessage(), isError = true) }

        AuthField(
            value = state.loginForm.login,
            onValueChange = onLoginChange,
            label = "Login",
            error = state.fieldErrors["login"],
        )

        AuthField(
            value = state.loginForm.password,
            onValueChange = onPasswordChange,
            label = "Contraseña",
            isPassword = true,
            imeAction = ImeAction.Done,
            error = state.fieldErrors["password"],
        )

        Button(
            onClick = onSubmit,
            enabled = !state.submitting,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
        ) {
            if (state.submitting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp,
                )
            } else {
                Text("Entrar")
            }
        }

        OutlinedButton(
            onClick = onGoToRegister,
            enabled = !state.submitting,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Crear cuenta")
        }
    }
}
