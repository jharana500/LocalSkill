package com.example.localskill.view.admin.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.localskill.view.common.components.LocalSkillCard
import com.example.localskill.view.common.components.LocalSkillDestructiveButton
import com.example.localskill.view.common.components.LocalSkillTopAppBar
import com.example.localskill.view.common.components.SectionHeader
import com.example.localskill.view.common.dialogs.ConfirmationDialog
import com.example.localskill.view.theme.AppTheme
import com.example.localskill.view.theme.Spacing
import com.example.localskill.viewmodel.AdminSettingsViewModel

@Composable
fun AdminSettingsScreen(
    viewModel: AdminSettingsViewModel,
    onBack: () -> Unit,
    onNotificationsClick: () -> Unit,
    onLogout: () -> Unit
) {
    val theme by viewModel.appTheme.collectAsStateWithLifecycle()
    var showLogoutConfirmation by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { LocalSkillTopAppBar(title = "Settings", onBack = onBack) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(Spacing.lg)
        ) {
            SectionHeader(title = "Appearance")
            LocalSkillCard(modifier = Modifier.padding(top = Spacing.xs)) {
                AppTheme.entries.forEach { option ->
                    ThemeOptionRow(
                        label = themeLabel(option),
                        selected = theme == option,
                        onSelect = { viewModel.setAppTheme(option) }
                    )
                }
            }

            SectionHeader(title = "Platform", modifier = Modifier.padding(top = Spacing.lg))
            LocalSkillCard(modifier = Modifier.padding(top = Spacing.xs)) {
                Text(
                    text = "Notifications",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onNotificationsClick)
                        .padding(vertical = Spacing.xs)
                )
            }

            LocalSkillDestructiveButton(
                text = "Log out",
                onClick = { showLogoutConfirmation = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = Spacing.xl)
            )
        }
    }

    if (showLogoutConfirmation) {
        ConfirmationDialog(
            title = "Log out?",
            message = "You'll need to log in again to access the admin console.",
            confirmLabel = "Log out",
            onConfirm = {
                showLogoutConfirmation = false
                onLogout()
            },
            onDismiss = { showLogoutConfirmation = false }
        )
    }
}

@Composable
private fun ThemeOptionRow(label: String, selected: Boolean, onSelect: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(selected = selected, onClick = onSelect),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = onSelect)
        Text(text = label, modifier = Modifier.padding(start = Spacing.xs))
    }
}

private fun themeLabel(theme: AppTheme): String = when (theme) {
    AppTheme.LIGHT -> "Light"
    AppTheme.DARK -> "Dark"
    AppTheme.SYSTEM -> "System default"
}
