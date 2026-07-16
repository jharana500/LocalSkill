package com.example.localskill.view.admin.categories

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.localskill.model.JobCategoryModel
import com.example.localskill.view.common.components.LocalSkillCard
import com.example.localskill.view.common.components.LocalSkillTextField
import com.example.localskill.view.common.components.LocalSkillTopAppBar
import com.example.localskill.view.common.states.EmptyState
import com.example.localskill.view.common.states.ErrorMessage
import com.example.localskill.view.common.states.FullScreenLoading
import com.example.localskill.view.theme.Spacing
import com.example.localskill.viewmodel.AdminCategoryEvent
import com.example.localskill.viewmodel.AdminCategoryViewModel

@Composable
fun AdminCategoriesScreen(
    viewModel: AdminCategoryViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var newCategoryName by remember { mutableStateOf("") }

    LaunchedEffect(Unit) { viewModel.loadCategories() }
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is AdminCategoryEvent.ShowMessage -> {
                    snackbarHostState.showSnackbar(event.message)
                    newCategoryName = ""
                }
            }
        }
    }

    Scaffold(
        topBar = { LocalSkillTopAppBar(title = "Categories", onBack = onBack) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).fillMaxSize().padding(Spacing.lg)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                LocalSkillTextField(
                    value = newCategoryName,
                    onValueChange = { newCategoryName = it },
                    label = "New category name",
                    modifier = Modifier.weight(1f)
                )
                TextButton(
                    onClick = { viewModel.addCategory(newCategoryName) },
                    enabled = newCategoryName.isNotBlank() && !uiState.isSaving
                ) { Text("Add") }
            }

            when {
                uiState.isLoading -> FullScreenLoading()

                uiState.errorMessage != null && uiState.categories.isEmpty() ->
                    ErrorMessage(message = uiState.errorMessage.orEmpty(), modifier = Modifier.padding(top = Spacing.md))

                uiState.categories.isEmpty() -> EmptyState(title = "No categories yet", description = "Add your first job category above.")

                else -> LazyColumn(
                    contentPadding = PaddingValues(vertical = Spacing.md),
                    verticalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {
                    items(uiState.categories, key = { it.id }) { category ->
                        CategoryRow(
                            category = category,
                            onToggle = { active -> viewModel.setCategoryActive(category.id, active) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryRow(category: JobCategoryModel, onToggle: (Boolean) -> Unit) {
    LocalSkillCard(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = category.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
                Text(
                    text = "${category.jobCount} jobs",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(checked = category.isActive, onCheckedChange = onToggle)
        }
    }
}
