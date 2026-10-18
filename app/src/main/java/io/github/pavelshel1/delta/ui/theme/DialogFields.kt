package io.github.pavelshel1.delta.ui.theme

import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.sp

@Composable
fun dialogFieldColors(): TextFieldColors = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = AppColors.Primary,
    unfocusedBorderColor = AppColors.OnSurfaceVar,
    focusedTextColor = AppColors.OnSurface,
    unfocusedTextColor = AppColors.OnSurface,
    focusedPlaceholderColor = AppColors.OnSurfaceVar,
    unfocusedPlaceholderColor = AppColors.OnSurfaceVar,
)

@Composable
fun DialogFieldLabel(text: String) {
    Text(
        text = text.uppercase(),
        fontSize = 11.sp,
        color = AppColors.OnSurfaceVar,
        letterSpacing = 0.6.sp,
    )
}
