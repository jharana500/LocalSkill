package com.example.localskill.view.common.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.localskill.model.JobModel
import com.example.localskill.utils.DateUtils
import com.example.localskill.utils.SalaryFormatter
import com.example.localskill.view.theme.Spacing

@Composable
fun JobCard(
    job: JobModel,
    isSaved: Boolean,
    onSaveToggle: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    applicationStatus: String? = null
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(Spacing.md)) {
            Row(verticalAlignment = Alignment.Top) {
                RemoteAvatar(
                    imageUrl = job.companyLogoUrl,
                    fallbackText = job.companyName,
                    size = 44.dp,
                    shape = RoundedCornerShape(10.dp)
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = Spacing.sm)
                ) {
                    Text(
                        text = job.title,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = job.companyName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (job.companyVerified) {
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = "Verified company",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .padding(start = Spacing.xxs)
                                    .height(14.dp)
                            )
                        }
                    }
                }

                IconButton(onClick = onSaveToggle) {
                    Icon(
                        imageVector = if (isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = if (isSaved) "Unsave job" else "Save job",
                        tint = if (isSaved) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(Spacing.xs))

            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.xxs)) {
                if (job.location.isNotBlank()) {
                    StatusChip(text = job.location, tone = StatusChipTone.NEUTRAL)
                }
                if (job.jobType.isNotBlank()) {
                    StatusChip(text = job.jobType.replace('_', ' '), tone = StatusChipTone.NEUTRAL)
                }
                if (job.workplaceType.isNotBlank()) {
                    StatusChip(text = job.workplaceType.replace('_', ' '), tone = StatusChipTone.NEUTRAL)
                }
            }

            Spacer(modifier = Modifier.height(Spacing.sm))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = SalaryFormatter.format(job.minimumSalary, job.maximumSalary, job.currency),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = DateUtils.formatRelative(job.createdAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (applicationStatus != null) {
                Spacer(modifier = Modifier.height(Spacing.xs))
                StatusChip(text = applicationStatus.replace('_', ' '), tone = StatusChipTone.SUCCESS)
            }
        }
    }
}

@Composable
fun JobCardSkeleton(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(Spacing.md)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                SkeletonBlock(modifier = Modifier.size(44.dp), shape = RoundedCornerShape(10.dp))
                Column(modifier = Modifier.padding(start = Spacing.sm)) {
                    SkeletonBlock(modifier = Modifier.width(160.dp).height(16.dp))
                    Spacer(modifier = Modifier.height(Spacing.xxs))
                    SkeletonBlock(modifier = Modifier.width(100.dp).height(12.dp))
                }
            }
            Spacer(modifier = Modifier.height(Spacing.sm))
            SkeletonBlock(modifier = Modifier.width(180.dp).height(14.dp))
        }
    }
}

@Composable
private fun SkeletonBlock(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(6.dp)
) {
    Box(
        modifier = modifier
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
    )
}
