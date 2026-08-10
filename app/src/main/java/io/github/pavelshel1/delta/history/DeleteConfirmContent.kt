 package io.github.pavelshel1.delta.history

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import io.github.pavelshel1.delta.ui.theme.AppColors

@Composable
fun DeleteConfirmContent(component: DeleteConfirmComponent) {
    AlertDialog(
        onDismissRequest = component.onDismissRequested,
        containerColor = AppColors.SurfaceHighest,
        titleContentColor = AppColors.OnSurface,
        shape = RoundedCornerShape(24.dp),
        title = {
            Text(text = if (component.count == 1) "Удалить запись?" else "Удалить записи (${component.count})?")
        },
        text = {
            Text(
                text = if (component.count == 1)
                    "Запись будет удалена без возможности восстановления."
                else
                    "Выбранные записи (${component.count}) будут удалены без возможности восстановления.",
                color = AppColors.OnSurfaceVar,
            )
        },
        confirmButton = {
            TextButton(onClick = component.onConfirmed) {
                Text(text = "Удалить", color = AppColors.Primary)
            }
        },
        dismissButton = {
            TextButton(onClick = component.onDismissRequested) {
                Text(text = "Отмена", color = AppColors.OnSurfaceVar)
            }
        },
    )
}
