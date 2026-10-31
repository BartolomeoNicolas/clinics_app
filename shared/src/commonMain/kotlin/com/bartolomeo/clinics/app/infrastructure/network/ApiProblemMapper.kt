package com.bartolomeo.clinics.app.infrastructure.network

import com.bartolomeo.clinics.app.domain.error.AppError
import com.bartolomeo.clinics.app.domain.error.FieldViolation

object ApiProblemMapper {

    /**
     * Traduce un status y su body a error de dominio.
     *
     * Un backend que no cumple el contrato (body vacío, HTML de un proxy, JSON
     * sin `code`) no rompe a la app: se conserva el status, que sí es fiable.
     */
    fun toAppError(status: Int, body: String?): AppError {
        val problem = body?.let(::decode) ?: return AppError.Backend(status = status)

        return AppError.Backend(
            status = problem.status ?: status,
            code = problem.code,
            fieldErrors = problem.fieldErrors.mapNotNull(::toViolation),
        )
    }

    private fun toViolation(error: ApiFieldError): FieldViolation? =
        error.field?.let { FieldViolation(field = it, message = error.message.orEmpty()) }

    private fun decode(body: String): ApiProblem? =
        runCatching { apiJson.decodeFromString<ApiProblem>(body) }.getOrNull()
}
