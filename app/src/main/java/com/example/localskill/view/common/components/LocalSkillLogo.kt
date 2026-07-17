package com.example.localskill.view.common.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.WorkOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.localskill.view.theme.IconSize

/**
 * A location pin (Local) with a briefcase badge (Skill) marking it — the two
 * halves of the app's name as one mark, instead of an unrelated generic icon.
 */
@Composable
fun LocalSkillLogo(
    modifier: Modifier = Modifier,
    size: Dp = IconSize.logo
) {
    Box(
        modifier = modifier
            .size(size)
            .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(size / 4)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.LocationOn,
            contentDescription = "LocalSkill",
            tint = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.size(size * 0.6f)
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = -size * 0.06f, y = -size * 0.06f)
                .size(size * 0.4f)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.onPrimary),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.WorkOutline,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(size * 0.22f)
            )
        }
    }
}
