package com.example.localskill.view.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.example.localskill.model.UserRole

enum class AppTheme {
    LIGHT,
    DARK,
    SYSTEM
}

@Composable
fun LocalSkillTheme(
    appTheme: AppTheme = AppTheme.SYSTEM,
    activeRole: UserRole = UserRole.JOB_SEEKER,
    // Dynamic color is intentionally never used: LocalSkill's role-based brand
    // identity must not be silently replaced by the device wallpaper palette.
    content: @Composable () -> Unit
) {
    val darkTheme = when (appTheme) {
        AppTheme.LIGHT -> false
        AppTheme.DARK -> true
        AppTheme.SYSTEM -> isSystemInDarkTheme()
    }

    val colorScheme = if (darkTheme) darkColorSchemeFor(activeRole) else lightColorSchemeFor(activeRole)

    MaterialTheme(
        colorScheme = colorScheme,
        typography = LocalSkillTypography,
        shapes = LocalSkillShapes,
        content = content
    )
}
