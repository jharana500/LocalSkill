package com.example.localskill.view.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.localskill.model.ApplicationModel
import com.example.localskill.model.ApplicationStatus
import com.example.localskill.model.JobModel
import com.example.localskill.model.NotificationModel
import com.example.localskill.model.ReviewModel
import com.example.localskill.model.SkillModel
import com.example.localskill.model.UserRole
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun LocalSkillButton(
    text: String,
    isLoading: Boolean = false,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        enabled = enabled && !isLoading,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
    ) {
        if (isLoading) CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary)
        else Text(text)
    }
}

@Composable
fun LocalSkillTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    minLines: Int = 1,
    keyboardType: KeyboardType = KeyboardType.Text,
    enabled: Boolean = true
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        enabled = enabled,
        minLines = minLines,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp)
    )
}

@Composable
fun LocalSkillPasswordField(value: String, onValueChange: (String) -> Unit, label: String) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        visualTransformation = PasswordVisualTransformation(),
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp)
    )
}

@Composable
fun SectionHeader(title: String, action: String? = null, onAction: (() -> Unit)? = null) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        if (action != null && onAction != null) {
            Text(action, color = MaterialTheme.colorScheme.primary, modifier = Modifier.clickable(onClick = onAction))
        }
    }
}

@Composable
fun ErrorMessage(message: String?) {
    if (message != null) {
        Text(message, color = MaterialTheme.colorScheme.error, modifier = Modifier.fillMaxWidth())
    }
}

