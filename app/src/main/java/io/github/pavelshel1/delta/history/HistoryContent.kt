package io.github.pavelshel1.delta.history

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
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
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.Context
import com.hrm.latex.renderer.measure.rememberLatexMeasurer
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.arkivanov.decompose.router.slot.ChildSlot
import com.hrm.latex.renderer.model.LatexConfig
import com.hrm.latex.renderer.model.LatexTheme
import dev.chrisbanes.haze.HazeDefaults
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import io.github.pavelshel1.delta.formula.deltaAbstractLatex
import io.github.pavelshel1.delta.formula.deltaSubstitutedWithResultLatex
import io.github.pavelshel1.delta.mail.MailDialogContent
import io.github.pavelshel1.delta.ui.theme.AppColors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val abstractHeightCache = mutableStateOf<Dp?>(null)
private val resultHeightCache = mutableStateMapOf<Long, Dp>()

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
    val state by component.state.subscribeAsState()
    val entries = state.entries
    val selectedIds = state.selectedIds
    val isSelectionMode = state.isSelectionMode
    val entryMenuSlot by component.entryMenu.subscribeAsState()
    val deleteConfirmSlot by component.deleteConfirm.subscribeAsState()
    val mailDialogSlot by component.mailDialog.subscribeAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val preConfig = remember { LatexConfig(fontSize = 14.sp, theme = LatexTheme.dark()) }
    val measurer = rememberLatexMeasurer(preConfig)
    val density = LocalDensity.current

    LaunchedEffect(measurer) {
        if (abstractHeightCache.value == null) {
            val localDensity = density
            val dims =
                withContext(Dispatchers.Default) { measurer.measure(deltaAbstractLatex, preConfig) }
            dims?.let { abstractHeightCache.value = with(localDensity) { it.heightPx.toDp() } }
        }
    }

    LaunchedEffect(entries, measurer) {
        val localDensity = density
        entries.filter { it.id !in resultHeightCache }.forEach { entry ->
            val formula = deltaSubstitutedWithResultLatex(
                entry.t,
                entry.pStart,
                entry.pEnd,
                entry.pStartBar,
                entry.pEndBar,
                entry.tStartK,
                entry.tEndK,
                entry.result
            )
            val dims = withContext(Dispatchers.Default) { measurer.measure(formula, preConfig) }
            dims?.let { resultHeightCache[entry.id] = with(localDensity) { it.heightPx.toDp() } }
        }
    }

    val abstractHeightDp by abstractHeightCache

    val statusBarTopPx = WindowInsets.statusBars.getTop(density)
    var toolbarHeightPx by remember {
        mutableIntStateOf(statusBarTopPx + with(density) { 57.dp.roundToPx() })
    }
    val toolbarHeightDp = with(density) { toolbarHeightPx.toDp() }
    val navBarBottomDp = with(density) { WindowInsets.navigationBars.getBottom(density).toDp() }

    val hazeState = rememberHazeState()

    deleteConfirmSlot.child?.instance?.let { DeleteConfirmContent(it) }
    mailDialogSlot.child?.instance?.let { MailDialogContent(it) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AppColors.Background)
    ) {
        if (entries.isEmpty()) {
            HistoryEmptyState(toolbarHeightDp)
        } else {
            HistoryEntryList(
                component = component,
                entries = entries,
                selectedIds = selectedIds,
                isSelectionMode = isSelectionMode,
                entryMenuSlot = entryMenuSlot,
                abstractHeightDp = abstractHeightDp,
                toolbarHeightDp = toolbarHeightDp,
                navBarBottomDp = navBarBottomDp,
                hazeState = hazeState,
                context = context,
                coroutineScope = coroutineScope,
            )
        }

        HistoryToolbar(
            entries = entries,
            selectedIds = selectedIds,
            isSelectionMode = isSelectionMode,
            onDismissRequested = component.onDismissRequested,
            onSelectionCleared = component::onSelectionCleared,
            onSelectAllToggled = component::onSelectAllToggled,
            onDeleteSelectedRequested = component::onDeleteSelectedRequested,
            onMailSelectedRequested = if (component.isMailEnabled) component::onMailSelectedRequested else null,
            hazeState = hazeState,
            context = context,
            coroutineScope = coroutineScope,
            onHeightChanged = { toolbarHeightPx = it },
        )
    }
}

