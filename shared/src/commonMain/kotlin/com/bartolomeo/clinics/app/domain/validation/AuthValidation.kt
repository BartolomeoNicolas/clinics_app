package com.bartolomeo.clinics.app.domain.validation

import com.bartolomeo.clinics.app.domain.model.LoginCommand
import com.bartolomeo.clinics.app.domain.model.RegisterCommand

// Los limites y los textos son los de RegisterRequest.java y LoginRequest.java del
// backend, en el mismo orden en que estan declarados: el criterio es que el usuario
// lea el mismo mensaje venga del cliente o del servidor. Por eso los mensajes van
// escritos tal cual, sin reescribirlos ni puntuarlos.
//
// La validacion del cliente es una puerta de cortesia: evita un viaje a la red, pero
// el backend valida igual, asi que un cambio de reglas alla no lo descubre nadie.
object AuthValidation {

    // Mismo patron que aplica jakarta @Email: full match, un arroba, dominio con
    // etiquetas separadas por punto. Sin esto el cliente rechazaria correos que el
    // servidor acepta, que es peor que lo contrario.
    private val EMAIL =
        "^[A-Za-z0-9.!#\$%&'*+/=?^_`{|}~-]+@[A-Za-z0-9](?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?(?:\\.[A-Za-z0-9](?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?)*\$"

    fun register(command: RegisterCommand): Map<String, String> = buildMap {
        putViolation("login", command.login, 3, 50, "El login es obligatorio",
            "El login debe tener entre 3 y 50 caracteres")
        putViolation("password", command.password, 4, 100, "La contrasena es obligatoria",
            "La contrasena debe tener entre 4 y 100 caracteres")
        putViolation("firstName", command.firstName, null, 50, "El nombre es obligatorio",
            "El nombre no puede pasar de 50 caracteres")
        putViolation("lastName", command.lastName, null, 50, "El apellido es obligatorio",
            "El apellido no puede pasar de 50 caracteres")
        putEmail(command.email)
        putOptionalLength("imageUrl", command.imageUrl, 0, 255,
            "La URL de imagen no puede pasar de 255 caracteres")
        putOptionalLength("langKey", command.langKey, 2, 10,
            "El idioma debe tener entre 2 y 10 caracteres")
    }

    fun login(command: LoginCommand): Map<String, String> = buildMap {
        putViolation("login", command.login, 1, 254, "El login es obligatorio",
            "El login debe tener entre 1 y 254 caracteres")
        putViolation("password", command.password, 4, 100, "La contrasena es obligatoria",
            "La contrasena debe tener entre 4 y 100 caracteres")
    }

    // El backend reporta todos los constraints violados de un mismo campo; para el
    // usuario conviene uno solo, el primero, que es el mas cercano a lo que falto.
    private fun MutableMap<String, String>.putViolation(
        field: String,
        value: String,
        min: Int?,
        max: Int,
        blankMessage: String,
        sizeMessage: String,
    ) {
        if (value.isBlank()) {
            put(field, blankMessage)
            return
        }
        if ((min != null && value.length < min) || value.length > max) {
            put(field, sizeMessage)
        }
    }

    private fun MutableMap<String, String>.putEmail(email: String) {
        if (email.isBlank()) {
            put("email", "El email es obligatorio")
            return
        }
        if (!EMAIL.toRegex().matches(email)) {
            put("email", "El email no tiene un formato valido")
            return
        }
        if (email.length < 5 || email.length > 254) {
            put("email", "El email debe tener entre 5 y 254 caracteres")
        }
    }

    // Opcionales: Jakarta valida @Size sobre null sin quejarse y no hay @NotBlank,
    // asi que no mandar el campo pasa. Pero si se manda, corre el Size completo,
    // min incluido: un langKey vacio rechazaria en el servidor, y conviene que lo
    // rechace aca con el mismo texto. El caso de uso manda null cuando el campo
    // quedo vacio, que es lo que el usuario quiso decir.
    private fun MutableMap<String, String>.putOptionalLength(
        field: String,
        value: String?,
        min: Int,
        max: Int,
        sizeMessage: String,
    ) {
        if (value != null && (value.length < min || value.length > max)) {
            put(field, sizeMessage)
        }
    }
}
