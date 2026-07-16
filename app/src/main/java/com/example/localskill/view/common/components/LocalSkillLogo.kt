package com.example.localskill.view.common.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WorkOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.localskill.view.theme.IconSize

@Composable
fun LocalSkillLogo(
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = IconSize.logo
) {
    Box(
        modifier = modifier
            .size(size)
            .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(size / 4)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.WorkOutline,
            contentDescription = "LocalSkill",
            tint = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.size(size / 2)
        )
    }
}