@Composable
private fun HistoryEmptyState(toolbarHeightDp: Dp, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(top = toolbarHeightDp)
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
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HistoryEntryList(
    component: HistoryComponent,
    entries: List<HistoryEntry>,
    selectedIds: List<Long>,
    isSelectionMode: Boolean,
    entryMenuSlot: ChildSlot<*, EntryMenuComponent>,
    abstractHeightDp: Dp?,
    toolbarHeightDp: Dp,
    navBarBottomDp: Dp,
    hazeState: HazeState,
    context: Context,
    coroutineScope: CoroutineScope,
) {
    val listState = rememberLazyListState(
        prefetchStrategy = remember { EagerPrefetchStrategy(ahead = 4) }
    )
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .hazeSource(state = hazeState),
        state = listState,
        contentPadding = PaddingValues(
            start = 14.dp,
            end = 14.dp,
            top = toolbarHeightDp + 10.dp,
            bottom = 10.dp + navBarBottomDp,
        ),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        items(entries, key = { it.id }) { entry ->
            val id = entry.id
            val onDelete = remember(id) { { component.onDeleteRequested(id) } }
            val onTap = remember(entry) { { component.onEntryTapped(entry) } }
            val onLongPress = remember(entry) { { component.onEntryLongPressed(entry) } }
            val onShare = remember(entry) {
                {
                    coroutineScope.launch { shareHistoryAsExcel(context, listOf(entry)) }
                    Unit
                }
            }
            val onMail = remember(entry) { { component.onMailRequested(entry) } }
            val onMenuRequested = remember(entry) { { component.onEntryMenuRequested(entry) } }
            val onMenuDismissRequested = remember(entryMenuSlot) {
                {
                    entryMenuSlot.child?.instance?.onDismissRequested?.invoke()
                    Unit
                }
            }
            HistoryCard(
                entry = entry,
                onDelete = onDelete,
                onShare = onShare,
                onMail = if (component.isMailEnabled) onMail else null,
                onClick = onTap,
                onLongClick = onLongPress,
                selectionMode = isSelectionMode,
                selected = id in selectedIds,
                menuExpanded = entryMenuSlot.child?.instance?.entry?.id == id,
                onMenuRequested = onMenuRequested,
                onMenuDismissRequested = onMenuDismissRequested,
                modifier = Modifier.animateItem(),
                abstractHeightDp = abstractHeightDp,
                resultHeightDp = resultHeightCache[entry.id],
            )
        }
    }
}

@Composable
private fun HistoryToolbar(
    entries: List<HistoryEntry>,
    selectedIds: List<Long>,
    isSelectionMode: Boolean,
    onDismissRequested: () -> Unit,
    onSelectionCleared: () -> Unit,
    onSelectAllToggled: () -> Unit,
    onDeleteSelectedRequested: () -> Unit,
    onMailSelectedRequested: (() -> Unit)?,
    hazeState: HazeState,
    context: Context,
    coroutineScope: CoroutineScope,
    onHeightChanged: (Int) -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .hazeEffect(
                state = hazeState,
                style = HazeStyle(
                    backgroundColor = AppColors.Background,
                    tint = HazeDefaults.tint(AppColors.Background),
                    fallbackTint = HazeTint(AppColors.Background),
                ),
            )
            .onSizeChanged { onHeightChanged(it.height) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .background(Color.Transparent),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(
                    onClick = if (isSelectionMode) onSelectionCleared else onDismissRequested,
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = if (isSelectionMode) "Отменить выделение" else "Закрыть",
                        tint = AppColors.Primary,
                    )
                }
                // Счётчик — targetState, чтобы уходящий заголовок показывал своё число, а не 0;
                // contentKey анимирует только смену режима, а не каждое изменение счётчика.
                // using null: ширину задаёт weight, анимация размера отставала бы от неё и сдвигала текст.
                AnimatedContent(
                    targetState = selectedIds.size,
                    contentKey = { it > 0 },
                    transitionSpec = { fadeIn() togetherWith fadeOut() using null },
                    modifier = Modifier.weight(1f),
                    label = "HistoryToolbarTitle",
                ) { count ->
                    // softWrap = false: пока иконки выделения уходят, заголовок обрезается, а не переносится
                    Text(
                        text = if (count > 0) "Выбрано: $count" else "История расчётов",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Normal,
                        color = AppColors.OnSurface,
                        maxLines = 1,
                        softWrap = false,
                    )
                }
                AnimatedVisibility(
                    visible = isSelectionMode,
                    enter = fadeIn() + expandHorizontally(),
                    exit = fadeOut() + shrinkHorizontally(),
                ) {
                    Row {
                        IconButton(onClick = onSelectAllToggled) {
                            Icon(
                                imageVector = Icons.Default.SelectAll,
                                contentDescription = "Выбрать всё",
                                tint = AppColors.OnSurfaceVar,
                            )
                        }
                        IconButton(onClick = {
                            val selected = entries.filter { it.id in selectedIds }
                            coroutineScope.launch { shareHistoryAsExcel(context, selected) }
                        }) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Поделиться выбранным",
                                tint = AppColors.OnSurfaceVar,
                            )
                        }
                        if (onMailSelectedRequested != null) {
                            IconButton(onClick = onMailSelectedRequested) {
                                Icon(
                                    imageVector = Icons.Default.Email,
                                    contentDescription = "Отправить выбранное по почте",
                                    tint = AppColors.OnSurfaceVar,
                                )
                            }
                        }
                        IconButton(onClick = onDeleteSelectedRequested) {
                            Icon(
                                imageVector = Icons.Default.DeleteSweep,
                                contentDescription = "Удалить выбранное",
                                tint = AppColors.OnSurfaceVar,
                            )
                        }
                    }
                }
            }
            HorizontalDivider(color = AppColors.OutlineVar)
        }
    }
}
