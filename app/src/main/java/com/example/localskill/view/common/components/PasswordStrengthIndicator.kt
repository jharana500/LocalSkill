package com.example.localskill.view.common.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.localskill.utils.PasswordStrength
import com.example.localskill.view.theme.ErrorColor
import com.example.localskill.view.theme.SuccessColor
import com.example.localskill.view.theme.WarningColor

@Composable
fun PasswordStrengthIndicator(
    strength: PasswordStrength,
    modifier: Modifier = Modifier
) {
    if (strength == PasswordStrength.NONE) return

    val (activeSegments, color, label) = when (strength) {
        PasswordStrength.NONE -> Triple(0, MaterialTheme.colorScheme.outlineVariant, "")
        PasswordStrength.WEAK -> Triple(1, ErrorColor, "Weak")
        PasswordStrength.MEDIUM -> Triple(2, WarningColor, "Medium")
        PasswordStrength.STRONG -> Triple(3, SuccessColor, "Strong")
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth()) {
            repeat(3) { index ->
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .height(4.dp)
                        .background(
                            color = if (index < activeSegments) color else MaterialTheme.colorScheme.outlineVariant,
                            shape = RoundedCornerShape(2.dp)
                        )
                ) {}
                if (index != 2) {
                    Spacer(modifier = Modifier.width(4.dp))
                }
            }
        }
        Text(
            text = "Password strength: $label",
            style = MaterialTheme.typography.labelSmall,
            color = color,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}