@Composable
fun EmptyState(text: String) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Text(text, modifier = Modifier.padding(18.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun LoadingState() {
    Row(Modifier.fillMaxWidth().padding(24.dp), horizontalArrangement = Arrangement.Center) {
        CircularProgressIndicator()
    }
}

@Composable
fun RoleCard(role: UserRole, selected: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(Modifier.padding(18.dp)) {
            Text(if (role == UserRole.WORKER) "Worker" else "Employer", style = MaterialTheme.typography.titleMedium)
            Text(
                if (role == UserRole.WORKER) "Find nearby jobs and showcase your skills."
                else "Post local work and hire trusted nearby talent.",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun RoleBadge(role: UserRole) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = if (role == UserRole.WORKER) "Worker" else "Employer",
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelLarge
        )
    }
}

@Composable
fun ActionCard(title: String, subtitle: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun ProfileCompletionCard(completedFields: Int, totalFields: Int, onClick: () -> Unit) {
    ActionCard(
        title = "Profile completion",
        subtitle = "$completedFields of $totalFields basics completed",
        onClick = onClick
    )
}

@Composable
fun DashboardStatCard(title: String, value: String) {
    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.padding(16.dp)) {
            Text(value, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
            Text(title, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun SkillCard(skill: SkillModel) {
    Card(
        Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(skill.title, style = MaterialTheme.typography.titleMedium)
            Text("${skill.category} - ${skill.rate}", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("${skill.experience} - ${skill.location}", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(skill.availability, color = MaterialTheme.colorScheme.primary)
            if (skill.description.isNotBlank()) {
                Text(skill.description.take(120), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (skill.portfolioUrl.isNotBlank()) {
                Text("Portfolio available", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Composable
fun JobCard(job: JobModel, distanceText: String? = null, onClick: (() -> Unit)? = null) {
    Card(
        Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(job.title, style = MaterialTheme.typography.titleMedium)
            Text(job.description.take(120), color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Skills: ${job.requiredSkills}", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("${job.budget} - ${job.displayLocation()}", color = MaterialTheme.colorScheme.onSurfaceVariant)
            distanceText?.let { Text(it, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge) }
            Text("${job.jobType} - ${job.status}", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge)
            if (job.deadline.isNotBlank()) {
                Text("Deadline: ${job.deadline}", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (job.createdAt > 0L) {
                Text("Posted ${job.createdAt.formatDate()}", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

private fun JobModel.displayLocation(): String =
    listOf(area, city, location).filter { it.isNotBlank() }.joinToString(", ").ifBlank { "Location not available" }

private fun Long.formatDate(): String =
    SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(this))

@Composable
fun ApplicationCard(
    application: ApplicationModel,
    showWorkerDetails: Boolean = false,
    isUpdating: Boolean = false,
    hasReviewed: Boolean = false,
    onAccept: (() -> Unit)? = null,
    onReject: (() -> Unit)? = null,
    onReview: (() -> Unit)? = null
) {
    Card(
        Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(application.jobTitle.ifBlank { "Job application" }, style = MaterialTheme.typography.titleMedium)
            if (showWorkerDetails) {
                Text(application.workerName.ifBlank { "Worker" }, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (application.workerLocation.isNotBlank()) {
                    Text(application.workerLocation, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (application.workerPhone.isNotBlank()) {
                    Text(application.workerPhone, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Text("${application.jobBudget} - ${application.jobLocation}", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                application.status,
                color = when (application.status) {
                    ApplicationStatus.ACCEPTED.name -> MaterialTheme.colorScheme.primary
                    ApplicationStatus.REJECTED.name -> MaterialTheme.colorScheme.error
                    else -> MaterialTheme.colorScheme.tertiary
                },
                style = MaterialTheme.typography.labelLarge
            )
            if (application.message.isNotBlank()) {
                Text(application.message.take(120), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (application.appliedAt > 0L) {
                Text("Applied ${application.appliedAt.formatDate()}", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelMedium)
            }
            if (application.status == ApplicationStatus.PENDING.name && onAccept != null && onReject != null) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = onAccept,
                        enabled = !isUpdating,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Accept")
                    }
                    Button(
                        onClick = onReject,
                        enabled = !isUpdating,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Reject")
                    }
                }
            } else if (application.status == ApplicationStatus.ACCEPTED.name && onReview != null) {
                LocalSkillButton(
                    text = if (hasReviewed) "Reviewed" else "Review",
                    onClick = onReview,
                    enabled = !hasReviewed
                )
            }
        }
    }
}

@Composable
fun RatingBar(
    rating: Int,
    onRatingSelected: ((Int) -> Unit)? = null,
    readOnly: Boolean = false
) {
    Row(horizontalArrangement = Arrangement.spacedBy(2.dp), verticalAlignment = Alignment.CenterVertically) {
        (1..5).forEach { star ->
            Text(
                text = if (star <= rating) "★" else "☆",
                color = if (star <= rating) Color(0xFFF59E0B) else MaterialTheme.colorScheme.outline,
                style = MaterialTheme.typography.titleLarge,
                modifier = if (!readOnly && onRatingSelected != null) Modifier.clickable { onRatingSelected(star) } else Modifier
            )
        }
    }
}

@Composable
fun ReviewCard(review: ReviewModel) {
    Card(
        Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(review.reviewerName.ifBlank { "Reviewer" }, style = MaterialTheme.typography.titleMedium)
            RatingBar(rating = review.rating, readOnly = true)
            if (review.jobTitle.isNotBlank()) {
                Text(review.jobTitle, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(review.comment, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (review.createdAt > 0L) {
                Text("Reviewed ${review.createdAt.formatDate()}", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

@Composable
fun WorkerCard(
    worker: com.example.localskill.model.UserModel,
    distanceText: String? = null,
    onViewProfile: (() -> Unit)? = null
) {
    Card(
        Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(worker.fullName.ifBlank { "Worker" }, style = MaterialTheme.typography.titleMedium)
            Text(worker.experience.ifBlank { worker.bio.ifBlank { "Skill profile available" } }, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(worker.displayLocation(), color = MaterialTheme.colorScheme.onSurfaceVariant)
            distanceText?.let { Text(it, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge) }
            Text(
                "${"%.1f".format(worker.averageRating)} rating · ${worker.totalReviews} reviews",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            onViewProfile?.let { LocalSkillButton("View Profile", onClick = it) }
        }
    }
}

@Composable
fun NotificationCard(
    notification: NotificationModel,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (notification.isRead) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.primaryContainer
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(notification.title, style = MaterialTheme.typography.titleMedium)
                Text(notification.type, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelMedium)
            }
            Text(notification.message, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (notification.senderName.isNotBlank()) {
                Text(notification.senderName, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelMedium)
            }
            if (notification.createdAt > 0L) {
                Text(notification.createdAt.formatDate(), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelMedium)
            }
            LocalSkillButton("Delete", onClick = onDelete)
        }
    }
}

private fun com.example.localskill.model.UserModel.displayLocation(): String =
    listOf(area, city).filter { it.isNotBlank() }.joinToString(", ").ifBlank { "Location not available" }
