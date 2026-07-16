package com.example.localskill.view.jobseeker.jobs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.localskill.view.common.components.LocalSkillTextField
import com.example.localskill.view.theme.Spacing

private val REPORT_REASONS = listOf(
    "Spam or misleading",
    "Discriminatory content",
    "Scam or fraudulent",
    "Already filled or expired",
    "Other"
)

@Composable
fun ReportJobDialog(
    onDismiss: () -> Unit,
    onSubmit: (reason: String, description: String) -> Unit
) {
    var selectedReason by remember { mutableStateOf(REPORT_REASONS.first()) }
    var description by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Report this job") },
        text = {
            Column {
                REPORT_REASONS.forEach { reason ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedReason == reason,
                            onClick = { selectedReason = reason }
                        )
                        Text(text = reason, style = MaterialTheme.typography.bodyMedium)
                    }
                }
                LocalSkillTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = "Additional details (optional)",
                    singleLine = false,
                    modifier = Modifier.padding(top = Spacing.sm)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onSubmit(selectedReason, description) }) {
                Text("Submit report")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
