package com.bartolomeo.clinics.app.infrastructure.network

import com.bartolomeo.clinics.app.domain.error.AppError
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import kotlin.coroutines.cancellation.CancellationException

// Punto unico de entrada a la red. Toda llamada fallida sale de aca como
// ApiException: o el status con el AppError del problem+json, o un fallo de
// conectividad tipado. Ningun call site tiene que atrapar excepciones sueltas de
// Ktor, y ninguna puede olvidarse del contrato de errores.
class ApiClient(private val http: HttpClient) {

    suspend fun get(url: String, block: HttpRequestBuilder.() -> Unit = {}): HttpResponse =
        execute { http.get(url, block) }

    suspend fun post(url: String, block: HttpRequestBuilder.() -> Unit = {}): HttpResponse =
        execute { http.post(url, block) }

    private suspend fun execute(call: suspend () -> HttpResponse): HttpResponse {
        val response = transportSafe(call)

        val status = response.status.value
        if (status !in HttpStatusCode.OK.value..HttpStatusCode.Accepted.value) {
            throw ApiException(ApiProblemMapper.toAppError(status, response.bodyAsText()))
        }

        return response
    }

    // Ktor lanza excepciones de la plataforma para cortes, DNS y TLS, y en comun no
    // se pueden nombrar todas. Cualquier fallo que no tenga respuesta utilizable es
    // un problema de conectividad, no un error del backend: no hay status ni code
    // que leer, y la pantalla no debe inventar uno.
    private suspend fun transportSafe(call: suspend () -> HttpResponse): HttpResponse = try {
        call()
    } catch (e: CancellationException) {
        throw e
    } catch (e: ApiException) {
        throw e
    } catch (e: HttpRequestTimeoutException) {
        throw ApiException(AppError.Connectivity(timedOut = true))
    } catch (_: Exception) {
        throw ApiException(AppError.Connectivity(timedOut = false))
    }
}
