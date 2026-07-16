package com.example.localskill.view.jobseeker.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.localskill.view.common.components.LocalSkillCard
import com.example.localskill.view.common.components.RemoteAvatar
import com.example.localskill.view.common.components.SectionHeader
import com.example.localskill.view.common.components.StatusChip
import com.example.localskill.view.common.states.FullScreenLoading
import com.example.localskill.view.theme.Spacing
import com.example.localskill.viewmodel.JobSeekerProfileViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun JobSeekerProfileScreen(
    viewModel: JobSeekerProfileViewModel,
    onEditPersonalInfoClick: () -> Unit,
    onEducationClick: () -> Unit,
    onExperienceClick: () -> Unit,
    onSkillsClick: () -> Unit,
    onResumeClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.loadProfile()
    }

    if (uiState.isLoading) {
        FullScreenLoading(modifier = modifier.fillMaxSize())
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(Spacing.lg)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            RemoteAvatar(imageUrl = uiState.profile.profileImageUrl, fallbackText = uiState.fullName, size = 72.dp)
            Column(modifier = Modifier.padding(start = Spacing.md)) {
                Text(text = uiState.fullName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                if (uiState.profile.headline.isNotBlank()) {
                    Text(text = uiState.profile.headline, style = MaterialTheme.typography.bodyMedium)
                }
                if (uiState.profile.city.isNotBlank()) {
                    Text(
                        text = uiState.profile.city,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        LocalSkillCard(modifier = Modifier.padding(top = Spacing.lg)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Profile completion", style = MaterialTheme.typography.titleSmall)
                Text(
                    text = "${uiState.profile.completionPercentage}%",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(top = Spacing.sm),
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
            StatCard(label = "Applications", value = uiState.applicationCount.toString(), modifier = Modifier.weight(1f))
            StatCard(label = "Saved jobs", value = uiState.savedJobCount.toString(), modifier = Modifier.weight(1f))
        }

        SectionHeader(title = "Contact", modifier = Modifier.padding(top = Spacing.lg))
        LocalSkillCard(modifier = Modifier.padding(top = Spacing.xs)) {
            Text(text = uiState.email, style = MaterialTheme.typography.bodyMedium)
            if (uiState.phone.isNotBlank()) {
                Text(
                    text = uiState.phone,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = Spacing.xxs)
                )
            }
        }

        if (uiState.profile.bio.isNotBlank()) {
            SectionHeader(title = "About", modifier = Modifier.padding(top = Spacing.lg))
            Text(
                text = uiState.profile.bio,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = Spacing.xs)
            )
        }

        if (uiState.profile.skills.isNotEmpty()) {
            SectionHeader(title = "Skills", modifier = Modifier.padding(top = Spacing.lg))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(Spacing.xxs),
                modifier = Modifier.padding(top = Spacing.xs)
            ) {
                uiState.profile.skills.forEach { skill -> StatusChip(text = skill.name) }
            }
        }

        ProfileMenuRow(title = "Personal information", subtitle = "Headline, bio, location", onClick = onEditPersonalInfoClick)
        ProfileMenuRow(
            title = "Education",
            subtitle = "${uiState.profile.education.size} entries",
            onClick = onEducationClick
        )
        ProfileMenuRow(
            title = "Experience",
            subtitle = "${uiState.profile.experience.size} entries",
            onClick = onExperienceClick
        )
        ProfileMenuRow(title = "Skills", subtitle = "${uiState.profile.skills.size} skills", onClick = onSkillsClick)
        ProfileMenuRow(
            title = "Resume",
            subtitle = if (uiState.profile.resume.isPresent) uiState.profile.resume.fileName else "Not added",
            onClick = onResumeClick
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = Spacing.md)
                .clickable(onClick = onSettingsClick),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = Icons.Default.Settings, contentDescription = null)
            Text(text = "Settings", modifier = Modifier.weight(1f).padding(start = Spacing.sm))
            Icon(imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    LocalSkillCard(modifier = modifier) {
        Text(text = value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun ProfileMenuRow(title: String, subtitle: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = Spacing.sm)
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.titleSmall)
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
    }
}
