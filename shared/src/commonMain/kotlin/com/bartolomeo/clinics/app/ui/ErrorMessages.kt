package com.bartolomeo.clinics.app.ui

import com.bartolomeo.clinics.app.domain.error.AppError

// Un solo lugar con los textos al usuario. Se decide por status y code; el texto
// de `detail` del backend nunca se muestra tal cual.
fun AppError.userMessage(): String = when (this) {
    is AppError.Connectivity ->
        if (timedOut) "Se agotó el tiempo de espera. Reintentá." else "Sin conexión con el servidor."

    is AppError.Backend -> when (status) {
        400 -> "Revisá los datos ingresados."
        401 -> "Tu sesión expiró. Iniciá sesión de nuevo."
        403 -> "No tenés permiso para esta operación."
        404 -> "No encontramos lo que buscás."
        409 -> "La operación entra en conflicto con el estado actual."
        in 500..599 -> "El servidor tuvo un problema. Reintentá en un momento."
        else -> "No se pudo completar la operación."
    }

    is AppError.Unexpected -> "El servidor respondió de una forma que no esperábamos."
}
