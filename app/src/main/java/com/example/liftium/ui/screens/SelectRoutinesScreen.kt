package com.example.liftium.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.liftium.ui.components.CreationStepper
import com.example.liftium.ui.theme.LiftiumTheme

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SelectRoutinesScreen(
    onBackClick: () -> Unit = {}, // Bottom back button (previous step)
    onBackToSettings: () -> Unit = {}, // Top navigation button (to settings)
    onNextClick: (List<String>) -> Unit = {}
) {

    var selectedRoutines by remember { mutableStateOf(listOf<String>()) }


    val availableRoutines = remember {
        mutableStateListOf(*com.example.liftium.data.repository.TemplateRoutineRepository.getAvailableRoutineNames().toTypedArray())
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    TextButton(onClick = onBackToSettings) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Back to Training Split Settings",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            CreationBottomBar(
                onBackClick = onBackClick,
                onNextClick = { onNextClick(selectedRoutines) },
                isNextEnabled = selectedRoutines.isNotEmpty()
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {

            CreationStepper(currentStep = 1)
            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Select Your Routines",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Choose the routines for your split. You can select the same routine multiple times.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(32.dp))


            Text(
                text = "Available Routines",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                availableRoutines.forEach { routine ->
                    SuggestionChip(
                        onClick = {
                            // Add the selected routine to the list
                            selectedRoutines = selectedRoutines + routine
                        },
                        label = { Text(routine) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            // Create Routine Button - Not implemented yet
            OutlinedButton(
                onClick = { /* Not implemented yet */ },
                modifier = Modifier.fillMaxWidth(),
                enabled = false
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Create New Routine (Coming Soon)")
            }

            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                text = "Selected Routines (${selectedRoutines.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (selectedRoutines.isEmpty()) {
                Text(
                    text = "No routines selected yet. Tap on a routine above to add it.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    selectedRoutines.forEachIndexed { index, routine ->
                        InputChip(
                            selected = true,
                            onClick = { /* No action on click */ },
                            label = { Text(routine) },
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Remove routine",
                                    modifier = Modifier
                                        .size(18.dp)
                                        .clickable() {
                                            // Remove this specific instance of the routine
                                            val tempList = selectedRoutines.toMutableList()
                                            tempList.removeAt(index)
                                            selectedRoutines = tempList
                                        }
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}


@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SelectRoutinesScreenPreview() {
    LiftiumTheme {
        SelectRoutinesScreen()
    }
}