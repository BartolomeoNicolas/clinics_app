package com.bartolomeo.clinics.app.infrastructure.network

import com.bartolomeo.clinics.app.domain.model.LoginCommand
import com.bartolomeo.clinics.app.domain.model.RegisterCommand
import com.bartolomeo.clinics.app.domain.model.Session
import com.bartolomeo.clinics.app.domain.model.User
import com.bartolomeo.clinics.app.domain.ports.out.AuthApiPort
import io.ktor.client.call.body
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

// Adaptador saliente: traduce los comandos del dominio a los payloads del contrato.
// Todas las respuestas pasan por ApiClient, asi que un 4xx o 5xx sale de aca como
// ApiException y no hay que atraparlo en este archivo.
class AuthApiAdapter(private val api: ApiClient, private val baseUrl: String) : AuthApiPort {

    override suspend fun register(command: RegisterCommand): User {
        val response = api.post("$baseUrl/api/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(
                RegisterPayload(
                    login = command.login,
                    password = command.password,
                    firstName = command.firstName,
                    lastName = command.lastName,
                    email = command.email,
                    imageUrl = command.imageUrl,
                    langKey = command.langKey,
                ),
            )
        }

        // 201 con el usuario canonico que armo el servidor: los campos no los
        // rellenamos nosotros, van tal cual los devolvio.
        return response.body<UserPayload>().toDomain()
    }

    override suspend fun login(command: LoginCommand): Session {
        val response = api.post("$baseUrl/api/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginPayload(login = command.login, password = command.password))
        }

        val payload = response.body<SessionPayload>()
        val issuedAt = nowEpochMillis()

        return Session(
            token = payload.idToken,
            expiresAtEpochMillis = payload.expiresIn?.let { issuedAt + it * 1_000 },
        )
    }
}

private fun UserPayload.toDomain() = User(
    id = id ?: 0L,
    login = login,
    firstName = firstName.orEmpty(),
    lastName = lastName.orEmpty(),
    email = email.orEmpty(),
    imageUrl = imageUrl,
    langKey = langKey,
    activated = activated ?: false,
    authorities = authorities,
)

internal expect fun nowEpochMillis(): Long
