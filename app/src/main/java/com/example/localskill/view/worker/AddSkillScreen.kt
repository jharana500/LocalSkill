package com.example.localskill.view.worker

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.localskill.viewmodel.worker.SkillUiState
import com.example.localskill.view.components.ErrorMessage
import com.example.localskill.view.components.LocalSkillButton
import com.example.localskill.view.components.LocalSkillTextField

@Composable
fun AddSkillScreen(state: SkillUiState, onUpdate: (String, String) -> Unit, onSave: () -> Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Add skill", style = MaterialTheme.typography.headlineMedium)
        Text("Build a professional profile employers can trust.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        LocalSkillTextField(state.title, { onUpdate("title", it) }, "Skill title")
        LocalSkillTextField(state.category, { onUpdate("category", it) }, "Category")
        LocalSkillTextField(state.rate, { onUpdate("rate", it) }, "Price or rate")
        LocalSkillTextField(state.experience, { onUpdate("experience", it) }, "Experience")
        LocalSkillTextField(state.location, { onUpdate("location", it) }, "Location")
        LocalSkillTextField(state.availability, { onUpdate("availability", it) }, "Availability")
        LocalSkillTextField(state.description, { onUpdate("description", it) }, "Description", minLines = 4)
        LocalSkillTextField(state.portfolioUrl, { onUpdate("portfolioUrl", it) }, "Portfolio link (optional)")
        ErrorMessage(state.errorMessage)
        state.successMessage?.let { Text(it, color = MaterialTheme.colorScheme.primary) }
        Spacer(Modifier.height(4.dp))
        LocalSkillButton("Save Skill", state.isLoading, onSave)
    }
}
