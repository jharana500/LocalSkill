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
import com.example.localskill.model.UserRole
import com.example.localskill.model.ApplicationStatus
import com.example.localskill.view.components.ActionCard
import com.example.localskill.viewmodel.dashboard.EmployerDashboardUiState
import com.example.localskill.view.components.ApplicationCard
import com.example.localskill.view.components.DashboardStatCard
import com.example.localskill.view.components.EmptyState
import com.example.localskill.view.components.ErrorMessage
import com.example.localskill.view.components.JobCard
import com.example.localskill.view.components.LocalSkillButton
import com.example.localskill.view.components.RoleBadge
import com.example.localskill.view.components.SectionHeader

@Composable
fun EmployerDashboardScreen(
    state: EmployerDashboardUiState,
    onPostJob: () -> Unit,
    onViewPostedJobs: () -> Unit,
    onApplications: () -> Unit,
    onWorkers: () -> Unit,
    onNotifications: () -> Unit,
    onLogout: () -> Unit
) {
    LazyColumn(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            Text("Namaste, ${state.user?.fullName?.ifBlank { "Employer" } ?: "Employer"}", style = MaterialTheme.typography.headlineSmall)
            Text("Post work and manage local applicants.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(10.dp))
            RoleBadge(UserRole.EMPLOYER)
        }
        item { DashboardStatCard("Posted jobs", state.jobs.size.toString()) }
        item { DashboardStatCard("Pending applications", state.applications.count { it.status == ApplicationStatus.PENDING.name }.toString()) }
        item { SectionHeader("Actions") }
        item {
            ActionCard("Notifications", "${state.unreadNotifications} unread updates.", onNotifications)
            Spacer(Modifier.height(8.dp))
            ActionCard("Post Job", "Create a new local work opportunity.", onPostJob)
            Spacer(Modifier.height(8.dp))
            ActionCard("View Posted Jobs", "Review jobs you have created.", onViewPostedJobs)
            Spacer(Modifier.height(8.dp))
            ActionCard("Applications Received", "Review workers who applied to your jobs.", onApplications)
            Spacer(Modifier.height(8.dp))
            ActionCard("Nearby Workers", "Discover skilled workers around your location.", onWorkers)
        }
        item { ErrorMessage(state.errorMessage) }
        item { SectionHeader("Recent Applications", action = "View All", onAction = onApplications) }
        if (state.applications.isEmpty()) {
            item { EmptyState("No applications received yet.\nApplications from workers will appear here after they apply to your jobs.") }
        } else {
            items(state.applications.take(3)) { application ->
                ApplicationCard(application = application, showWorkerDetails = true)
            }
        }
        item { SectionHeader("Recent Posted Jobs") }
        if (state.jobs.isEmpty()) {
            item { EmptyState("Your posted jobs will appear here once you create one.") }
        } else {
            items(state.jobs.take(3)) { job -> JobCard(job) }
        }
        item {
            Spacer(Modifier.height(6.dp))
            LocalSkillButton("Logout", onClick = onLogout)
        }
    }
}
