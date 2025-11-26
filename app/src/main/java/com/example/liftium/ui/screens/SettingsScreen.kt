package com.example.liftium.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.liftium.R
import com.example.liftium.mock.MockData
import com.example.liftium.ui.components.BottomNavItem
import com.example.liftium.ui.components.LiftiumBottomNavigation
import com.example.liftium.ui.components.LiftiumTopAppBar
import com.example.liftium.ui.theme.Dimens
import com.example.liftium.ui.theme.LiftiumTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit = {},
    onEditTrainingSplit: () -> Unit = {},
    onLogOut: () -> Unit = {},
    onNavigateToHome: () -> Unit = {},
    currentRoute: String = "profile",
    modifier: Modifier = Modifier
) {
    val user = MockData.currentUser
    var showLogoutDialog by remember { mutableStateOf(false) }
    
    val bottomNavItems = listOf(
        BottomNavItem("home", "Home", iconVector = Icons.Default.Home),
        BottomNavItem("profile", "Profile & Settings", iconVector = Icons.Default.Person)
    )
    
    Scaffold(
        topBar = {
            LiftiumTopAppBar(
                title = "LIFTIUM"
            )
        },
        bottomBar = {
            LiftiumBottomNavigation(
                items = bottomNavItems,
                currentRoute = currentRoute,
                onItemClick = { route ->
                    when (route) {
                        "home" -> onNavigateToHome()
                        "profile" -> { /* Already on settings */ }
                    }
                }
            )
        },
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Dimens.ScreenPadding)
        ) {
            Spacer(modifier = Modifier.height(Dimens.SpaceLarge))
            
            // Page Title
            Text(
                text = "User Settings",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            
            Spacer(modifier = Modifier.height(Dimens.SpaceSmall))
            
            // Subtitle
            Text(
                text = "Manage your account preferences",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = Dimens.SectionSpacing)
            )
            
            // Training Split Section
            SettingsCard(
                title = "Training Split",
                description = "Customize your weekly workout schedule and exercise preferences.",
                buttonText = "Edit Training Split",
                buttonIconPainter = painterResource(id = R.drawable.fitness_center_24px),
                onButtonClick = onEditTrainingSplit
            )
            
            Spacer(modifier = Modifier.height(Dimens.SectionSpacing))
            
            // Account Access Section
            SettingsCard(
                title = "Account Access",
                description = "Clear your current session data and return to the main screen.",
                buttonText = "Log Out",
                buttonIconPainter = painterResource(id = R.drawable.fitness_center_24px), // You can replace with logout icon
                onButtonClick = { showLogoutDialog = true },
                isDestructive = false
            )
            
            Spacer(modifier = Modifier.height(Dimens.SpaceExtraLarge))
        }
    }
    
    // Logout Confirmation Dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { 
                Text(
                    "Log Out",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                ) 
            },
            text = { 
                Text(
                    "Are you sure you want to log out? This will clear your current session data and return you to the main screen.",
                    style = MaterialTheme.typography.bodyMedium
                ) 
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        onLogOut()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("Log Out")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showLogoutDialog = false }
                ) {
                    Text("Cancel")
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            textContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsCard(
    title: String,
    description: String,
    buttonText: String,
    buttonIconPainter: Painter,
    onButtonClick: () -> Unit,
    isDestructive: Boolean = false,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Dimens.CardPadding),
        colors = CardDefaults.cardColors(
            containerColor = if (isDestructive) {
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = Dimens.CardElevation
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.CardPadding)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = if (isDestructive) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            )
            
            Spacer(modifier = Modifier.height(Dimens.SpaceSmall))
            
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
            )
            
            if (isDestructive) {
                Spacer(modifier = Modifier.height(Dimens.SpaceSmall))
                Text(
                    text = "Warning: This action cannot be undone. All your data will be permanently removed.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Medium
                )
            }
            
            Spacer(modifier = Modifier.height(Dimens.SpaceLarge))
            
            Button(
                onClick = onButtonClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isDestructive) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.primary
                    }
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    painter = buttonIconPainter,
                    contentDescription = null,
                    modifier = Modifier.size(Dimens.IconSizeSmall)
                )
                Spacer(modifier = Modifier.width(Dimens.SpaceSmall))
                Text(
                    text = buttonText,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Preview(name = "Light Mode")
@Preview(name = "Dark Mode", uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SettingsScreenPreview() {
    LiftiumTheme {
        SettingsScreen(
            currentRoute = "profile"
        )
    }
}
