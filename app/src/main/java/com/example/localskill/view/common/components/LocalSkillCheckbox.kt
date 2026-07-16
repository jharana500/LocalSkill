package com.example.localskill.view.common.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import com.example.localskill.view.theme.Spacing
import com.example.localskill.view.theme.TouchTarget

@Composable
fun LocalSkillCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    errorMessage: String? = null
) {
    androidx.compose.foundation.layout.Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .defaultMinSize(minHeight = TouchTarget.minimum)
                .clickable(role = Role.Checkbox) { onCheckedChange(!checked) }
                .semantics { role = Role.Checkbox },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(checked = checked, onCheckedChange = onCheckedChange)
            Text(text = label, style = MaterialTheme.typography.bodyMedium)
        }
        if (errorMessage != null) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = Spacing.sm)
            )
        }
    }
}
