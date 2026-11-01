package com.bartolomeo.clinics.app

import com.bartolomeo.clinics.app.application.usecase.FieldValidationException
import com.bartolomeo.clinics.app.application.usecase.LoginUseCase
import com.bartolomeo.clinics.app.application.usecase.LogoutUseCase
import com.bartolomeo.clinics.app.application.usecase.RegisterUseCase
import com.bartolomeo.clinics.app.application.usecase.RestoreSessionUseCase
import com.bartolomeo.clinics.app.domain.model.LoginCommand
import com.bartolomeo.clinics.app.domain.model.RegisterCommand
import com.bartolomeo.clinics.app.domain.model.Session
import com.bartolomeo.clinics.app.domain.model.User
import com.bartolomeo.clinics.app.domain.ports.out.AuthApiPort
import com.bartolomeo.clinics.app.domain.ports.out.SessionStore
import com.bartolomeo.clinics.app.domain.ports.out.SessionTokenProvider
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

// El token es lo unico que el store puede contener y lo unico que una prueba puede
// mirar: nunca se compara contra el texto de una pantalla ni contra un log.
class AuthUseCasesTest {

    private class RecordingAuthApi(
        val issuedToken: String = "tok-de-prueba",
        val registeredUser: User = sampleUser,
    ) : AuthApiPort {
        var logins = 0
        var registrations = 0
        var lastLogin: LoginCommand? = null
        var lastRegister: RegisterCommand? = null

        override suspend fun register(command: RegisterCommand): User {
            registrations += 1
            lastRegister = command
            return registeredUser
        }

        override suspend fun login(command: LoginCommand): Session {
            logins += 1
            lastLogin = command
            return Session(token = issuedToken, expiresAtEpochMillis = 999_999L)
        }
    }

    private class MemorySessionStore : SessionStore {
        var token: String? = null
        var expiresAt: Long? = null
        var saves = 0
        var clears = 0

        override suspend fun token(): String? = token
        override suspend fun save(token: String, expiresAtEpochMillis: Long?) {
            saves += 1
            this.token = token
            this.expiresAt = expiresAtEpochMillis
        }

        override suspend fun clear() {
            clears += 1
            token = null
            expiresAt = null
        }
    }

    @Test
    fun loginOkGuardaElTokenEnElAlmacenamiento() = runTest {
        val api = RecordingAuthApi(issuedToken = "jwt-emitido")
        val store = MemorySessionStore()

        val session = LoginUseCase(api, store)(
            LoginCommand(login = "nbartolomeo", password = "clave1234"),
        )

        assertEquals("jwt-emitido", session.token)
        assertEquals(1, api.logins)
        assertEquals(1, store.saves)
        assertEquals("jwt-emitido", store.token)
        assertEquals(999_999L, store.expiresAt)
    }

    @Test
    fun loginInvalidoCortaAntesDeLlamarAlBackendNiGuardarNada() = runTest {
        val api = RecordingAuthApi()
        val store = MemorySessionStore()

        // El login de sesion acepta desde 1 caracter, asi que se vacia el campo
        // para que el rechazo venga del NotBlank y no de un limite de tamano.
        val rejected = assertFailsWith<FieldValidationException> {
            LoginUseCase(api, store)(LoginCommand(login = "  ", password = "123"))
        }

        assertEquals(
            mapOf(
                "login" to "El login es obligatorio",
                "password" to "La contrasena debe tener entre 4 y 100 caracteres",
            ),
            rejected.fields,
        )
        assertEquals(0, api.logins, "un formulario invalido no debe costarle un viaje al servidor")
        assertEquals(0, store.saves)
        assertNull(store.token)
    }

    @Test
    fun registroInvalidoNoLlegaAlBackend() = runTest {
        val api = RecordingAuthApi()

        val rejected = assertFailsWith<FieldValidationException> {
            RegisterUseCase(api)(sampleCommand.copy(email = "sin-arroba"))
        }

        assertEquals(
            mapOf("email" to "El email no tiene un formato valido"),
            rejected.fields,
        )
        assertEquals(0, api.registrations)
    }

    @Test
    fun registroValidoDevuelveElUsuarioCanonicoDelBackend() = runTest {
        val api = RecordingAuthApi()

        val user = RegisterUseCase(api)(sampleCommand)

        assertEquals(1, api.registrations)
        assertEquals("nbartolomeo", user.login)
        assertEquals("Nicolas", user.firstName)
        assertEquals(listOf("ROLE_USER"), user.authorities)
        assertTrue(user.activated)
    }

