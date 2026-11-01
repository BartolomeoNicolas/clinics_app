package com.bartolomeo.clinics.app

import com.bartolomeo.clinics.app.domain.error.AppError
import com.bartolomeo.clinics.app.infrastructure.network.ApiClient
import com.bartolomeo.clinics.app.infrastructure.network.ApiException
import com.bartolomeo.clinics.app.domain.ports.out.SessionTokenProvider
import com.bartolomeo.clinics.app.infrastructure.network.createHttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.get
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class ApiClientTest {

    private val problemJson = """
        {
          "title": "Conflict",
          "status": 409,
          "detail": "Slot already held",
          "code": "SLOT_ALREADY_HELD"
        }
    """.trimIndent()

    // El puerto en memoria lo reemplaza el SecureTokenStore en androidMain: la
    // prueba solo necesita que el interceptor lea lo que ella responda.
    private fun client(engine: MockEngine, token: String? = null): ApiClient {
        val provider = object : SessionTokenProvider {
            override suspend fun token(): String? = token
        }
        return ApiClient(createHttpClient(engine = engine, tokenProvider = provider))
    }

    @Test
    fun unErrorDelBackendLlegaComoExcepcionTipadaConSuCode() = runTest {
        val engine = MockEngine {
            respond(problemJson, HttpStatusCode.Conflict)
        }

        val exception = assertFailsWith<ApiException> { client(engine).get(URL) }

        assertEquals(AppError.Backend(status = 409, code = "SLOT_ALREADY_HELD"), exception.error)
    }

    @Test
    fun elInterceptorInyectaElJwtDeSesion() = runTest {
        var authorization: String? = null
        val engine = MockEngine { request ->
            authorization = request.headers[HttpHeaders.Authorization]
            respond("{}", HttpStatusCode.OK)
        }

        client(engine, token = "jwt-de-prueba").get(URL)

        assertEquals("Bearer jwt-de-prueba", authorization)
    }

    @Test
    fun sinSesionNoSeMandaLaCabecera() = runTest {
        var authorization: String? = null
        val engine = MockEngine { request ->
            authorization = request.headers[HttpHeaders.Authorization]
            respond("{}", HttpStatusCode.OK)
        }

        client(engine).get(URL)

        assertNull(authorization)
    }

    @Test
    fun losReintentosQuedanAcotadosAnteUn5xx() = runTest {
        var attempts = 0
        val engine = MockEngine {
            attempts++
            respond("boom", HttpStatusCode.InternalServerError)
        }

        assertFailsWith<ApiException> { client(engine).get(URL) }

        assertEquals(3, attempts, "el intento inicial más los 2 reintentos")
    }

    @Test
    fun un4xxNoSeReintentaPorqueDependeDelCliente() = runTest {
        var attempts = 0
        val engine = MockEngine {
            attempts++
            respond("{}", HttpStatusCode.BadRequest)
        }

        assertFailsWith<ApiException> { client(engine).get(URL) }

        assertEquals(1, attempts)
    }

    private companion object {
        const val URL = "http://localhost:8082/api/prueba"
    }

    // Ktor lanza excepciones de la plataforma para cortes, DNS o TLS. Si escapan sin
    // traducir, la pantalla no tiene status ni code que leer y se tendria que inventar
    // uno; aca salen tipadas como conectividad, que es lo que el contrato distingue.
    @Test
    fun unFalloDeRedSaleComoConectividadYPuedeReintentarse() = runTest {
        val engine = MockEngine { throw RuntimeException("Connection refused") }

        val exception = assertFailsWith<ApiException> { client(engine).get(URL) }

        val connectivity = exception.error as? AppError.Connectivity
        assertNotNull(connectivity, "un corte de red no es un error del backend")
        assertFalse(connectivity.timedOut)
        // Sin status y sin code: el mensaje de usuario lo decide la app.
        assertNull(connectivity.code)
    }
}
