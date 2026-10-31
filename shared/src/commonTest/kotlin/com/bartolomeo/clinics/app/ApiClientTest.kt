package com.bartolomeo.clinics.app

import com.bartolomeo.clinics.app.domain.error.AppError
import com.bartolomeo.clinics.app.infrastructure.network.ApiClient
import com.bartolomeo.clinics.app.infrastructure.network.ApiException
import com.bartolomeo.clinics.app.infrastructure.network.SessionTokenHolder
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

    private fun client(engine: MockEngine, token: String? = null): ApiClient =
        ApiClient(
            createHttpClient(
                engine = engine,
                tokenProvider = SessionTokenHolder().apply { set(token) },
            ),
        )

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
}
