package com.bartolomeo.clinics.app.infrastructure.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// El registro va en camelCase, que es como JavaSerializa el DTO del backend.
@Serializable
data class RegisterPayload(
    val login: String,
    val password: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val imageUrl: String? = null,
    val langKey: String? = null,
)

@Serializable
data class LoginPayload(
    val login: String,
    val password: String,
)

// La sesion llega en snake_case: los nombres de wire respetan JSON y los de codigo
// la convencion del lenguaje (lo que ya hace el backend con @JsonProperty).
@Serializable
data class SessionPayload(
    @SerialName("id_token") val idToken: String,
    @SerialName("token_type") val tokenType: String? = null,
    @SerialName("expires_in") val expiresIn: Long? = null,
    val scope: String? = null,
)

@Serializable
data class UserPayload(
    val id: Long? = null,
    val login: String,
    val firstName: String? = null,
    val lastName: String? = null,
    val email: String? = null,
    val imageUrl: String? = null,
    val langKey: String? = null,
    val activated: Boolean? = null,
    val authorities: List<String> = emptyList(),
)
