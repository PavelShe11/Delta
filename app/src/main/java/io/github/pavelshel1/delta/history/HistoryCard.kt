package io.github.pavelshel1.delta.history

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.ui.unit.Dp
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.core.os.ConfigurationCompat
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.pavelshel1.delta.formula.DeltaPAbstractFormula
import io.github.pavelshel1.delta.formula.DeltaPFormulaWithValues
import io.github.pavelshel1.delta.ui.theme.AppColors
import java.text.SimpleDateFormat
import java.util.Date

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HistoryCard(
    entry: HistoryEntry,
    onDelete: () -> Unit,
    onShare: () -> Unit,
    onMail: (() -> Unit)?,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    selectionMode: Boolean,
    selected: Boolean,
    menuExpanded: Boolean,
    onMenuRequested: () -> Unit,
    onMenuDismissRequested: () -> Unit,
    modifier: Modifier = Modifier,
    abstractHeightDp: Dp? = null,
    resultHeightDp: Dp? = null,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(if (selected) AppColors.Primary.copy(alpha = 0.12f) else AppColors.SurfaceHighest)
            .border(
                width = if (selected) 1.5.dp else 0.dp,
                color = if (selected) AppColors.Primary else Color.Transparent,
                shape = RoundedCornerShape(20.dp),
            )
            .combinedClickable(onClick = onClick, onLongClick = onLongClick),
    ) {
        HistoryCardHeader(
            timestampMs = entry.timestampMs,
            selectionMode = selectionMode,
            selected = selected,
            menuExpanded = menuExpanded,
            onMenuRequested = onMenuRequested,
            onMenuDismissRequested = onMenuDismissRequested,
            onDelete = onDelete,
            onShare = onShare,
            onMail = onMail,
        )

        HistoryCardBody(
            entry = entry,
            abstractHeightDp = abstractHeightDp,
            resultHeightDp = resultHeightDp,
        )
    }
}

@Composable
private fun HistoryCardHeader(
    timestampMs: Long,
    selectionMode: Boolean,
    selected: Boolean,
    menuExpanded: Boolean,
    onMenuRequested: () -> Unit,
    onMenuDismissRequested: () -> Unit,
    onDelete: () -> Unit,
    onShare: () -> Unit,
    onMail: (() -> Unit)?,
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            HistoryCardTimestamp(timestampMs)

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                DeltaPBadge()

                if (selectionMode) {
                    SelectionIndicator(selected)
                } else {
                    HistoryCardMenu(
                        expanded = menuExpanded,
                        onMenuRequested = onMenuRequested,
                        onDismissRequested = onMenuDismissRequested,
                        onDelete = onDelete,
                        onShare = onShare,
                        onMail = onMail,
                    )
                }
            }
        }

        HorizontalDivider(color = AppColors.OutlineVar)
    }
}

@Composable
private fun HistoryCardTimestamp(timestampMs: Long) {
    val configuration = LocalConfiguration.current
    val locale = remember(configuration) {
        ConfigurationCompat.getLocales(configuration).get(0) ?: java.util.Locale.ROOT
    }
    val ts = remember(timestampMs, locale) {
        SimpleDateFormat("dd.MM.yyyy HH:mm", locale).format(Date(timestampMs))
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        Icon(
            imageVector = Icons.Outlined.AccessTime,
            contentDescription = null,
            tint = AppColors.OnSurfaceVar,
            modifier = Modifier.size(13.dp),
        )
        Text(
            text = ts,
            fontSize = 11.sp,
            color = AppColors.OnSurfaceVar,
        )
    }
}

@Composable
private fun DeltaPBadge() {
    Text(
        text = "ΔP",
        fontSize = 13.sp,
        fontStyle = FontStyle.Italic,
        fontWeight = FontWeight.Medium,
        color = AppColors.RVar,
        modifier = Modifier
            .clip(CircleShape)
            .background(AppColors.RVar.copy(alpha = 0.12f))
            .border(1.dp, AppColors.RVar.copy(alpha = 0.30f), CircleShape)
            .padding(horizontal = 10.dp, vertical = 3.dp),
    )
}

