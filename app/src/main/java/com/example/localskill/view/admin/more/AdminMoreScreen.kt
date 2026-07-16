package com.example.localskill.view.admin.more

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import com.example.localskill.view.theme.Spacing

private data class MoreMenuItem(val title: String, val icon: ImageVector, val onClick: () -> Unit)

@Composable
fun AdminMoreScreen(
    onUsersClick: () -> Unit,
    onCategoriesClick: () -> Unit,
    onActivityClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        MoreMenuItem("User management", Icons.Default.People, onUsersClick),
        MoreMenuItem("Categories", Icons.Default.Category, onCategoriesClick),
        MoreMenuItem("Activity log", Icons.Default.History, onActivityClick),
        MoreMenuItem("Settings", Icons.Default.Settings, onSettingsClick)
    )

    Column(modifier = modifier.fillMaxSize().padding(Spacing.lg)) {
        Text(text = "More", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        items.forEach { item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = Spacing.md)
                    .clickable(onClick = item.onClick),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = item.icon, contentDescription = null)
                Text(text = item.title, modifier = Modifier.weight(1f).padding(start = Spacing.sm))
                Icon(imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
            }
        }
    }
}
