package io.github.pavelshel1.delta.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.pavelshel1.delta.ui.theme.AppColors

@Composable
fun HistoryContent(component: HistoryComponent, modifier: Modifier = Modifier) {
    val entries by component.entries.collectAsState()
    var showClearDialog by remember { mutableStateOf(false) }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = {
                Text(
                    text = "Очистить историю?",
                    color = AppColors.OnSurface,
                )
            },
            text = {
                Text(
                    text = "Все сохранённые расчёты будут удалены без возможности восстановления.",
                    color = AppColors.OnSurfaceVar,
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    component.onClear()
                    showClearDialog = false
                }) {
                    Text(text = "Очистить", color = AppColors.Primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text(text = "Отмена", color = AppColors.OnSurfaceVar)
                }
            },
            containerColor = AppColors.SurfaceHighest,
            titleContentColor = AppColors.OnSurface,
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppColors.Background),
    ) {
        // TopBar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.statusBars)
                .height(56.dp)
                .background(AppColors.Background),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = component::onDismiss) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Закрыть",
                    tint = AppColors.Primary,
                )
            }
            Text(
                text = "История расчётов",
                fontSize = 18.sp,
                fontWeight = FontWeight.Normal,
                color = AppColors.OnSurface,
                modifier = Modifier.weight(1f),
            )
            if (entries.isNotEmpty()) {
                IconButton(onClick = { showClearDialog = true }) {
                    Icon(
                        imageVector = Icons.Default.DeleteSweep,
                        contentDescription = "Очистить историю",
                        tint = AppColors.OnSurfaceVar,
                    )
                }
            }
        }

        HorizontalDivider(color = AppColors.OutlineVar)

        if (entries.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 44.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Icon(
                    imageVector = Icons.Outlined.AccessTime,
                    contentDescription = null,
                    tint = AppColors.OnSurface.copy(alpha = 0.20f),
                    modifier = Modifier.size(52.dp),
                )
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "История пуста",
                    fontSize = 15.sp,
                    color = AppColors.OnSurfaceVar,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Сохраняйте результаты, нажав значок закладки под расчётом",
                    fontSize = 12.sp,
                    color = AppColors.OnSurfaceVar.copy(alpha = 0.55f),
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp,
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(entries, key = { it.id }) { entry ->
                    HistoryCard(
                        entry = entry,
                        onDelete = { component.onDelete(entry.id) },
                        modifier = Modifier.animateItem(),
                    )
                }
            }
        }
    }
}
