package com.bartolomeo.clinics.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = ClinicsPrimary,
    onPrimary = ClinicsOnPrimary,
    primaryContainer = ClinicsPrimaryContainer,
    onPrimaryContainer = ClinicsOnPrimaryContainer,
    secondary = ClinicsSecondary,
    onSecondary = ClinicsOnSecondary,
    secondaryContainer = ClinicsSecondaryContainer,
    onSecondaryContainer = ClinicsOnSecondaryContainer,
    background = ClinicsBackground,
    onBackground = ClinicsOnBackground,
    surface = ClinicsSurface,
    onSurface = ClinicsOnSurface,
    surfaceVariant = ClinicsSurfaceVariant,
    onSurfaceVariant = ClinicsOnSurfaceVariant,
    error = ClinicsError,
    onError = ClinicsOnError,
    errorContainer = ClinicsErrorContainer,
    onErrorContainer = ClinicsOnErrorContainer,
)

private val DarkColors = darkColorScheme(
    primary = ClinicsPrimaryDark,
    onPrimary = ClinicsOnPrimaryDark,
    primaryContainer = ClinicsPrimaryContainerDark,
    onPrimaryContainer = ClinicsOnPrimaryContainerDark,
    secondary = ClinicsSecondaryDark,
    onSecondary = ClinicsOnSecondaryDark,
    secondaryContainer = ClinicsSecondaryContainerDark,
    onSecondaryContainer = ClinicsOnSecondaryContainerDark,
    background = ClinicsBackgroundDark,
    onBackground = ClinicsOnBackgroundDark,
    surface = ClinicsSurfaceDark,
    onSurface = ClinicsOnSurfaceDark,
    surfaceVariant = ClinicsSurfaceVariantDark,
    onSurfaceVariant = ClinicsOnSurfaceVariantDark,
    error = ClinicsErrorDark,
    onError = ClinicsOnErrorDark,
    errorContainer = ClinicsErrorContainerDark,
    onErrorContainer = ClinicsOnErrorContainerDark,
)

@Composable
fun ClinicsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = Typography(),
        content = content,
    )
}
