package com.example.localskill.view.jobseeker.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.localskill.model.SkillModel
import com.example.localskill.utils.Constants
import com.example.localskill.view.common.components.LocalSkillTextField
import com.example.localskill.view.common.components.LocalSkillTopAppBar
import com.example.localskill.view.common.states.EmptyState
import com.example.localskill.view.theme.Spacing
import com.example.localskill.viewmodel.JobSeekerProfileViewModel
import com.example.localskill.viewmodel.ProfileEvent

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ManageSkillsScreen(
    viewModel: JobSeekerProfileViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var skillInput by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is ProfileEvent.ShowMessage -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    val canAddMore = uiState.profile.skills.size < Constants.MAX_SKILLS_COUNT

    fun submitSkill() {
        val trimmed = skillInput.trim()
        if (trimmed.isEmpty() || !canAddMore) return
        viewModel.addSkill(SkillModel(name = trimmed))
        skillInput = ""
    }

    Scaffold(
        topBar = { LocalSkillTopAppBar(title = "Skills", onBack = onBack) },
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
            LocalSkillTextField(
                value = skillInput,
                onValueChange = { skillInput = it },
                label = if (canAddMore) "Add a skill" else "Skill limit reached (${Constants.MAX_SKILLS_COUNT})",
                enabled = canAddMore,
                imeAction = ImeAction.Done,
                onImeAction = ::submitSkill
            )

            Text(
                text = "Press enter to add. Tap a skill below to remove it. Duplicates like " +
                    "\"Kotlin\" and \"kotlin\" are merged automatically.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = Spacing.xxs)
            )

            if (uiState.profile.skills.isEmpty()) {
                EmptyState(
                    title = "No skills added yet",
                    description = "Add skills so employers can find you for the right jobs.",
                    modifier = Modifier.padding(top = Spacing.xl)
                )
            } else {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = Spacing.lg)
                ) {
                    uiState.profile.skills.forEach { skill ->
                        InputChip(
                            selected = false,
                            onClick = { viewModel.removeSkill(skill.name) },
                            label = { Text(skill.name) },
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Remove ${skill.name}"
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}
