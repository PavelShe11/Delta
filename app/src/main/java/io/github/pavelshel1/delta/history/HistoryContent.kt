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
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListLayoutInfo
import androidx.compose.foundation.lazy.LazyListPrefetchScope
import androidx.compose.foundation.lazy.LazyListPrefetchStrategy
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.layout.LazyLayoutPrefetchState
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hrm.latex.renderer.measure.rememberLatexMeasurer
import com.hrm.latex.renderer.model.LatexConfig
import com.hrm.latex.renderer.model.LatexTheme
import io.github.pavelshel1.delta.formula.deltaAbstractLatex
import io.github.pavelshel1.delta.formula.deltaSubstitutedWithResultLatex
import io.github.pavelshel1.delta.ui.theme.AppColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// Кэш на время жизни процесса — при повторном открытии истории высоты уже известны
private val abstractHeightCache = mutableStateOf<Dp?>(null)
private val resultHeightCache = mutableStateMapOf<Long, Dp>()

// Предзагрузка ahead элементов в сторону скролла — даёт время Latex-у распарситься до появления
@OptIn(ExperimentalFoundationApi::class)
private class EagerPrefetchStrategy(private val ahead: Int = 4) :
    LazyListPrefetchStrategy by LazyListPrefetchStrategy() {

    private val handles = ArrayDeque<LazyLayoutPrefetchState.PrefetchHandle>()
    private var scrollForward = true

    override fun LazyListPrefetchScope.onScroll(delta: Float, layoutInfo: LazyListLayoutInfo) {
        if (layoutInfo.visibleItemsInfo.isEmpty()) return
        val forward = delta < 0
        if (forward != scrollForward) {
            handles.forEach { it.cancel() }
            handles.clear()
            scrollForward = forward
        }
        handles.forEach { it.cancel() }
        handles.clear()
        val base = if (forward) layoutInfo.visibleItemsInfo.last().index + 1
                   else layoutInfo.visibleItemsInfo.first().index - 1
        repeat(ahead) { i ->
            val idx = if (forward) base + i else base - i
            if (idx in 0 until layoutInfo.totalItemsCount) {
                handles.addLast(schedulePrefetch(idx))
            }
        }
    }
}

@Composable
fun HistoryContent(component: HistoryComponent, modifier: Modifier = Modifier) {
    val entries by component.entries.collectAsState()
    var showClearDialog by remember { mutableStateOf(false) }

    val preConfig = remember { LatexConfig(fontSize = 14.sp, theme = LatexTheme.dark()) }
    val measurer = rememberLatexMeasurer(preConfig)
    val density = LocalDensity.current

    // Замер константной абстрактной формулы — только один раз за время жизни процесса
    LaunchedEffect(measurer) {
        if (abstractHeightCache.value == null) {
            val localDensity = density
            val dims = withContext(Dispatchers.Default) { measurer.measure(deltaAbstractLatex, preConfig) }
            dims?.let { abstractHeightCache.value = with(localDensity) { it.heightPx.toDp() } }
        }
    }

    rememberLazyListState()

    // Замер формул результатов для новых записей
    LaunchedEffect(entries, measurer) {
        val localDensity = density
        entries.filter { it.id !in resultHeightCache }.forEach { entry ->
            val formula = deltaSubstitutedWithResultLatex(entry.t, entry.pStart, entry.pEnd, entry.tStartK, entry.tEndK, entry.result)
            val dims = withContext(Dispatchers.Default) { measurer.measure(formula, preConfig) }
            dims?.let { resultHeightCache[entry.id] = with(localDensity) { it.heightPx.toDp() } }
        }
    }

    val abstractHeightDp by abstractHeightCache

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
            @OptIn(ExperimentalFoundationApi::class)
            val listState = rememberLazyListState(
                prefetchStrategy = remember { EagerPrefetchStrategy(ahead = 4) }
            )
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                state = listState,
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(entries, key = { it.id }) { entry ->
                    val id = entry.id
                    val onDelete = remember(id) { { component.onDelete(id) } }
                    HistoryCard(
                        entry = entry,
                        onDelete = onDelete,
                        modifier = Modifier.animateItem(),
                        abstractHeightDp = abstractHeightDp,
                        resultHeightDp = resultHeightCache[entry.id],
                    )
                }
            }
        }
    }
}
