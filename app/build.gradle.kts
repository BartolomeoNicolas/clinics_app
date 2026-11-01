import java.util.Properties

plugins {
    // AGP 9 trae Kotlin integrado: NO va org.jetbrains.kotlin.android.
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
}

// La IP en LAN de la maquina de desarrollo varia por equipo, así que se lee de
// local.properties (gitignored) y solo tiene un valor por defecto de respaldo.
// Nunca se escribe en código Kotlin: el baseUrl vive en BuildConfig.
val lanHost: String = run {
    val file = rootProject.file("local.properties")
    if (file.exists()) {
        Properties().apply { file.inputStream().use { load(it) } }.getProperty("lan.host")
    } else {
        null
    }
} ?: "192.168.1.100"

fun backendUrls(host: String): Pair<String, String> {
    val booking = "http://${host}:${libs.versions.bookingPort.get()}"
    val sync = "http://${host}:${libs.versions.syncPort.get()}"
    return "\"$booking\"" to "\"$sync\""
}

android {
    namespace = "com.bartolomeo.clinics.app"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.bartolomeo.clinics.app"
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        versionCode = 1
        versionName = "0.1.0"
    }

    buildFeatures {
        buildConfig = true
    }

    // Cada destino es un build config distinto: el emulador resuelve el host a
    // 10.0.2.2, un dispositivo físico habla con la máquina por la LAN.
    flavorDimensions += "destino"

    productFlavors {
        create("emulador") {
            dimension = "destino"
            val (booking, sync) = backendUrls("10.0.2.2")
            buildConfigField("String", "BOOKING_BASE_URL", booking)
            buildConfigField("String", "SYNC_BASE_URL", sync)
        }
        create("dispositivo") {
            dimension = "destino"
            val (booking, sync) = backendUrls(lanHost)
            buildConfigField("String", "BOOKING_BASE_URL", booking)
            buildConfigField("String", "SYNC_BASE_URL", sync)
        }
    }
}

dependencies {
    implementation(project(":shared"))
    implementation(libs.androidx.activity.compose)
    // viewModel { } se llama en MainActivity: sin esto el composition root no podria
    // crear el ViewModel, y el de :shared lo trae con scope implementation.
    implementation(libs.lifecycle.viewmodel.compose)
}
