package com.bartolomeo.clinics.app.application.usecase

// Rechazo de la validacion del cliente. Lleva el mismo shape que los fieldErrors
// del problem+json, asi la pantalla solo tiene una idea de "campo con mensaje" y no
// distingue si vino de aca o del backend.
class FieldValidationException(val fields: Map<String, String>) :
    Exception("Datos de formulario invalidos")
