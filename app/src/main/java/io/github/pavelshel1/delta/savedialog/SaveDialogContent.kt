package io.github.pavelshel1.delta.savedialog

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.pavelshel1.delta.ui.theme.AppColors
import io.github.pavelshel1.delta.ui.theme.DialogFieldLabel
import io.github.pavelshel1.delta.ui.theme.dialogFieldColors

private const val NAME_MAX = 40
private const val COMMENT_MAX = 300

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SaveDialogContent(component: SaveDialogComponent, onSaved: () -> Unit = {}) {
    var name by remember { mutableStateOf("") }
    var comment by remember { mutableStateOf("") }

    val fieldColors = dialogFieldColors()

    AlertDialog(
        onDismissRequest = component.onCancelRequested,
        containerColor = AppColors.SurfaceHighest,
        titleContentColor = AppColors.OnSurface,
        shape = RoundedCornerShape(24.dp),
        title = { Text(text = "Сохранить в историю") },
        text = {
            Column {
                DialogFieldLabel("Название")
                OutlinedTextField(
                    value = name,
                    onValueChange = { if (it.length <= NAME_MAX) name = it },
                    placeholder = { Text("Схема Nº1 Установка АВТ - 3") },
                    singleLine = true,
                    colors = fieldColors,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp, bottom = 14.dp),
                )
                DialogFieldLabel("Комментарий")
                OutlinedTextField(
                    value = comment,
                    onValueChange = { if (it.length <= COMMENT_MAX) comment = it },
                    placeholder = { Text("Дополнительные заметки к расчёту") },
                    minLines = 3,
                    maxLines = 6,
                    colors = fieldColors,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp),
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                component.onSaveRequested(name.trim().ifBlank { null }, comment.trim().ifBlank { null })
                onSaved()
            }) {
                Text(text = "Сохранить", color = AppColors.Primary)
            }
        },
        dismissButton = {
            TextButton(onClick = component.onCancelRequested) {
                Text(text = "Отмена", color = AppColors.OnSurfaceVar)
            }
        },
    )
}
