package com.example.localskill.view.jobseeker.settings

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
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.localskill.model.ProfileVisibility
import com.example.localskill.view.common.components.LocalSkillCard
import com.example.localskill.view.common.components.LocalSkillDestructiveButton
import com.example.localskill.view.common.components.LocalSkillTopAppBar
import com.example.localskill.view.common.components.SectionHeader
import com.example.localskill.view.common.dialogs.ConfirmationDialog
import com.example.localskill.view.theme.AppTheme
import com.example.localskill.view.theme.Spacing
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.localskill.viewmodel.JobSeekerSettingsViewModel

@Composable
fun JobSeekerSettingsScreen(
    viewModel: JobSeekerSettingsViewModel,
    onBack: () -> Unit,
    onAccountInfoClick: () -> Unit,
    onLogout: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
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
                AppTheme.entries.forEach { theme ->
                    ThemeOptionRow(
                        label = themeLabel(theme),
                        selected = uiState.appTheme == theme,
                        onSelect = { viewModel.setAppTheme(theme) }
                    )
                }
            }

            SectionHeader(title = "Privacy", modifier = Modifier.padding(top = Spacing.lg))
            LocalSkillCard(modifier = Modifier.padding(top = Spacing.xs)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Public profile", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            text = "Allow companies to discover your profile",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = uiState.profileVisibility == ProfileVisibility.PUBLIC.name,
                        onCheckedChange = { checked ->
                            viewModel.setProfileVisibility(
                                if (checked) ProfileVisibility.PUBLIC.name else ProfileVisibility.PRIVATE.name
                            )
                        }
                    )
                }
            }

            SectionHeader(title = "Account", modifier = Modifier.padding(top = Spacing.lg))
            LocalSkillCard(
                modifier = Modifier
                    .padding(top = Spacing.xs)
                    .fillMaxWidth()
            ) {
                Text(
                    text = "Personal information",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onAccountInfoClick)
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
            message = "You'll need to log in again to access your account.",
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
