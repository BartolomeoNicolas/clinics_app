package com.bartolomeo.clinics.app.infrastructure.network

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

// Forma exacta del problem+json del anexo 13. Campos opcionales porque ni el
// 401 ni el 403 de Spring Security garantizan todos ellos.
@Serializable
data class ApiProblem(
    val type: String? = null,
    val title: String? = null,
    val status: Int? = null,
    val detail: String? = null,
    val message: String? = null,
    val path: String? = null,
    val code: String? = null,
    val fieldErrors: List<ApiFieldError> = emptyList(),
)

@Serializable
data class ApiFieldError(
    val objectName: String? = null,
    val field: String? = null,
    val message: String? = null,
)

// Un solo Json para parsear y para ContentNegotiation. Los campos desconocidos se
// ignoran a propósito: el backend puede agregarse y eso no debe romper a la app.
internal val apiJson = Json {
    ignoreUnknownKeys = true
    isLenient = true
}
