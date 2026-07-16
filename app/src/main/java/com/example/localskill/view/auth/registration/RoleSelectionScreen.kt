package com.example.localskill.view.auth.registration

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.example.localskill.view.common.components.LocalSkillTextButton
import com.example.localskill.view.common.components.RoleSelectionCard
import com.example.localskill.view.theme.CompanyContainer
import com.example.localskill.view.theme.CompanyPrimary
import com.example.localskill.view.theme.JobSeekerContainer
import com.example.localskill.view.theme.JobSeekerPrimary
import com.example.localskill.view.theme.LocalSkillTheme
import com.example.localskill.view.theme.Spacing

@Composable
fun RoleSelectionScreen(
    onJobSeekerSelected: () -> Unit,
    onCompanySelected: () -> Unit,
    onLoginClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(Spacing.lg)
    ) {
        Text(
            text = "Join LocalSkill",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Choose how you'd like to use LocalSkill",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = Spacing.xxs, bottom = Spacing.lg)
        )

        RoleSelectionCard(
            icon = Icons.Default.Person,
            title = "Job Seeker",
            description = "Looking for your next opportunity?",
            bullets = listOf(
                "Search and apply for jobs",
                "Build a professional profile",
                "Track your applications"
            ),
            onClick = onJobSeekerSelected,
            accentColor = JobSeekerPrimary,
            accentContainerColor = JobSeekerContainer
        )

        RoleSelectionCard(
            icon = Icons.Default.Business,
            title = "Company",
            description = "Hiring for your team?",
            bullets = listOf(
                "Publish job opportunities",
                "Manage applicants",
                "Build a verified company presence"
            ),
            onClick = onCompanySelected,
            accentColor = CompanyPrimary,
            accentContainerColor = CompanyContainer,
            modifier = Modifier.padding(top = Spacing.md)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = Spacing.xl),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Already have an account? ",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            LocalSkillTextButton(text = "Log in", onClick = onLoginClick)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun RoleSelectionScreenPreview() {
    LocalSkillTheme {
        RoleSelectionScreen(onJobSeekerSelected = {}, onCompanySelected = {}, onLoginClick = {})
    }
}