@Composable
private fun SelectionIndicator(selected: Boolean) {
    Icon(
        imageVector = if (selected) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
        contentDescription = if (selected) "Выбрано" else "Не выбрано",
        tint = if (selected) AppColors.Primary else AppColors.OnSurface.copy(alpha = 0.35f),
        modifier = Modifier.size(22.dp),
    )
}

@Composable
private fun HistoryCardMenu(
    expanded: Boolean,
    onMenuRequested: () -> Unit,
    onDismissRequested: () -> Unit,
    onDelete: () -> Unit,
    onShare: () -> Unit,
    onMail: (() -> Unit)?,
) {
    Box {
        IconButton(
            onClick = onMenuRequested,
            modifier = Modifier.size(28.dp),
        ) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "Действия",
                tint = AppColors.OnSurface.copy(alpha = 0.45f),
                modifier = Modifier.size(18.dp),
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = onDismissRequested,
            shape = RoundedCornerShape(16.dp),
        ) {
            DropdownMenuItem(
                text = { Text("Удалить") },
                leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) },
                onClick = {
                    onDismissRequested()
                    onDelete()
                },
            )
            DropdownMenuItem(
                text = { Text("Поделиться") },
                leadingIcon = { Icon(Icons.Default.Share, contentDescription = null) },
                onClick = {
                    onDismissRequested()
                    onShare()
                },
            )
            if (onMail != null) {
                DropdownMenuItem(
                    text = { Text("Отправить по почте") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                    onClick = {
                        onDismissRequested()
                        onMail()
                    },
                )
            }
        }
    }
}

@Composable
private fun HistoryCardBody(
    entry: HistoryEntry,
    abstractHeightDp: Dp?,
    resultHeightDp: Dp?,
) {
    if (entry.name != null) {
        Text(
            text = entry.name,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = AppColors.OnSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp)
                .padding(top = 8.dp),
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp)
            .padding(bottom = 16.dp, top = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        DeltaPAbstractFormula(fontSize = 14.sp, fixedHeight = abstractHeightDp)

        HorizontalDivider(
            color = AppColors.OutlineVar,
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp),
        )

        DeltaPFormulaWithValues(
            t = entry.t,
            pStart = entry.pStart,
            pEnd = entry.pEnd,
            pStartBar = entry.pStartBar,
            pEndBar = entry.pEndBar,
            tStartK = entry.tStartK,
            tEndK = entry.tEndK,
            result = entry.result,
            fontSize = 14.sp,
            fixedHeight = resultHeightDp,
        )

        HistoryComment(text = entry.comment)
    }
}

@Composable
private fun HistoryComment(text: String?) {
    if (text.isNullOrBlank()) return

    var expanded by remember { mutableStateOf(false) }
    var isTruncated by remember { mutableStateOf(false) }
    val chevronRotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        label = "commentChevron",
    )

    Column(modifier = Modifier.fillMaxWidth().padding(top = 12.dp)) {
        HorizontalDivider(
            color = AppColors.OutlineVar,
            modifier = Modifier.padding(bottom = 12.dp),
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (isTruncated) Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                    ) { expanded = !expanded } else Modifier
                )
                .animateContentSize(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = text,
                fontSize = 13.sp,
                lineHeight = 19.sp,
                color = AppColors.OnSurfaceVar,
                maxLines = if (expanded) Int.MAX_VALUE else 1,
                overflow = TextOverflow.Ellipsis,
                onTextLayout = { layout -> if (!expanded) isTruncated = layout.hasVisualOverflow },
                modifier = Modifier.weight(1f),
            )
            if (isTruncated) {
                Icon(
                    imageVector = Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = AppColors.OnSurfaceVar,
                    modifier = Modifier
                        .size(15.dp)
                        .rotate(chevronRotation),
                )
            }
        }
    }
}
