package com.example.localskill.view.employer

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.localskill.view.components.EmptyState
import com.example.localskill.view.components.ErrorMessage
import com.example.localskill.view.components.LoadingState
import com.example.localskill.view.components.LocalSkillTextField
import com.example.localskill.view.components.WorkerCard
import com.example.localskill.viewmodel.employer.WorkersUiState

@Composable
fun WorkersScreen(
    state: WorkersUiState,
    onSearchChange: (String) -> Unit,
    onRadiusSelected: (Double?) -> Unit,
    distanceText: (com.example.localskill.model.UserModel) -> String?
) {
    val radiusOptions = listOf(null to "All", 2.0 to "Within 2 km", 5.0 to "Within 5 km", 10.0 to "Within 10 km", 25.0 to "Within 25 km")

    LazyColumn(
        Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Nearby Workers", style = MaterialTheme.typography.headlineMedium)
            Text("Discover skilled workers around your job location.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        item {
            LocalSkillTextField(
                value = state.searchQuery,
                onValueChange = onSearchChange,
                label = "Search workers, skills, or area"
            )
        }
        item {
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                radiusOptions.forEach { (radius, label) ->
                    FilterChip(
                        selected = state.selectedRadiusKm == radius,
                        onClick = { onRadiusSelected(radius) },
                        label = { Text(label) }
                    )
                }
            }
        }
        if (state.isLoading) {
            item { LoadingState() }
        }
        item { ErrorMessage(state.errorMessage) }
        if (!state.isLoading && state.filteredWorkers.isEmpty()) {
            item { EmptyState("No nearby workers found.\nTry increasing the radius or searching another skill.") }
        } else {
            items(state.filteredWorkers) { worker -> WorkerCard(worker, distanceText = distanceText(worker)) }
        }
    }
}
