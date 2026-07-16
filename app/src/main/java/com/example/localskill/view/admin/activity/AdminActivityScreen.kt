package com.example.localskill.view.admin.activity

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.localskill.utils.DateUtils
import com.example.localskill.view.common.components.LocalSkillCard
import com.example.localskill.view.common.components.LocalSkillTopAppBar
import com.example.localskill.view.common.states.EmptyState
import com.example.localskill.view.common.states.FullScreenLoading
import com.example.localskill.view.theme.Spacing
import com.example.localskill.viewmodel.AdminDashboardViewModel

@Composable
fun AdminActivityScreen(
    viewModel: AdminDashboardViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { viewModel.loadActivityLog() }

    Scaffold(
        topBar = { LocalSkillTopAppBar(title = "Activity log", onBack = onBack) }
    ) { innerPadding ->
        when {
            uiState.isLoading -> FullScreenLoading(modifier = Modifier.padding(innerPadding).fillMaxSize())

            uiState.recentActivity.isEmpty() -> EmptyState(
                title = "No activity yet",
                description = "Administrator actions will appear here.",
                modifier = Modifier.padding(innerPadding).fillMaxSize()
            )

            else -> LazyColumn(
                modifier = Modifier.padding(innerPadding).fillMaxSize(),
                contentPadding = PaddingValues(Spacing.lg),
                verticalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                items(uiState.recentActivity, key = { it.id }) { activity ->
                    LocalSkillCard(modifier = Modifier.fillMaxWidth()) {
                        Column {
                            Text(text = activity.summary, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                            Text(
                                text = DateUtils.formatDate(activity.createdAt),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
