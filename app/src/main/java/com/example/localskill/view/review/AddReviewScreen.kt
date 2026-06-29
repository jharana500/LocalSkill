package com.example.localskill.view.review

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.localskill.view.components.ErrorMessage
import com.example.localskill.view.components.LoadingState
import com.example.localskill.view.components.LocalSkillButton
import com.example.localskill.view.components.LocalSkillTextField
import com.example.localskill.view.components.RatingBar
import com.example.localskill.viewmodel.review.AddReviewUiState

@Composable
fun AddReviewScreen(
    state: AddReviewUiState,
    onRatingSelected: (Int) -> Unit,
    onCommentChange: (String) -> Unit,
    onSubmit: () -> Unit
) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Leave a Review", style = MaterialTheme.typography.headlineMedium)
        Text(state.jobTitle.ifBlank { "Completed work" }, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text("For ${state.receiverName.ifBlank { "this user" }}", style = MaterialTheme.typography.titleMedium)
        if (state.isLoading) LoadingState()
        RatingBar(rating = state.rating, onRatingSelected = onRatingSelected)
        LocalSkillTextField(
            value = state.comment,
            onValueChange = onCommentChange,
            label = "Review comment",
            minLines = 4
        )
        ErrorMessage(state.errorMessage)
        state.successMessage?.let { Text(it, color = MaterialTheme.colorScheme.primary) }
        Spacer(Modifier.height(4.dp))
        LocalSkillButton(
            text = if (state.hasReviewed) "Reviewed" else "Submit Review",
            isLoading = state.isSubmitting,
            onClick = onSubmit,
            enabled = !state.hasReviewed
        )
    }
}
