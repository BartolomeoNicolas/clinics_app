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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.bartolomeo.clinics.app.ui.auth.AuthUiState
import com.bartolomeo.clinics.app.ui.components.AuthField
import com.bartolomeo.clinics.app.ui.components.FormBanner
import com.bartolomeo.clinics.app.ui.userMessage

// Los campos y sus limites los de la seccion 3.2, y los opcionales se mandan como
// null si quedaron vacios: esa decision vive en el caso de uso, no aca. Esta
// pantalla solo lee el formulario del estado y reporta cambios de un campo.
@Composable
fun RegisterScreen(
    state: AuthUiState,
    onLoginChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onFirstNameChange: (String) -> Unit,
    onLastNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onImageUrlChange: (String) -> Unit,
    onLangKeyChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onBack: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = "Crear cuenta",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 8.dp),
        )

        state.error?.let {
            FormBanner(it.userMessage(), isError = true, modifier = Modifier.padding(bottom = 8.dp))
        }

        AuthField(
            value = state.registerForm.login,
            onValueChange = onLoginChange,
            label = "Login",
            error = state.fieldErrors["login"],
        )
        AuthField(
            value = state.registerForm.password,
            onValueChange = onPasswordChange,
            label = "Contraseña",
            isPassword = true,
            error = state.fieldErrors["password"],
        )
        AuthField(
            value = state.registerForm.firstName,
            onValueChange = onFirstNameChange,
            label = "Nombre",
            error = state.fieldErrors["firstName"],
        )
        AuthField(
            value = state.registerForm.lastName,
            onValueChange = onLastNameChange,
            label = "Apellido",
            error = state.fieldErrors["lastName"],
        )
        AuthField(
            value = state.registerForm.email,
            onValueChange = onEmailChange,
            label = "Email",
            keyboardType = KeyboardType.Email,
            error = state.fieldErrors["email"],
        )
        AuthField(
            value = state.registerForm.imageUrl,
            onValueChange = onImageUrlChange,
            label = "Foto de perfil (opcional)",
            keyboardType = KeyboardType.Uri,
            error = state.fieldErrors["imageUrl"],
        )
        AuthField(
            value = state.registerForm.langKey,
            onValueChange = onLangKeyChange,
            label = "Idioma (opcional)",
            imeAction = ImeAction.Done,
            error = state.fieldErrors["langKey"],
        )

        Button(
            onClick = onSubmit,
            enabled = !state.submitting,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
        ) {
            if (state.submitting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp,
                )
            } else {
                Text("Registrarme")
            }
        }

        OutlinedButton(
            onClick = onBack,
            enabled = !state.submitting,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Ya tengo cuenta")
        }
    }
}
