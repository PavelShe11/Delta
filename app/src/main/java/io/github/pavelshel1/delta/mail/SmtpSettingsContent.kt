package io.github.pavelshel1.delta.mail

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import io.github.pavelshel1.delta.ui.theme.AppColors
import io.github.pavelshel1.delta.ui.theme.DialogFieldLabel
import io.github.pavelshel1.delta.ui.theme.dialogFieldColors

@Composable
fun SmtpSettingsContent(component: SmtpSettingsComponent) {
    val context = LocalContext.current
    var from by remember { mutableStateOf(component.initial.from) }
    var host by remember { mutableStateOf(component.initial.host) }
    var port by remember { mutableStateOf(component.initial.port.toString()) }
        val fieldColors = dialogFieldColors()

    AlertDialog(
        onDismissRequest = component.onCancelRequested,
        containerColor = AppColors.SurfaceHighest,
        titleContentColor = AppColors.OnSurface,
        shape = RoundedCornerShape(24.dp),
        title = { Text(text = "Настройки SMTP") },
        text = {
            Column {
                DialogFieldLabel("Отправитель (From)")
                OutlinedTextField(
                    value = from,
                    onValueChange = { from = it },
                    placeholder = { Text("delta@company.local") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    colors = fieldColors,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp, bottom = 14.dp),
                )
                DialogFieldLabel("SMTP-сервер")
                OutlinedTextField(
                    value = host,
                    onValueChange = { host = it },
                    placeholder = { Text("smtp.company.local") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                    colors = fieldColors,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp, bottom = 14.dp),
                )
                DialogFieldLabel("Порт")
                OutlinedTextField(
                    value = port,
                    onValueChange = { if (it.length <= 5 && it.all(Char::isDigit)) port = it },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = fieldColors,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp),
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    component.onSaveRequested(parseSmtpSettings(from, host, port))
                    Toast.makeText(context, "Настройки SMTP сохранены", Toast.LENGTH_SHORT).show()
                },
            ) {
                Text(
                    text = "Сохранить",
                    color = AppColors.Primary,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = component.onCancelRequested) {
                Text(text = "Отмена", color = AppColors.OnSurfaceVar)
            }
        },
    )
}
