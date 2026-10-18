package io.github.pavelshel1.delta.mail

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import io.github.pavelshel1.delta.ui.theme.AppColors
import io.github.pavelshel1.delta.ui.theme.DialogFieldLabel
import io.github.pavelshel1.delta.ui.theme.dialogFieldColors

/** Адреса из истории, содержащие введённый текст; точное совпадение не подсказываем. */
internal fun matchingRecipients(all: List<String>, query: String): List<String> {
    val q = query.trim()
    return all.filter { it.contains(q, ignoreCase = true) && !it.equals(q, ignoreCase = true) }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MailDialogContent(component: MailDialogComponent) {
    val context = LocalContext.current
    val status by component.status.subscribeAsState()
    val sending = status == MailSendStatus.Sending

    var to by remember { mutableStateOf(component.initialTo) }
    var subject by remember { mutableStateOf(component.initialSubject) }
    var body by remember { mutableStateOf("") }

    var toFocused by remember { mutableStateOf(false) }
    var suggestionsDismissed by remember { mutableStateOf(false) }
    val suggestions = remember(to) { matchingRecipients(component.recipients, to) }
    val suggestionsVisible = toFocused && !suggestionsDismissed && !sending && suggestions.isNotEmpty()

    val fieldColors = dialogFieldColors()

    LaunchedEffect(status) {
        if (status == MailSendStatus.Sent) {
            Toast.makeText(context, "Письмо отправлено", Toast.LENGTH_SHORT).show()
            component.onDismissRequested()
        }
    }

    AlertDialog(
        onDismissRequest = { if (!sending) component.onDismissRequested() },
        containerColor = AppColors.SurfaceHighest,
        titleContentColor = AppColors.OnSurface,
        shape = RoundedCornerShape(24.dp),
        title = { Text(text = "Отправить по почте") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                DialogFieldLabel("Кому")
                ExposedDropdownMenuBox(
                    expanded = suggestionsVisible,
                    onExpandedChange = { suggestionsDismissed = !it },
                ) {
                    OutlinedTextField(
                        value = to,
                        onValueChange = {
                            to = it
                            suggestionsDismissed = false
                        },
                        placeholder = { Text("ivanov@company.local") },
                        singleLine = true,
                        enabled = !sending,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        colors = fieldColors,
                        modifier = Modifier
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable)
                            .onFocusChanged { toFocused = it.isFocused }
                            .fillMaxWidth()
                            .padding(top = 6.dp, bottom = 14.dp),
                    )
                    ExposedDropdownMenu(
                        expanded = suggestionsVisible,
                        onDismissRequest = { suggestionsDismissed = true },
                    ) {
                        suggestions.forEach { address ->
                            DropdownMenuItem(
                                text = { Text(address) },
                                onClick = {
                                    to = address
                                    suggestionsDismissed = true
                                },
                            )
                        }
                    }
                }

                DialogFieldLabel("Тема")
                OutlinedTextField(
                    value = subject,
                    onValueChange = { subject = it },
                    enabled = !sending,
                    maxLines = 3,
                    colors = fieldColors,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp, bottom = 14.dp),
                )

                DialogFieldLabel("Текст")
                OutlinedTextField(
                    value = body,
                    onValueChange = { body = it },
                    enabled = !sending,
                    placeholder = { Text("Текст письма") },
                    minLines = 3,
                    maxLines = 6,
                    colors = fieldColors,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp),
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(top = 12.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.AttachFile,
                        contentDescription = null,
                        tint = AppColors.OnSurfaceVar,
                        modifier = Modifier.size(16.dp),
                    )
                    Text(
                        text = "Excel-файл · актов: ${component.attachmentCount}",
                        fontSize = 12.sp,
                        color = AppColors.OnSurfaceVar,
                    )
                }

                (status as? MailSendStatus.Failed)?.let {
                    Text(
                        text = it.message,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 10.dp),
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = to.isNotBlank() && !sending,
                onClick = { component.onSendRequested(to.trim(), subject.trim(), body) },
            ) {
                if (sending) {
                    CircularProgressIndicator(
                        color = AppColors.Primary,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(18.dp),
                    )
                } else {
                    Text(
                        text = "Отправить",
                        color = if (to.isNotBlank()) AppColors.Primary else AppColors.OnSurfaceVar,
                    )
                }
            }
        },
        dismissButton = {
            TextButton(enabled = !sending, onClick = component.onDismissRequested) {
                Text(text = "Отмена", color = AppColors.OnSurfaceVar)
            }
        },
    )
}
