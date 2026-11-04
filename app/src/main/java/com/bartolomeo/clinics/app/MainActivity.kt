package com.bartolomeo.clinics.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bartolomeo.clinics.app.application.usecase.LoginUseCase
import com.bartolomeo.clinics.app.application.usecase.LogoutUseCase
import com.bartolomeo.clinics.app.application.usecase.RegisterUseCase
import com.bartolomeo.clinics.app.application.usecase.RestoreSessionUseCase
import com.bartolomeo.clinics.app.infrastructure.network.AuthApiAdapter
import com.bartolomeo.clinics.app.infrastructure.network.createApiClient
import com.bartolomeo.clinics.app.infrastructure.storage.SecureTokenStore
import com.bartolomeo.clinics.app.ui.auth.AuthViewModel

// Composition root: aca se cruzan lo que decide el build config con lo que es de
// plataforma. El ViewModel va por viewModel() para sobrevivir a rotaciones de pantalla.
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val config = AppConfig(
            bookingBaseUrl = BuildConfig.BOOKING_BASE_URL,
            syncBaseUrl = BuildConfig.SYNC_BASE_URL,
        )

        val sessionStore = SecureTokenStore(this)
        val apiClient = createApiClient(sessionStore)
        val authApi = AuthApiAdapter(apiClient, config.bookingBaseUrl)

        setContent {
            val authViewModel: AuthViewModel = viewModel {
                AuthViewModel(
                    registerUseCase = RegisterUseCase(authApi),
                    loginUseCase = LoginUseCase(authApi, sessionStore),
                    logoutUseCase = LogoutUseCase(sessionStore),
                    restoreSessionUseCase = RestoreSessionUseCase(sessionStore),
                )
            }

            ClinicsApp(config = config, authViewModel = authViewModel)
        }
    }
}
