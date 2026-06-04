package io.github.pavelshel1.delta.unitsheet

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.pavelshel1.delta.ui.theme.AppColors
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch


private val UNIT_LABELS = listOf("К", "°C")

private fun FieldKey.displayName(): String = when (this) {
    FieldKey.TStart -> "T нач"
    FieldKey.TEnd   -> "T кон"
    FieldKey.PStart -> "P нач"
    FieldKey.PEnd   -> "P кон"
    FieldKey.Time   -> "t"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnitSheetContent(component: UnitSheetComponent) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    fun hideAndThen(action: () -> Unit) {
        scope.launch { sheetState.hide() }.invokeOnCompletion { action() }
    }

    ModalBottomSheet(
        onDismissRequest = component::onDismiss,
        sheetState = sheetState,
        containerColor = AppColors.Surface,
    ) {
        Row(
            modifier = Modifier.padding(start = 24.dp, end = 24.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Единица измерения",
                fontSize = 13.sp,
                color = AppColors.OnSurfaceVar,
                letterSpacing = 0.3.sp,
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = component.fieldKey.displayName(),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = AppColors.Primary,
            )
        }

        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 24.dp),
            color = AppColors.OutlineVar,
        )

        UNIT_LABELS.forEachIndexed { idx, label ->
            val selected = idx == component.currentIdx
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { hideAndThen { component.onSelect(idx) } }
                    .padding(horizontal = 24.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = label,
                    fontSize = 20.sp,
                    fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal,
                    color = if (selected) AppColors.Primary else AppColors.OnSurface,
                    modifier = Modifier.weight(1f),
                )
                if (selected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = AppColors.Primary,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}
