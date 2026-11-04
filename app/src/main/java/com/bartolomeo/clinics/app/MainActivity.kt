package com.bartolomeo.clinics.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // El destino lo decide el build config; en el código no hay ninguna URL.
        val config = AppConfig(
            bookingBaseUrl = BuildConfig.BOOKING_BASE_URL,
            syncBaseUrl = BuildConfig.SYNC_BASE_URL,
        )

        setContent {
            ClinicsApp(config = config)
        }
    }
}
