package com.bartolomeo.clinics.app

import com.bartolomeo.clinics.app.domain.model.LoginCommand
import com.bartolomeo.clinics.app.domain.model.RegisterCommand
import com.bartolomeo.clinics.app.domain.validation.AuthValidation
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

// Cada mensaje esperado esta copiado literalmente del atributo `message` del DTO
// correspondiente del backend (RegisterRequest.java y LoginRequest.java). Si alguno
// cambia alla, falla aca: ese es exactamente el criterio de que el usuario lea lo
// mismo venga del cliente o del servidor.
class AuthValidationTest {

    private fun register(
        login: String = "nbartolomeo",
        password: String = "clave1234",
        firstName: String = "Nicolas",
        lastName: String = "Bartolomeo",
        email: String = "nico@fi.uba.ar",
        imageUrl: String? = null,
        langKey: String? = "es",
    ) = RegisterCommand(login, password, firstName, lastName, email, imageUrl, langKey)

    private fun login(
        login: String = "nbartolomeo",
        password: String = "clave1234",
    ) = LoginCommand(login, password)

    @Test
    fun loginVacioUsaElMensajeDeNotBlank() {
        assertEquals(
            mapOf("login" to "El login es obligatorio"),
            AuthValidation.register(register(login = "")),
        )
    }

    @Test
    fun loginDemasiadoCortoUsaElDeSize() {
        assertEquals(
            mapOf("login" to "El login debe tener entre 3 y 50 caracteres"),
            AuthValidation.register(register(login = "ab")),
        )
    }

    @Test
    fun loginDemasiadoLargoUsaElDeSize() {
        assertEquals(
            mapOf("login" to "El login debe tener entre 3 y 50 caracteres"),
            AuthValidation.register(register(login = "a".repeat(51))),
        )
    }

    @Test
    fun passwordEnElLimiteDeLaCatedraPasa() {
        // 4 caracteres: el minimo del backend.
        assertEquals(emptyMap(), AuthValidation.register(register(password = "1234")))
    }

    @Test
    fun passwordCortaUsaElMensajeDelBackend() {
        assertEquals(
            mapOf("password" to "La contrasena debe tener entre 4 y 100 caracteres"),
            AuthValidation.register(register(password = "123")),
        )
    }

    @Test
    fun passwordVaciaUsaElDeNotBlank() {
        assertEquals(
            mapOf("password" to "La contrasena es obligatoria"),
            AuthValidation.register(register(password = "")),
        )
    }

    @Test
    fun nombreVacioYPasadoDe50UsanSusMensajes() {
        assertEquals(
            mapOf("firstName" to "El nombre es obligatorio"),
            AuthValidation.register(register(firstName = "   ")),
        )
        assertEquals(
            mapOf("firstName" to "El nombre no puede pasar de 50 caracteres"),
            AuthValidation.register(register(firstName = "a".repeat(51))),
        )
    }

    @Test
    fun apellidoVacioYPasadoDe50UsanSusMensajes() {
        assertEquals(
            mapOf("lastName" to "El apellido es obligatorio"),
            AuthValidation.register(register(lastName = "")),
        )
        assertEquals(
            mapOf("lastName" to "El apellido no puede pasar de 50 caracteres"),
            AuthValidation.register(register(lastName = "b".repeat(51))),
        )
    }

    @Test
    fun emailVacioUsaElDeNotBlank() {
        assertEquals(
            mapOf("email" to "El email es obligatorio"),
            AuthValidation.register(register(email = "")),
        )
    }

    @Test
    fun emailSinArrobaUsaElDeFormato() {
        assertEquals(
            mapOf("email" to "El email no tiene un formato valido"),
            AuthValidation.register(register(email = "nico-alfasincarroba")),
        )
    }

    @Test
    fun emailValidoPeroCortoUsaElDeSize() {
        // "a@b" pasa el patron de jakarta pero tiene menos de 5 caracteres: ahi
        // manda el Size, y en ese mismo orden lo reporta el servidor.
        assertEquals(
            mapOf("email" to "El email debe tener entre 5 y 254 caracteres"),
            AuthValidation.register(register(email = "a@b")),
        )
    }

    @Test
    fun camposOpcionalesVaciosNoGeneranError() {
        assertEquals(
            emptyMap(),
            AuthValidation.register(register(imageUrl = null, langKey = null)),
        )
    }

    @Test
    fun langKeyEnElRangoDeLaCatedraPasa() {
        assertEquals(emptyMap(), AuthValidation.register(register(langKey = "es")))
        assertEquals(emptyMap(), AuthValidation.register(register(langKey = "a".repeat(10))))
    }

    @Test
    fun langKeyFueraDeRangoUsaElMensajeDelBackend() {
        assertEquals(
            mapOf("langKey" to "El idioma debe tener entre 2 y 10 caracteres"),
            AuthValidation.register(register(langKey = "a")),
        )
        assertEquals(
            mapOf("langKey" to "El idioma debe tener entre 2 y 10 caracteres"),
            AuthValidation.register(register(langKey = "a".repeat(11))),
        )
    }

    @Test
    fun imagenDePerfilMasLargaQue255Rechaza() {
        assertEquals(
            mapOf("imageUrl" to "La URL de imagen no puede pasar de 255 caracteres"),
            AuthValidation.register(register(imageUrl = "https://x/" + "a".repeat(250))),
        )
    }

    @Test
    fun unFormularioValidoNoTieneNadaQueReclamar() {
        assertEquals(emptyMap(), AuthValidation.register(register()))
    }

    @Test
    fun elLoginDeSesionAceptaDesdeUnCaracter() {
        // LoginRequest pide 1..254, distinto del registro que pide 3..50.
        assertEquals(emptyMap(), AuthValidation.login(login(login = "n")))
        assertEquals(
            mapOf("login" to "El login debe tener entre 1 y 254 caracteres"),
            AuthValidation.login(login(login = "a".repeat(255))),
        )
    }

    @Test
    fun elPasswordDeSesionUsaElMismoRangoQueElDeRegistro() {
        assertEquals(
            mapOf("password" to "La contrasena debe tener entre 4 y 100 caracteres"),
            AuthValidation.login(login(password = "123")),
        )
        assertEquals(
            mapOf("password" to "La contrasena es obligatoria"),
            AuthValidation.login(login(password = "")),
        )
    }

    @Test
    fun seReportaUnSoloMensajePorCampo() {
        // Un login vacio viola NotBlank y Size a la vez; el backend manda los dos
        // y al usuario le conviene solo el primero, que es el que explica que falto.
        val rejected = AuthValidation.register(register(login = "", password = ""))
        assertEquals(2, rejected.size)
        assertTrue(rejected.values.all { it.isNotBlank() })
    }
}
