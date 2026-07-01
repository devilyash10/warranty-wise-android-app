package dev.yash.warrantywise.ui.theme

import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun formFieldColors(): TextFieldColors = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = Gold,
    unfocusedBorderColor = DarkOutline,
    focusedLabelColor = Gold,
    unfocusedLabelColor = TextSecondary,
    focusedTextColor = TextOnDark,
    unfocusedTextColor = TextOnDark,
    cursorColor = Gold,
    focusedLeadingIconColor = Gold,
    unfocusedLeadingIconColor = TextSecondary,
    focusedContainerColor = Color.Transparent,
    unfocusedContainerColor = Color.Transparent
)
