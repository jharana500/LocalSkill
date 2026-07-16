package com.example.localskill.view.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import com.example.localskill.model.UserRole

private data class RolePalette(
    val primary: Color,
    val primaryDark: Color,
    val container: Color
)

private fun paletteFor(role: UserRole): RolePalette = when (role) {
    UserRole.JOB_SEEKER -> RolePalette(JobSeekerPrimary, JobSeekerPrimaryDark, JobSeekerContainer)
    UserRole.COMPANY -> RolePalette(CompanyPrimary, CompanyPrimaryDark, CompanyContainer)
    UserRole.ADMIN -> RolePalette(AdminPrimary, AdminPrimaryDark, AdminContainer)
}

fun lightColorSchemeFor(role: UserRole): ColorScheme {
    val palette = paletteFor(role)
    return lightColorScheme(
        primary = palette.primary,
        onPrimary = Color.White,
        primaryContainer = palette.container,
        onPrimaryContainer = palette.primaryDark,
        secondary = palette.primaryDark,
        onSecondary = Color.White,
        background = BackgroundNeutral,
        onBackground = TextPrimary,
        surface = SurfaceNeutral,
        onSurface = TextPrimary,
        surfaceVariant = DisabledSurface,
        onSurfaceVariant = TextSecondary,
        outline = BorderNeutral,
        outlineVariant = BorderNeutral,
        error = ErrorColor,
        onError = Color.White
    )
}

fun darkColorSchemeFor(role: UserRole): ColorScheme {
    val palette = paletteFor(role)
    return darkColorScheme(
        primary = palette.primary,
        onPrimary = Color.White,
        primaryContainer = palette.primaryDark,
        onPrimaryContainer = palette.container,
        secondary = palette.container,
        onSecondary = TextPrimaryDark,
        background = BackgroundNeutralDark,
        onBackground = TextPrimaryDark,
        surface = SurfaceNeutralDark,
        onSurface = TextPrimaryDark,
        surfaceVariant = DisabledSurfaceDark,
        onSurfaceVariant = TextSecondaryDark,
        outline = BorderNeutralDark,
        outlineVariant = BorderNeutralDark,
        error = ErrorColor,
        onError = Color.White
    )
}
