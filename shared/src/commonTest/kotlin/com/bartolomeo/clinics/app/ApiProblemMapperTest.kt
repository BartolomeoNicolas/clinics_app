package com.bartolomeo.clinics.app

import com.bartolomeo.clinics.app.domain.error.AppError
import com.bartolomeo.clinics.app.domain.error.FieldViolation
import com.bartolomeo.clinics.app.infrastructure.network.ApiProblemMapper
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ApiProblemMapperTest {

    @Test
    fun conservaElCodeEstableDeUnConflicto() {
        val body = """
            {
              "type": "https://www.jhipster.tech/problem/problem-with-message",
              "title": "Conflict",
              "status": 409,
              "detail": "Slot already held",
              "message": "error.http.409",
              "path": "/api/appointment-holds",
              "code": "SLOT_ALREADY_HELD"
            }
        """.trimIndent()

        val error = ApiProblemMapper.toAppError(status = 409, body = body)

        assertEquals(AppError.Backend(status = 409, code = "SLOT_ALREADY_HELD"), error)
    }

    @Test
    fun convierteLosFieldErrorsDeUnaValidacion() {
        val body = """
            {
              "type": "https://www.jhipster.tech/problem/constraint-violation",
              "title": "Method argument not valid",
              "status": 400,
              "message": "error.validation",
              "path": "/api/appointment-holds",
              "code": "VALIDATION_ERROR",
              "fieldErrors": [
                {"objectName": "appointmentHoldRequestDTO", "field": "professionalId", "message": "must not be null"}
              ]
            }
        """.trimIndent()

        val error = ApiProblemMapper.toAppError(status = 400, body = body) as AppError.Backend

        assertEquals("VALIDATION_ERROR", error.code)
        assertEquals(listOf(FieldViolation("professionalId", "must not be null")), error.fieldErrors)
    }

    @Test
    fun toleraUn401SinCodePorqueSpringSecurityNoLoGarantiza() {
        val error = ApiProblemMapper.toAppError(status = 401, body = null)

        assertEquals(AppError.Backend(status = 401, code = null), error)
        assertNull((error as AppError.Backend).code)
    }

    @Test
    fun noRompeConUnBodyQueNoEsProblemJson() {
        val error = ApiProblemMapper.toAppError(status = 502, body = "<html>Bad gateway</html>")

        assertEquals(AppError.Backend(status = 502, code = null), error)
    }

    @Test
    fun respetaElStatusDelBodyCuandoDifiereDelHttp() {
        val body = """{"status": 423, "code": "PROCESS_EXPIRED", "detail": "expired"}"""

        val error = ApiProblemMapper.toAppError(status = 400, body = body) as AppError.Backend

        assertEquals(423, error.status)
        assertEquals("PROCESS_EXPIRED", error.code)
    }
}
