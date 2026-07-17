package com.example.localskill.view.jobseeker.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.Balance
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Construction
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.localskill.model.JobCategoryModel
import com.example.localskill.view.common.components.LocalSkillTextButton
import com.example.localskill.view.theme.Spacing

/** Categories are free-text, admin-created names, not a fixed enum — match on
 * common keywords so related categories get a relevant icon, falling back to
 * a generic briefcase for anything unrecognized. */
private fun iconForCategory(name: String): ImageVector {
    val normalized = name.lowercase()
    return when {
        "engineer" in normalized || "develop" in normalized || "software" in normalized || "it " in normalized -> Icons.Default.Code
        "market" in normalized || "advertis" in normalized -> Icons.Default.Campaign
        "sales" in normalized || "business dev" in normalized -> Icons.AutoMirrored.Filled.TrendingUp
        "design" in normalized || "creative" in normalized -> Icons.Default.Brush
        "financ" in normalized || "account" in normalized || "bank" in normalized -> Icons.Default.Savings
        "health" in normalized || "medic" in normalized || "nurs" in normalized || "hospital" in normalized -> Icons.Default.LocalHospital
        "educat" in normalized || "teach" in normalized || "tutor" in normalized -> Icons.AutoMirrored.Filled.MenuBook
        "customer" in normalized || "support" in normalized -> Icons.Default.SupportAgent
        "human resource" in normalized || " hr" in normalized || normalized.startsWith("hr") -> Icons.Default.Groups
        "legal" in normalized || "law" in normalized -> Icons.Default.Balance
        "construct" in normalized || "civil" in normalized -> Icons.Default.Construction
        "hospitality" in normalized || "hotel" in normalized || "restaurant" in normalized || "food" in normalized -> Icons.Default.Restaurant
        "retail" in normalized || "store" in normalized -> Icons.Default.Storefront
        "transport" in normalized || "logistic" in normalized || "driver" in normalized || "delivery" in normalized -> Icons.Default.LocalShipping
        "agricultur" in normalized || "farm" in normalized -> Icons.Default.Agriculture
        "security" in normalized || "guard" in normalized -> Icons.Default.Security
        else -> Icons.Default.Work
    }
}

@Composable
fun HeroBanner(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primary, MaterialTheme.shapes.large)
            .padding(Spacing.lg)
    ) {
        Column {
            Text(
                text = "Find your next opportunity",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onPrimary
            )
            Text(
                text = "Verified jobs from trusted employers across Nepal",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f),
                modifier = Modifier.padding(top = Spacing.xxs)
            )
        }
    }
}

@Composable
fun CategoryCard(
    category: JobCategoryModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.width(96.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier.padding(Spacing.sm),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = iconForCategory(category.name),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Text(
                text = category.name,
                style = MaterialTheme.typography.labelMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = Spacing.xxs)
            )
            if (category.jobCount > 0) {
                Text(
                    text = "${category.jobCount} jobs",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun ProfileCompletionCard(
    completionPercentage: Int,
    missingSections: List<String>,
    onCompleteProfileClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (completionPercentage >= 100) return

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = MaterialTheme.shapes.large
    ) {
        Column(modifier = Modifier.padding(Spacing.md)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Complete your profile", style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "$completionPercentage%",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            LinearProgressIndicator(
                progress = { completionPercentage / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = Spacing.xs)
                    .height(6.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            if (missingSections.isNotEmpty()) {
                Text(
                    text = "Missing: ${missingSections.joinToString(", ")}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = Spacing.xs)
                )
            }

            LocalSkillTextButton(text = "Complete now", onClick = onCompleteProfileClick)
        }
    }
}

@Composable
fun HomeSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    onSeeAllClick: (() -> Unit)? = null
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, style = MaterialTheme.typography.titleMedium)
        if (onSeeAllClick != null) {
            LocalSkillTextButton(text = "See all", onClick = onSeeAllClick)
        }
    }
}
