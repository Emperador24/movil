package com.example.liftium.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CreationStepper(currentStep: Int) {

    val steps = listOf("Split Selection", "Day Setup", "Review")

    Row (
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        steps.forEachIndexed { index, title ->

            val stepNumber = index + 1
            val isActive = stepNumber == currentStep
            val isCompleted = stepNumber < currentStep

            Step(
                number = stepNumber,
                title = title,
                isActive = isActive,
                isCompleted = isCompleted
            )

            if(index < steps.size - 1) {

                Divider(
                    modifier = Modifier.weight(1f),
                    color = if(isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                    thickness = 1.dp
                )
            }
        }

    }
}

@Composable
private fun Step(number: Int, title: String, isActive: Boolean, isCompleted: Boolean) {

    val circleColor = when {
        isActive -> MaterialTheme.colorScheme.primary
        isCompleted -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    val textColor = when {

        isActive -> MaterialTheme.colorScheme.onPrimary
        isCompleted -> MaterialTheme.colorScheme.onPrimary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Column (
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(IntrinsicSize.Min)
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(circleColor),
                contentAlignment = Alignment.Center
            ) {
                Text (

                    text = "✓",
                    color = if( isCompleted) MaterialTheme.colorScheme.onPrimary else Color.Transparent,
                    fontWeight = FontWeight.Bold
                )

                if(!isCompleted) {

                    Text(
                        text = "$number",
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = title,
            color = textColor,
            fontSize = 12.sp,
            fontWeight = if (isActive || isCompleted) FontWeight.SemiBold else FontWeight.Normal,
            maxLines = 1
        )

    }
}



@Preview(showBackground = true)
@Composable
fun CreationStepperPreview() {
    // Set the current step to simulate the preview of different steps
    Column {
        CreationStepper(currentStep = 1) // Preview for step 1
        CreationStepper(currentStep = 2) // Preview for step 2
        CreationStepper(currentStep = 3) // Preview for step 3
    }
}
