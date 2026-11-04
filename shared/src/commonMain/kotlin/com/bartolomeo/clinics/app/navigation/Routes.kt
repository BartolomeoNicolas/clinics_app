package com.bartolomeo.clinics.app.navigation

// Rutas del flujo completo del enunciado 5: sesión, búsqueda, detalle,
// disponibilidad, reserva con solicitud de teléfono, resultado, mis reservas y
// estado de sincronización.
object Routes {

    const val ARG_PROFESSIONAL_ID = "professionalId"
    const val ARG_HOLD_ID = "holdId"
    const val ARG_APPOINTMENT_ID = "appointmentId"

    const val LOGIN = "login"
    const val REGISTER = "register"

    const val SEARCH = "search"
    const val PROFESSIONAL = "professional/{$ARG_PROFESSIONAL_ID}"
    const val AVAILABILITY = "availability/{$ARG_PROFESSIONAL_ID}"

    const val BOOKING_WAIT = "booking/wait/{$ARG_HOLD_ID}"
    const val PHONE_REQUEST = "booking/phone/{$ARG_HOLD_ID}"
    const val BOOKING_RESULT = "booking/result/{$ARG_APPOINTMENT_ID}"

    const val MY_BOOKINGS = "bookings"
    const val SYNC_STATUS = "sync"

    fun professional(professionalId: String) = "professional/$professionalId"

    fun availability(professionalId: String) = "availability/$professionalId"

    fun bookingWait(holdId: String) = "booking/wait/$holdId"

    fun phoneRequest(holdId: String) = "booking/phone/$holdId"

    fun bookingResult(appointmentId: String) = "booking/result/$appointmentId"
}
