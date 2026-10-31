pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "clinics_app"

// AGP 9 ya no permite aplicar org.jetbrains.kotlin.multiplatform junto con
// com.android.application en el mismo módulo, así que el entry point de Android
// va aparte del código compartido: :app con la Activity, :shared con commonMain
// y androidMain.
include(":app", ":shared")
