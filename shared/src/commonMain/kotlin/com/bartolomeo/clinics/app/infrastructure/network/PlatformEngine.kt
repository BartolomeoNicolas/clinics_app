package com.bartolomeo.clinics.app.infrastructure.network

import io.ktor.client.engine.HttpClientEngine

// El motor es de plataforma. El resto del código solo conoce la interfaz de Ktor,
// así que cuando entre una segunda plataforma no se toca el cliente.
internal expect fun platformHttpEngine(): HttpClientEngine
