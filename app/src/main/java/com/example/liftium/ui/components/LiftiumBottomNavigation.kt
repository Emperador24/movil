package com.example.liftium.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.liftium.R
import com.example.liftium.ui.theme.LiftiumTheme

// Data class that supports both Painter and ImageVector for flexibility
data class BottomNavItem(
    val route: String,
    val label: String,
    val iconPainter: Painter? = null,
    val iconVector: ImageVector? = null // Fallback for when drawable isn't available
) {
    init {
        require(iconPainter != null || iconVector != null) {
            "Either iconPainter or iconVector must be provided"
        }
    }
}

@Composable
fun LiftiumBottomNavigation(
    items: List<BottomNavItem>,
    currentRoute: String,
    onItemClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        items.forEach { item ->
            NavigationBarItem(
                icon = {
                    when {
                        item.iconPainter != null -> Icon(
                            painter = item.iconPainter,
                            contentDescription = item.label
                        )
                        item.iconVector != null -> Icon(
                            imageVector = item.iconVector,
                            contentDescription = item.label
                        )
                    }
                },
                label = {
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelSmall
                    )
                },
                selected = currentRoute == item.route,
                onClick = { onItemClick(item.route) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    selectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    }
}

@Preview(name = "Light Mode")
@Preview(name = "Dark Mode", uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun LiftiumBottomNavigationPreview() {
    LiftiumTheme {
        val items = listOf(
            BottomNavItem("home", "Home", iconVector = Icons.Default.Home),
            BottomNavItem("profile", "Profile & Settings", iconVector = Icons.Default.Person)
        )
        
        LiftiumBottomNavigation(
            items = items,
            currentRoute = "profile", // Show selected state for testing contrast
            onItemClick = {}
        )
    }
}
