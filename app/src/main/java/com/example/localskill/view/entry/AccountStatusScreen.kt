package com.example.localskill.view.entry

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.example.localskill.model.AccountStatus
import com.example.localskill.view.common.components.LocalSkillTextButton
import com.example.localskill.view.common.components.StatusChip
import com.example.localskill.view.common.components.StatusChipTone
import com.example.localskill.view.theme.LocalSkillTheme
import com.example.localskill.view.theme.Spacing

@Composable
fun AccountStatusScreen(
    accountStatus: AccountStatus,
    onLogout: () -> Unit
) {
    val (title, message, tone) = when (accountStatus) {
        AccountStatus.PENDING -> Triple(
            "Account under review",
            "We're verifying your company details. This usually takes 1-2 business days.",
            StatusChipTone.WARNING
        )

        AccountStatus.SUSPENDED -> Triple(
            "Account suspended",
            "Your account has been suspended. Contact support for more information.",
            StatusChipTone.ERROR
        )

        AccountStatus.ACTIVE -> Triple(
            "Account status",
            "Your account is active.",
            StatusChipTone.SUCCESS
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(Spacing.lg),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.HourglassTop,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = Spacing.md)
        )

        StatusChip(text = accountStatus.name, tone = tone)

        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = Spacing.md)
        )

        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = Spacing.xs)
        )

        LocalSkillTextButton(
            text = "Log out",
            onClick = onLogout,
            modifier = Modifier.padding(top = Spacing.xl)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AccountStatusScreenPendingPreview() {
    LocalSkillTheme {
        AccountStatusScreen(accountStatus = AccountStatus.PENDING, onLogout = {})
    }
}
