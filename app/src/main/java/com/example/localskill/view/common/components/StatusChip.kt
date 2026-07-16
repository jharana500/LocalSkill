package com.example.localskill.view.common.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.localskill.view.theme.PillShape
import com.example.localskill.view.theme.Spacing
import com.example.localskill.view.theme.SuccessColor
import com.example.localskill.view.theme.WarningColor

enum class StatusChipTone {
    NEUTRAL,
    SUCCESS,
    WARNING,
    ERROR
}

@Composable
fun StatusChip(
    text: String,
    modifier: Modifier = Modifier,
    tone: StatusChipTone = StatusChipTone.NEUTRAL
) {
    val (background, content) = when (tone) {
        StatusChipTone.NEUTRAL -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
        StatusChipTone.SUCCESS -> SuccessColor.copy(alpha = 0.14f) to SuccessColor
        StatusChipTone.WARNING -> WarningColor.copy(alpha = 0.16f) to WarningColor
        StatusChipTone.ERROR -> MaterialTheme.colorScheme.error.copy(alpha = 0.12f) to MaterialTheme.colorScheme.error
    }

    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = content,
        modifier = modifier
            .background(background, PillShape)
            .padding(horizontal = Spacing.sm, vertical = Spacing.xxs)
    )
}
