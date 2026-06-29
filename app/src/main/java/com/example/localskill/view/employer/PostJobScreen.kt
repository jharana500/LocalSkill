package com.example.localskill.view.employer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.localskill.viewmodel.employer.PostJobUiState
import com.example.localskill.view.components.ErrorMessage
import com.example.localskill.view.components.LocalSkillButton
import com.example.localskill.view.components.LocalSkillTextField

@Composable
fun PostJobScreen(state: PostJobUiState, onUpdate: (String, String) -> Unit, onPost: () -> Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Post a Job", style = MaterialTheme.typography.headlineMedium)
        Text("Find skilled local workers near you.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                LocalSkillTextField(state.title, { onUpdate("title", it) }, "Job title")
                LocalSkillTextField(state.description, { onUpdate("description", it) }, "Job description", minLines = 4)
                LocalSkillTextField(state.requiredSkills, { onUpdate("requiredSkills", it) }, "Required skills")
                LocalSkillTextField(state.budget, { onUpdate("budget", it) }, "Budget or salary")
                LocalSkillTextField(state.city, { onUpdate("city", it) }, "City")
                LocalSkillTextField(state.area, { onUpdate("area", it) }, "Area")
                LocalSkillTextField(state.location, { onUpdate("location", it) }, "Address or landmark")
                LocalSkillTextField(state.jobType, { onUpdate("jobType", it) }, "Job type")
                LocalSkillTextField(state.deadline, { onUpdate("deadline", it) }, "Deadline optional")
            }
        }
        ErrorMessage(state.errorMessage)
        state.successMessage?.let { Text(it, color = MaterialTheme.colorScheme.primary) }
        Spacer(Modifier.height(4.dp))
        LocalSkillButton("Post Job", state.isLoading, onPost)
    }
}
