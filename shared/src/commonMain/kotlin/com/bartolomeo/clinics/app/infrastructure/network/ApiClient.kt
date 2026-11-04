package com.bartolomeo.clinics.app.infrastructure.network

import com.bartolomeo.clinics.app.domain.error.AppError
import io.ktor.client.HttpClient
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode

// Punto único de entrada a la red. Toda llamada que no sea 2xx se convierte en
// ApiException con el AppError del problem+json, así ningún call site puede
// olvidarse del contrato de errores.
class ApiClient(private val http: HttpClient) {

    suspend fun get(url: String, block: HttpRequestBuilder.() -> Unit = {}): HttpResponse =
        checked(http.get(url, block))

    suspend fun post(url: String, block: HttpRequestBuilder.() -> Unit = {}): HttpResponse =
        checked(http.post(url, block))

    private suspend fun checked(response: HttpResponse): HttpResponse {
        val status = response.status.value
        if (status !in HttpStatusCode.OK.value..HttpStatusCode.Accepted.value) {
            throw ApiException(ApiProblemMapper.toAppError(status, response.bodyAsText()))
        }
        return response
    }
}
