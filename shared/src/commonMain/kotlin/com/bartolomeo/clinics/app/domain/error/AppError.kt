package com.bartolomeo.clinics.app.domain.error

// El contrato (anexo 13) obliga a decidir por `status` y `code`, nunca por el
// texto de `detail`, que puede cambiar o estar en otro idioma.
sealed interface AppError {

    /** `code` del problem+json. Los 401 y 403 de Spring Security no lo garantizan. */
    val code: String?

    /** El backend respondió con un status de error, con o sin problem+json. */
    data class Backend(
        val status: Int,
        override val code: String? = null,
        val fieldErrors: List<FieldViolation> = emptyList(),
    ) : AppError

    /** Nunca llegó una respuesta utilizable: sin red, host inalcanzable o timeout. */
    data class Connectivity(
        val timedOut: Boolean = false,
        override val code: String? = null,
    ) : AppError

    /** Respondió algo que no es el problem+json del contrato. */
    data class Unexpected(
        val status: Int,
        override val code: String? = null,
    ) : AppError
}

data class FieldViolation(
    val field: String,
    val message: String,
)
