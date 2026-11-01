package com.bartolomeo.clinics.app.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bartolomeo.clinics.app.application.usecase.FieldValidationException
import com.bartolomeo.clinics.app.application.usecase.LoginUseCase
import com.bartolomeo.clinics.app.application.usecase.LogoutUseCase
import com.bartolomeo.clinics.app.application.usecase.RegisterUseCase
import com.bartolomeo.clinics.app.application.usecase.RestoreSessionUseCase
import com.bartolomeo.clinics.app.domain.error.AppError
import com.bartolomeo.clinics.app.domain.model.LoginCommand
import com.bartolomeo.clinics.app.domain.model.RegisterCommand
import com.bartolomeo.clinics.app.infrastructure.network.ApiException
import com.bartolomeo.clinics.app.ui.asFieldErrors
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Lo que se escribe en cada formulario. Vive en el ViewModel y no en la pantalla
// porque ahi esta la unica copia: girar el telefono, volver de otro flujo o que
// Android mate el proceso a mitad de escribir no pierde lo escrito, y ademas entra
// en los tests del ViewModel sin tener que montar un arbol de composicion.
// Son dos bloques separados porque son dos pantallas independientes: lo que se
// escribio para entrar no se cuela en el registro ni al reves.
data class LoginForm(
    val login: String = "",
    val password: String = "",
)

data class RegisterForm(
    val login: String = "",
    val password: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val imageUrl: String = "",
    val langKey: String = "",
)

data class AuthUiState(
    /** Todavia no se sabe si hay sesion guardada: el arranque espera esto. */
    val booted: Boolean = false,
    val hasSession: Boolean = false,
    val submitting: Boolean = false,
    val loginForm: LoginForm = LoginForm(),
    val registerForm: RegisterForm = RegisterForm(),
    val fieldErrors: Map<String, String> = emptyMap(),
    /** Error general del formulario: credenciales, red, servidor. */
    val error: AppError? = null,
    /** Aviso que viaja de una pantalla a otra, por ejemplo el registro exitoso. */
    val notice: String? = null,
)

// Navegacion one-shot. Va por flujo y no por estado para que no se dispare otra vez
// al recomponer ni al girar el telefono.
sealed interface AuthEvent {
    data object LoggedIn : AuthEvent

    data object Registered : AuthEvent

    data object SessionClosed : AuthEvent
}

class AuthViewModel(
    private val registerUseCase: RegisterUseCase,
    private val loginUseCase: LoginUseCase,
    private val logoutUseCase: LogoutUseCase,
    restoreSessionUseCase: RestoreSessionUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(AuthUiState())
    val state: StateFlow<AuthUiState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<AuthEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<AuthEvent> = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            val restored = restoreSessionUseCase()
            _state.update { it.copy(booted = true, hasSession = restored) }
        }
    }

    // Un setter por campo. La pantalla solo reporta que cambio y no guarda nada:
    // el estado vive en un solo lugar y cada modificacion se ve en el flujo.
    fun onLoginChange(value: String) =
        _state.update { it.copy(loginForm = it.loginForm.copy(login = value)) }

    fun onPasswordChange(value: String) =
        _state.update { it.copy(loginForm = it.loginForm.copy(password = value)) }

    fun onRegisterLoginChange(value: String) =
        _state.update { it.copy(registerForm = it.registerForm.copy(login = value)) }

    fun onRegisterPasswordChange(value: String) =
        _state.update { it.copy(registerForm = it.registerForm.copy(password = value)) }

    fun onFirstNameChange(value: String) =
        _state.update { it.copy(registerForm = it.registerForm.copy(firstName = value)) }

    fun onLastNameChange(value: String) =
        _state.update { it.copy(registerForm = it.registerForm.copy(lastName = value)) }

    fun onEmailChange(value: String) =
        _state.update { it.copy(registerForm = it.registerForm.copy(email = value)) }

    fun onImageUrlChange(value: String) =
        _state.update { it.copy(registerForm = it.registerForm.copy(imageUrl = value)) }

    fun onLangKeyChange(value: String) =
        _state.update { it.copy(registerForm = it.registerForm.copy(langKey = value)) }

    // La pantalla arma el comando con lo que hay escrito y no con nada mas: el
    // recorte y el null de los opcionales los resuelve el caso de uso.
    fun submitLogin() = attempt {
        val form = _state.value.loginForm
        loginUseCase(LoginCommand(login = form.login, password = form.password))

        // Se limpia recien cuando entro: si el intento fallo, lo escrito se queda
        // para que el usuario no tenga que volver a tipearlo.
        _state.update { it.copy(loginForm = LoginForm()) }
        _events.tryEmit(AuthEvent.LoggedIn)
    }

    fun submitRegister() = attempt {
        val form = _state.value.registerForm
        val user = registerUseCase(
            RegisterCommand(
                login = form.login,
                password = form.password,
                firstName = form.firstName,
                lastName = form.lastName,
                email = form.email,
                imageUrl = form.imageUrl,
                langKey = form.langKey,
            ),
        )

        // Se avisa con el login que devolvio el servidor, no con el que escribio el
        // usuario: si el backend lo normalizo, ahi se ve.
        _state.update {
            it.copy(
                registerForm = RegisterForm(),
                notice = "Cuenta creada para ${user.login}. Iniciá sesión.",
            )
        }
        _events.tryEmit(AuthEvent.Registered)
    }

    // No es un formulario, asi que no entra por attempt: no hay campos que validar
    // ni estado de envio que mostrar en pantalla.
    fun logout() {
        viewModelScope.launch {
            try {
                logoutUseCase()

                // Ademas del token se borra lo que quedo escrito: nadie deberia
                // heredar una contrasena de la sesion que acaba de cerrar.
                _state.update {
                    it.copy(
                        loginForm = LoginForm(),
                        registerForm = RegisterForm(),
                        fieldErrors = emptyMap(),
                        error = null,
                        notice = null,
                    )
                }
                _events.tryEmit(AuthEvent.SessionClosed)
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                _state.update { it.copy(error = AppError.Connectivity()) }
            }
        }
    }

    // Un solo camino de errores para los dos formularios: validacion del cliente,
    // error tipado del backend, o cualquier otra falla que no puede tumbar la app.
    private fun attempt(work: suspend () -> Unit) {
        if (_state.value.submitting) return
        _state.update {
            it.copy(submitting = true, fieldErrors = emptyMap(), error = null, notice = null)
        }

        viewModelScope.launch {
            try {
                work()
                _state.update { it.copy(submitting = false) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: FieldValidationException) {
                _state.update { it.copy(submitting = false, fieldErrors = e.fields) }
            } catch (e: ApiException) {
                val fields = e.error.asFieldErrors()
                _state.update {
                    if (fields.isNotEmpty()) {
                        it.copy(submitting = false, fieldErrors = fields)
                    } else {
                        it.copy(submitting = false, error = e.error)
                    }
                }
            } catch (_: Exception) {
                _state.update { it.copy(submitting = false, error = AppError.Connectivity()) }
            }
        }
    }
}
