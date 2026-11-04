package com.bartolomeo.clinics.app.infrastructure.network

import com.bartolomeo.clinics.app.domain.port.SessionTokenProvider
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json

fun createHttpClient(
    engine: HttpClientEngine,
    tokenProvider: SessionTokenProvider,
): HttpClient = HttpClient(engine) {

    // Los errores los traduce ApiClient; que el plugin no lance primero es lo
    // que permite leer el problem+json del cuerpo.
    expectSuccess = false

    install(ContentNegotiation) {
        json(apiJson)
    }

    install(HttpTimeout) {
        requestTimeoutMillis = 15_000
        connectTimeoutMillis = 5_000
        socketTimeoutMillis = 15_000
    }

    install(HttpRequestRetry) {
        // Reintentos acotados y solo ante 5xx: un 4xx depende de lo que mandó el
        // cliente y reintentarlo no arregla nada.
        maxRetries = 2
        retryOnServerErrors()
        delayMillis { attempt -> 250L * attempt }
    }

    install(sessionTokenPlugin(tokenProvider))
}
