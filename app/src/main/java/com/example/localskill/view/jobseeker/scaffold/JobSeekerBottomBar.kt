package com.example.localskill.view.jobseeker.scaffold

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import com.example.localskill.view.navigation.JobSeekerRoute

private data class BottomBarItem(
    val route: String,
    val label: String,
    val icon: ImageVector
)

private val bottomBarItems = listOf(
    BottomBarItem(JobSeekerRoute.Home.route, "Home", Icons.Default.Home),
    BottomBarItem(JobSeekerRoute.Explore.route, "Explore", Icons.Default.Search),
    BottomBarItem(JobSeekerRoute.Applications.route, "Applications", Icons.Default.Description),
    BottomBarItem(JobSeekerRoute.Saved.route, "Saved", Icons.Default.Bookmark),
    BottomBarItem(JobSeekerRoute.Profile.route, "Profile", Icons.Default.Person)
)

@Composable
fun JobSeekerBottomBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit
) {
    NavigationBar {
        bottomBarItems.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.route,
                onClick = { onNavigate(item.route) },
                icon = { Icon(imageVector = item.icon, contentDescription = item.label) },
                label = {
                    Text(text = item.label, maxLines = 1, overflow = TextOverflow.Ellipsis)
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}
