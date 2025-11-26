package com.example.liftium.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.liftium.mock.MockData
import com.example.liftium.model.DayOfWeek
import com.example.liftium.model.SplitDay
import com.example.liftium.ui.theme.Dimens
import com.example.liftium.ui.theme.LiftiumTheme

@Composable
fun WorkoutDaySelector(
    selectedDay: String,
    dayOfWeek: String,
    availableDays: List<SplitDay>,
    onDaySelected: (SplitDay) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    
    Box(modifier = modifier.fillMaxWidth()) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded },
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            shape = RoundedCornerShape(Dimens.CardPadding),
            elevation = CardDefaults.cardElevation(
                defaultElevation = Dimens.CardElevation
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Dimens.CardPadding),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceMedium)
                ) {
                    // Day indicator
                    Surface(
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Text(
                                text = dayOfWeek.take(3), // Mon, Tue, etc.
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                    
                    Column {
                        Text(
                            text = selectedDay,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = dayOfWeek,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = "Select workout day",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        
        // Dropdown Menu
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth()
        ) {
            availableDays.forEach { splitDay ->
                DropdownMenuItem(
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceMedium)
                        ) {
                            // Day indicator
                            Surface(
                                color = if (splitDay.isRestDay) 
                                    MaterialTheme.colorScheme.surfaceVariant 
                                else 
                                    MaterialTheme.colorScheme.primary,
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.size(32.dp)
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    Text(
                                        text = DayOfWeek.fromInt(splitDay.dayOfWeek).displayName.take(3),
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (splitDay.isRestDay) 
                                            MaterialTheme.colorScheme.onSurfaceVariant 
                                        else 
                                            MaterialTheme.colorScheme.onPrimary
                                    )
                                }
                            }
                            
                            Column {
                                Text(
                                    text = splitDay.name,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = DayOfWeek.fromInt(splitDay.dayOfWeek).displayName,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    onClick = {
                        onDaySelected(splitDay)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Preview(name = "Light Mode")
@Preview(name = "Dark Mode", uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun WorkoutDaySelectorPreview() {
    LiftiumTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            WorkoutDaySelector(
                selectedDay = "Legs",
                dayOfWeek = "Monday",
                availableDays = MockData.allSplitDays,
                onDaySelected = {}
            )
            
            WorkoutDaySelector(
                selectedDay = "Push",
                dayOfWeek = "Tuesday", 
                availableDays = MockData.allSplitDays,
                onDaySelected = {}
            )
            
            WorkoutDaySelector(
                selectedDay = "Rest",
                dayOfWeek = "Sunday",
                availableDays = MockData.allSplitDays,
                onDaySelected = {}
            )
        }
    }
}