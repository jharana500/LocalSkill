package com.example.localskill.view.dashboard

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
import com.example.localskill.model.UserModel
import com.example.localskill.model.UserRole
import com.example.localskill.view.components.ActionCard
import com.example.localskill.viewmodel.dashboard.WorkerDashboardUiState
import com.example.localskill.view.components.ApplicationCard
import com.example.localskill.view.components.DashboardStatCard
import com.example.localskill.view.components.EmptyState
import com.example.localskill.view.components.ErrorMessage
import com.example.localskill.view.components.JobCard
import com.example.localskill.view.components.LocalSkillButton
import com.example.localskill.view.components.ProfileCompletionCard
import com.example.localskill.view.components.RoleBadge
import com.example.localskill.view.components.SectionHeader
import com.example.localskill.view.components.SkillCard

@Composable
fun WorkerDashboardScreen(
    state: WorkerDashboardUiState,
    onAddSkill: () -> Unit,
    onViewJobs: () -> Unit,
    onApplications: () -> Unit,
    onNotifications: () -> Unit,
    onJobClick: (String) -> Unit,
    onProfile: () -> Unit,
    onLogout: () -> Unit
) {
    LazyColumn(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            Text("Namaste, ${state.user?.fullName?.ifBlank { "Worker" } ?: "Worker"}", style = MaterialTheme.typography.headlineSmall)
            Text("Find nearby jobs that match your skills.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(10.dp))
            RoleBadge(UserRole.WORKER)
        }
        item { ProfileCompletionCard(completedFields = state.user.completedBasics(), totalFields = 7, onClick = onProfile) }
        item { DashboardStatCard("Applications", state.applications.size.toString()) }
        item { SectionHeader("Actions") }
        item {
            ActionCard("Notifications", "${state.unreadNotifications} unread updates.", onNotifications)
            Spacer(Modifier.height(8.dp))
            ActionCard("Add Skill", "Show employers what you can do.", onAddSkill)
            Spacer(Modifier.height(8.dp))
            ActionCard("View Profile", "Update your profile and worker details.", onProfile)
            Spacer(Modifier.height(8.dp))
            ActionCard("View Jobs", "Browse local opportunities when they are available.", onViewJobs)
            Spacer(Modifier.height(8.dp))
            ActionCard("My Applications", "Track jobs you have applied for.", onApplications)
        }
        item { ErrorMessage(state.errorMessage) }
        item { SectionHeader("Recent Applications", action = "View All", onAction = onApplications) }
        if (state.applications.isEmpty()) {
            item { EmptyState("You have not applied to any jobs yet.\nJobs you apply for will appear here.") }
        } else {
            items(state.applications.take(3)) { application -> ApplicationCard(application) }
        }
        item { SectionHeader("Recent Open Jobs", action = "Browse", onAction = onViewJobs) }
        if (state.jobs.isEmpty()) {
            item { EmptyState("No open jobs found yet.\nNew local opportunities will appear here once employers post jobs.") }
        } else {
            items(state.jobs.take(3)) { job -> JobCard(job) { onJobClick(job.id) } }
        }
        item { SectionHeader("My Skills") }
        if (state.skills.isEmpty()) {
            item { EmptyState("Your skills will appear here once you add them.") }
        } else {
            items(state.skills) { skill -> SkillCard(skill) }
        }
        item {
            Spacer(Modifier.height(6.dp))
            LocalSkillButton("Logout", onClick = onLogout)
        }
    }
}

private fun UserModel?.completedBasics(): Int {
    if (this == null) return 0
    return listOf(fullName, email, phone, location, bio, experience, availability).count { it.isNotBlank() }
}
