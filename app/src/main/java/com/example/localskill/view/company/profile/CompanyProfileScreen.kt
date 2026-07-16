package com.example.localskill.view.company.profile

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.localskill.model.CompanyVerificationStatus
import com.example.localskill.view.common.components.LocalSkillCard
import com.example.localskill.view.common.components.RemoteAvatar
import com.example.localskill.view.common.components.SectionHeader
import com.example.localskill.view.common.components.StatusChip
import com.example.localskill.view.common.components.StatusChipTone
import com.example.localskill.view.common.states.FullScreenLoading
import com.example.localskill.view.theme.Spacing
import com.example.localskill.viewmodel.CompanyProfileViewModel

@Composable
fun CompanyProfileScreen(
    viewModel: CompanyProfileViewModel,
    onEditProfileClick: () -> Unit,
    onVerificationClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val logoPickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) viewModel.uploadLogo(uri)
    }

    LaunchedEffect(Unit) { viewModel.loadProfile() }

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
            Box {
                RemoteAvatar(imageUrl = uiState.company.logoUrl, fallbackText = uiState.company.companyName, size = 72.dp)
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .align(Alignment.BottomEnd)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                        .clickable { logoPickerLauncher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Change company logo",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
            Column(modifier = Modifier.padding(start = Spacing.md)) {
                Text(text = uiState.company.companyName.ifBlank { "Your company" }, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                StatusChip(text = verificationLabel(uiState.company.verificationStatus), tone = verificationTone(uiState.company.verificationStatus))
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = Spacing.md),
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
            StatCard(label = "Posted jobs", value = uiState.postedJobCount.toString(), modifier = Modifier.weight(1f))
            StatCard(label = "Active jobs", value = uiState.activeJobCount.toString(), modifier = Modifier.weight(1f))
        }

        if (uiState.company.description.isNotBlank()) {
            SectionHeader(title = "About", modifier = Modifier.padding(top = Spacing.lg))
            Text(text = uiState.company.description, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = Spacing.xs))
        }

        SectionHeader(title = "Contact", modifier = Modifier.padding(top = Spacing.lg))
        LocalSkillCard(modifier = Modifier.padding(top = Spacing.xs)) {
            Text(text = uiState.company.email, style = MaterialTheme.typography.bodyMedium)
            if (uiState.company.phone.isNotBlank()) {
                Text(text = uiState.company.phone, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = Spacing.xxs))
            }
            if (uiState.company.website.isNotBlank()) {
                Text(text = uiState.company.website, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = Spacing.xxs))
            }
            if (uiState.company.address.isNotBlank()) {
                Text(text = "${uiState.company.address}, ${uiState.company.city}", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = Spacing.xxs))
            }
        }

        ProfileMenuRow(title = "Edit company profile", subtitle = "Business details, address, contact", onClick = onEditProfileClick)
        ProfileMenuRow(title = "Verification", subtitle = verificationLabel(uiState.company.verificationStatus), onClick = onVerificationClick)

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

private fun verificationLabel(status: String): String = when (status) {
    CompanyVerificationStatus.VERIFIED.name -> "Verified"
    CompanyVerificationStatus.PENDING.name -> "Under review"
    CompanyVerificationStatus.REJECTED.name -> "Rejected — action needed"
    else -> "Not submitted"
}

private fun verificationTone(status: String): StatusChipTone = when (status) {
    CompanyVerificationStatus.VERIFIED.name -> StatusChipTone.SUCCESS
    CompanyVerificationStatus.PENDING.name -> StatusChipTone.WARNING
    CompanyVerificationStatus.REJECTED.name -> StatusChipTone.ERROR
    else -> StatusChipTone.NEUTRAL
}