    // Lo que se valida tiene que ser lo que se manda: si el usuario escribe el
    // login con espacios alrededor, el backend recibe el recortado y valida ese.
    // Validar sin recortar aprobaria un login que alla rechazaria.
    @Test
    fun laValidacionCorreSobreElValorQueSeManda() = runTest {
        val api = RecordingAuthApi()

        val rejected = assertFailsWith<FieldValidationException> {
            // 6 caracteres con espacios (pasarian el minimo de 3 del registro),
            // pero "ab" recortado tiene 2 y el backend lo rechazaria.
            RegisterUseCase(api)(sampleCommand.copy(login = "  ab  "))
        }

        assertEquals(
            mapOf("login" to "El login debe tener entre 3 y 50 caracteres"),
            rejected.fields,
        )
        assertEquals(0, api.registrations, "un login que alla no aceptaria no debe viajar")
    }

    @Test
    fun alEntrarElLoginLlegaRecortadoAlServidor() = runTest {
        val api = RecordingAuthApi()

        LoginUseCase(api, MemorySessionStore())(
            LoginCommand(login = "  nbartolomeo  ", password = "clave1234"),
        )

        assertEquals("nbartolomeo", api.lastLogin?.login)
    }

    @Test
    fun alRegistrarSeRecortanLosCamposYLosVaciosNoSeMandan() = runTest {
        val api = RecordingAuthApi()

        RegisterUseCase(api)(
            sampleCommand.copy(
                login = "  nbartolomeo  ",
                firstName = "  Nicolas  ",
                email = "  nico@fi.uba.ar  ",
                imageUrl = "   ", // solo espacios: cuenta como vacio
                langKey = "  ", // idem: sin esto el Size del backend rechazaria
            ),
        )

        val sent = assertNotNull(api.lastRegister)
        assertEquals("nbartolomeo", sent.login)
        assertEquals("Nicolas", sent.firstName)
        assertEquals("nico@fi.uba.ar", sent.email)
        assertNull(sent.imageUrl, "un opcionales vacio se manda como null")
        assertNull(sent.langKey, "un opcionales vacio se manda como null")
    }

    @Test
    fun unOpcionalConValorSeMandaRecortadoYNoNulo() = runTest {
        val api = RecordingAuthApi()

        RegisterUseCase(api)(sampleCommand.copy(langKey = "  es  "))

        assertEquals("es", assertNotNull(api.lastRegister).langKey)
    }

    @Test
    fun cerrarSesionBorraElTokenPorCompleto() = runTest {
        val store = MemorySessionStore()
        store.token = "jwt-vigente"
        store.expiresAt = 123L

        LogoutUseCase(store)()

        assertEquals(1, store.clears)
        assertNull(store.token, "el token tiene que desaparecer, no quedar enmascarado")
        assertNull(store.expiresAt)
        assertNull(store.token())
    }

    @Test
    fun restaurarSesionDetectaCuandoQuedaAlgoGuardado() = runTest {
        val store = MemorySessionStore()

        assertFalse(RestoreSessionUseCase(store)(), "store vacio: hay que pedir login")

        store.token = "jwt-vigente"
        assertTrue(RestoreSessionUseCase(store)())
    }

    @Test
    fun laLecturaCompartidaVeElMismoTokenQueGuardoElLogin() = runTest {
        val store = MemorySessionStore()
        val http: SessionTokenProvider = store

        LoginUseCase(RecordingAuthApi(issuedToken = "jwt-compartido"), store)(
            LoginCommand(login = "nbartolomeo", password = "clave1234"),
        )

        // Este es el camino que usa el interceptor de Ktor: si no ve el token, la
        // autenticacion no llega al backend por mas que el login salio bien.
        assertEquals("jwt-compartido", http.token())
    }

    private companion object {
        val sampleCommand = RegisterCommand(
            login = "nbartolomeo",
            password = "clave1234",
            firstName = "Nicolas",
            lastName = "Bartolomeo",
            email = "nico@fi.uba.ar",
        )

        val sampleUser = User(
            id = 7L,
            login = "nbartolomeo",
            firstName = "Nicolas",
            lastName = "Bartolomeo",
            email = "nico@fi.uba.ar",
            imageUrl = null,
            langKey = "es",
            activated = true,
            authorities = listOf("ROLE_USER"),
        )
    }
}
