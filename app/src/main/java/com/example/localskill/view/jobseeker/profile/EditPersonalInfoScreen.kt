package com.example.localskill.view.jobseeker.profile

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.localskill.view.common.components.LocalSkillPrimaryButton
import com.example.localskill.view.common.components.LocalSkillTextField
import com.example.localskill.view.common.components.LocalSkillTopAppBar
import com.example.localskill.view.theme.Spacing
import com.example.localskill.viewmodel.JobSeekerProfileViewModel
import com.example.localskill.viewmodel.ProfileEvent

@Composable
fun EditPersonalInfoScreen(
    viewModel: JobSeekerProfileViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    var headline by remember(uiState.profile.headline) { mutableStateOf(uiState.profile.headline) }
    var bio by remember(uiState.profile.bio) { mutableStateOf(uiState.profile.bio) }
    var city by remember(uiState.profile.city) { mutableStateOf(uiState.profile.city) }
    var district by remember(uiState.profile.district) { mutableStateOf(uiState.profile.district) }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is ProfileEvent.ShowMessage -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    Scaffold(
        topBar = { LocalSkillTopAppBar(title = "Personal Information", onBack = onBack) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(Spacing.lg)
        ) {
            LocalSkillTextField(value = headline, onValueChange = { headline = it }, label = "Professional headline")
            LocalSkillTextField(
                value = bio,
                onValueChange = { bio = it },
                label = "Bio",
                singleLine = false,
                modifier = Modifier.padding(top = Spacing.md)
            )
            LocalSkillTextField(
                value = city,
                onValueChange = { city = it },
                label = "City",
                modifier = Modifier.padding(top = Spacing.md)
            )
            LocalSkillTextField(
                value = district,
                onValueChange = { district = it },
                label = "District",
                modifier = Modifier.padding(top = Spacing.md)
            )

            LocalSkillPrimaryButton(
                text = "Save",
                onClick = { viewModel.updatePersonalInfo(headline, bio, city, district) },
                isLoading = uiState.isMutating,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = Spacing.lg)
            )
        }
    }
}
