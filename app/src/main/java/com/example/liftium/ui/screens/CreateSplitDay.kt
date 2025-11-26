package com.example.liftium.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.example.liftium.ui.components.CreationStepper
import com.example.liftium.ui.theme.LiftiumTheme
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Serializable
data class DayState(
    val dayName: String,
    val isTrainingDay: Boolean = false,
    val workoutName: String = "",
    val exerciseCount: Int = 0
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateSplitDayScreen(
    availableRoutines: List<String>,
    onBackClick: () -> Unit = {},
    onBackToSettings: () -> Unit = {},
    onNextClick: (List<DayState>) -> Unit = {}
) {

    var daysState by rememberSaveable(stateSaver = listSaver) {
        mutableStateOf(
            mutableListOf(
                DayState("Sunday"),
                DayState("Monday", isTrainingDay = true),
                DayState("Tuesday"),
                DayState("Wednesday"),
                DayState("Thursday"),
                DayState("Friday"),
                DayState("Saturday")
            )
        )
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
                onNextClick = { onNextClick(daysState) },
                isNextEnabled = true
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
            CreationStepper(currentStep = 2)
            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Set Up Your Training Days",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Choose which days are training days and which are rest days",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(24.dp))

            daysState.forEachIndexed { index, day ->
                DaySetupCard(
                    dayState = day,
                    onDayStateChange = { newDayState ->
                        val newList = daysState.toMutableList()
                        newList[index] = newDayState
                        daysState = newList
                    },
                    availableWorkouts = availableRoutines
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DaySetupCard(
    dayState: DayState,
    onDayStateChange: (DayState) -> Unit,
    availableWorkouts: List<String> = listOf("Push", "Pull", "Legs", "Upper Body", "Lower Body")
) {
    val radioOptions = listOf("Training", "Rest")
    val selectedOption = if (dayState.isTrainingDay) "Training" else "Rest"
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = dayState.dayName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Row {
                    radioOptions.forEach { text ->
                        Row(
                            Modifier
                                .selectable(
                                    selected = (text == selectedOption),
                                    onClick = {
                                        onDayStateChange(
                                            dayState.copy(isTrainingDay = text == "Training")
                                        )
                                    },
                                    role = Role.RadioButton
                                )
                                .padding(horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (text == selectedOption),
                                onClick = null,
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = MaterialTheme.colorScheme.primary
                                )
                            )
                            Text(
                                text = text,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }
                    }
                }
            }

            if (dayState.isTrainingDay) {
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "Workout",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded },
                    modifier = Modifier
                        .fillMaxWidth()
                        .zIndex(1f)
                ) {
                    OutlinedTextField(
                        value = dayState.workoutName.ifEmpty { "Select a workout" },
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Workout") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                        },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        availableWorkouts.forEach { workout ->
                            DropdownMenuItem(
                                text = { Text(workout) },
                                onClick = {
                                    onDayStateChange(dayState.copy(workoutName = workout))
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

val listSaver = androidx.compose.runtime.saveable.listSaver<MutableList<DayState>, Any>(
    save = { list ->
        list.map {
            mapOf(
                "dayName" to it.dayName,
                "isTraining" to it.isTrainingDay,
                "workoutName" to it.workoutName
            )
        }
    },
    restore = { restored ->
        restored.map {
            val map = it as Map<String, Any>
            DayState(
                dayName = map["dayName"] as String,
                isTrainingDay = map["isTraining"] as Boolean,
                workoutName = map["workoutName"] as String,
            )
        }.toMutableStateList()
    }
)
/*
@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun CreateSplitDayScreenPreview() {
    LiftiumTheme {
        CreateSplitDayScreen()
    }
}*/

@Composable
fun CreationBottomBar(
    onBackClick: () -> Unit = {},
    onNextClick: () -> Unit,
    isNextEnabled: Boolean
) {
    Surface(
        color = MaterialTheme.colorScheme.background,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onBackClick) {
                Text("Back", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Button(
                onClick = onNextClick,
                enabled = isNextEnabled,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Text("Next", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}