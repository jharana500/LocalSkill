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
    /** The registered route template — compared against the current back-stack entry's route. */
    val templateRoute: String,
    /** The concrete route actually passed to navigate(). */
    val navigateRoute: String,
    val label: String,
    val icon: ImageVector
)

private val bottomBarItems = listOf(
    BottomBarItem(JobSeekerRoute.Home.route, JobSeekerRoute.Home.route, "Home", Icons.Default.Home),
    BottomBarItem(JobSeekerRoute.Explore.route, JobSeekerRoute.Explore.BASE_ROUTE, "Explore", Icons.Default.Search),
    BottomBarItem(JobSeekerRoute.Applications.route, JobSeekerRoute.Applications.route, "Applications", Icons.Default.Description),
    BottomBarItem(JobSeekerRoute.Saved.route, JobSeekerRoute.Saved.route, "Saved", Icons.Default.Bookmark),
    BottomBarItem(JobSeekerRoute.Profile.route, JobSeekerRoute.Profile.route, "Profile", Icons.Default.Person)
)

@Composable
fun JobSeekerBottomBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit
) {
    NavigationBar {
        bottomBarItems.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.templateRoute,
                onClick = { onNavigate(item.navigateRoute) },
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
