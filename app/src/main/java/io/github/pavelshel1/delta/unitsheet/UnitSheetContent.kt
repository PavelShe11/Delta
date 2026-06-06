package io.github.pavelshel1.delta.unitsheet

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.pavelshel1.delta.ui.theme.AppColors
import kotlinx.coroutines.launch

private fun FieldKey.unitLabels(): List<String> = when (this) {
    FieldKey.TStart, FieldKey.TEnd -> listOf("К", "°C")
    FieldKey.PStart, FieldKey.PEnd -> listOf("МПа", "кПа", "бар", "атм")
    FieldKey.Time                  -> listOf("ч", "мин", "с")
}

private fun FieldKey.varMain(): String = when (this) {
    FieldKey.TStart, FieldKey.TEnd -> "T"
    FieldKey.PStart, FieldKey.PEnd -> "P"
    FieldKey.Time                  -> "t"
}

private fun FieldKey.varSub(): String? = when (this) {
    FieldKey.TStart, FieldKey.PStart -> "нач"
    FieldKey.TEnd, FieldKey.PEnd     -> "кон"
    FieldKey.Time                    -> null
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnitSheetContent(component: UnitSheetComponent) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val labels = component.fieldKey.unitLabels()

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
            Spacer(modifier = Modifier.padding(start = 8.dp))
            UnitSheetVarChip(
                main = component.fieldKey.varMain(),
                sub = component.fieldKey.varSub(),
            )
        }

        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 0.dp).padding(bottom = 8.dp),
            color = AppColors.OutlineVar,
        )

        labels.forEachIndexed { idx, label ->
            val selected = idx == component.currentIdx
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { hideAndThen { component.onSelect(idx) } }
                    .background(if (selected) AppColors.Primary.copy(alpha = 0.08f) else androidx.compose.ui.graphics.Color.Transparent)
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

@Composable
private fun UnitSheetVarChip(main: String, sub: String?) {
    Row(
        modifier = Modifier
            .clip(CircleShape)
            .background(AppColors.Primary.copy(alpha = 0.10f))
            .border(1.dp, AppColors.Primary.copy(alpha = 0.22f), CircleShape)
            .padding(horizontal = 10.dp, vertical = 2.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        Text(
            text = main,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            fontStyle = FontStyle.Italic,
            color = AppColors.Primary,
        )
        if (sub != null) {
            Text(
                text = sub,
                fontSize = 9.sp,
                color = AppColors.Primary,
                modifier = Modifier.padding(start = 1.dp, bottom = 1.dp),
            )
        }
    }
}
