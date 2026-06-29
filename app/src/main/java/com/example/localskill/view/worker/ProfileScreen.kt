package com.example.localskill.view.worker

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.localskill.model.UserRole
import com.example.localskill.view.components.ErrorMessage
import com.example.localskill.view.components.EmptyState
import com.example.localskill.view.components.LocalSkillButton
import com.example.localskill.view.components.LocalSkillTextField
import com.example.localskill.view.components.RatingBar
import com.example.localskill.view.components.ReviewCard
import com.example.localskill.view.components.RoleBadge
import com.example.localskill.view.components.SectionHeader
import com.example.localskill.view.components.SkillCard
import com.example.localskill.viewmodel.worker.ProfileUiState

@Composable
fun ProfileScreen(
    state: ProfileUiState,
    onUpdate: (String, String) -> Unit,
    onSave: () -> Unit,
    onAddSkill: () -> Unit,
    onLogout: () -> Unit
) {
    LazyColumn(
        Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Worker Profile", style = MaterialTheme.typography.headlineMedium)
            Text("Keep your details clear so nearby employers can trust your work.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(8.dp))
            RoleBadge(state.user?.role ?: UserRole.WORKER)
            Spacer(Modifier.height(8.dp))
            RatingBar(rating = state.user?.averageRating?.toInt() ?: 0, readOnly = true)
            Text(
                "${"%.1f".format(state.user?.averageRating ?: 0.0)} average rating · ${state.user?.totalReviews ?: 0} reviews",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        item {
            LocalSkillTextField(state.fullName, { onUpdate("fullName", it) }, "Full name")
            Spacer(Modifier.height(10.dp))
            LocalSkillTextField(state.email, {}, "Email", enabled = false)
            Spacer(Modifier.height(10.dp))
            LocalSkillTextField(state.phone, { onUpdate("phone", it) }, "Phone")
            Spacer(Modifier.height(10.dp))
            LocalSkillTextField(state.city, { onUpdate("city", it) }, "City")
            Spacer(Modifier.height(10.dp))
            LocalSkillTextField(state.area, { onUpdate("area", it) }, "Area")
            Spacer(Modifier.height(10.dp))
            LocalSkillTextField(state.location, { onUpdate("location", it) }, "Address or landmark")
            Spacer(Modifier.height(10.dp))
            LocalSkillTextField(state.bio, { onUpdate("bio", it) }, "Short bio", minLines = 3)
            Spacer(Modifier.height(10.dp))
            LocalSkillTextField(state.experience, { onUpdate("experience", it) }, "Experience summary")
            Spacer(Modifier.height(10.dp))
            LocalSkillTextField(state.availability, { onUpdate("availability", it) }, "Availability status")
        }
        item {
            ErrorMessage(state.errorMessage)
            state.successMessage?.let { Text(it, color = MaterialTheme.colorScheme.primary) }
        }
        item {
            LocalSkillButton("Save Profile", state.isSaving, onSave)
            Spacer(Modifier.height(8.dp))
            LocalSkillButton("Add Skill", onClick = onAddSkill)
        }
        item { SectionHeader("Skills") }
        if (state.skills.isEmpty()) {
            item { EmptyState("Your skills will appear here once you add them.") }
        } else {
            items(state.skills) { skill -> SkillCard(skill) }
        }
        item { SectionHeader("Reviews") }
        if (state.reviews.isEmpty()) {
            item { EmptyState("No reviews yet.\nCompleted work reviews will appear here.") }
        } else {
            items(state.reviews) { review -> ReviewCard(review) }
        }
        item {
            Spacer(Modifier.height(8.dp))
            LocalSkillButton("Logout", onClick = onLogout)
        }
    }
}
