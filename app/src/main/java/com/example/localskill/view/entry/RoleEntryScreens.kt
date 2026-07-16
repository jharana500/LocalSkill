package com.example.localskill.view.entry

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.example.localskill.view.common.components.LocalSkillTextButton
import com.example.localskill.view.common.components.StatusChip
import com.example.localskill.view.theme.AppTheme
import com.example.localskill.view.theme.LocalSkillTheme
import com.example.localskill.view.theme.Spacing
import com.example.localskill.model.UserRole

/**
 * Temporary phase boundaries: the real Company and Admin experiences are
 * built in later phases. Job Seeker now has its own full nested graph
 * (see JobSeekerNavGraph.kt) — these screens exist so role-aware routing
 * for the remaining roles has somewhere real to land in the meantime.
 */
@Composable
fun CompanyEntryScreen(onLogout: () -> Unit) {
    RoleEntryContent(
        roleLabel = "Company",
        message = "Your recruitment dashboard is coming in the next phase.",
        onLogout = onLogout
    )
}

@Composable
fun AdminEntryScreen(onLogout: () -> Unit) {
    RoleEntryContent(
        roleLabel = "Administrator",
        message = "The admin console is coming in a later phase.",
        onLogout = onLogout
    )
}

@Composable
private fun RoleEntryContent(
    roleLabel: String,
    message: String,
    onLogout: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(Spacing.lg),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        StatusChip(text = "Phase boundary")

        Text(
            text = "Welcome, $roleLabel",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = Spacing.md)
        )

        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = Spacing.xs)
        )

        LocalSkillTextButton(
            text = "Log out",
            onClick = onLogout,
            modifier = Modifier.padding(top = Spacing.xl)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CompanyEntryScreenPreview() {
    LocalSkillTheme(appTheme = AppTheme.SYSTEM, activeRole = UserRole.COMPANY) {
        CompanyEntryScreen(onLogout = {})
    }
}
