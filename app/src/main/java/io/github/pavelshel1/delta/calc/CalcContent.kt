package io.github.pavelshel1.delta.calc

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.SubdirectoryArrowRight
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import dev.chrisbanes.haze.HazeDefaults
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import io.github.pavelshel1.delta.R
import io.github.pavelshel1.delta.about.AboutSheetContent
import io.github.pavelshel1.delta.about.AppInfo
import io.github.pavelshel1.delta.formula.DeltaPAbstractFormula
import io.github.pavelshel1.delta.formula.DeltaPFormulaWithValues
import io.github.pavelshel1.delta.ui.theme.AppColors
import io.github.pavelshel1.delta.unitsheet.FieldKey
import io.github.pavelshel1.delta.unitsheet.UnitSheetContent
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.math.BigDecimal
import kotlin.time.Duration.Companion.milliseconds


private val LocalHazeState = compositionLocalOf { HazeState() }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalcContent(component: CalcComponent, historyCount: Int = 0, modifier: Modifier = Modifier) {
    val state by component.state.subscribeAsState()
    val pUnitLabel = FieldKey.PStart.units[state.pUnitIdx]
    val focusManager = LocalFocusManager.current
    var pendingHistoryOpen by remember { mutableStateOf(false) }
    var showAbout by remember { mutableStateOf(false) }
    val appName = stringResource(R.string.app_name)
    val appInfo = remember(appName) { AppInfo.fromBuildConfig(appName) }

    val filledCount = state.filledCount

    val result = state.result
    val lastResult = remember { mutableStateOf<BigDecimal?>(null) }
    if (result != null) lastResult.value = result

    var justSaved by remember { mutableStateOf(false) }
    LaunchedEffect(state) { justSaved = false }

    val listState = rememberLazyListState()
    val density = LocalDensity.current
    val imeVisible = WindowInsets.ime.getBottom(density) > 0

    LaunchedEffect(pendingHistoryOpen, imeVisible) {
        if (pendingHistoryOpen && !imeVisible) {
            pendingHistoryOpen = false
            component.onHistoryRequested()
        }
    }

    var pendingUnitKey by remember { mutableStateOf<FieldKey?>(null) }

    fun onPressureUnitTapped(key: FieldKey) {
        focusManager.clearFocus()
        if (imeVisible) pendingUnitKey = key
        else component.onUnitChipTapped(key)
    }

    val focusTStart = remember { FocusRequester() }
    val focusTEnd = remember { FocusRequester() }
    val focusPStart = remember { FocusRequester() }
    val focusPStartBar = remember { FocusRequester() }
    val focusPEnd = remember { FocusRequester() }
    val focusPEndBar = remember { FocusRequester() }
    val focusTime = remember { FocusRequester() }

    LaunchedEffect(pendingUnitKey, imeVisible) {
        val key = pendingUnitKey ?: return@LaunchedEffect
        if (!imeVisible) {
            pendingUnitKey = null
            component.onUnitChipTapped(key)
        }
    }

    val floatingBottomPadding = 16.dp
    val floatingBottomPaddingPx = with(density) { floatingBottomPadding.toPx() }
    var floatingHeightPx by remember { mutableIntStateOf(0) }

    val hasResult = result != null
    val showFloating by remember(hasResult) {
        derivedStateOf {
            if (!hasResult) return@derivedStateOf false
            val info = listState.layoutInfo
            val item = info.visibleItemsInfo.firstOrNull { it.key == "result_block" }
                ?: return@derivedStateOf true
            val itemBottom = item.offset + item.size
            val threshold = info.viewportEndOffset - floatingBottomPaddingPx
            itemBottom > threshold
        }
    }

    val hazeState = rememberHazeState()

    CompositionLocalProvider(LocalHazeState provides hazeState) {
        Box(modifier = modifier.fillMaxSize()) {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                containerColor = AppColors.Background,
                contentWindowInsets = WindowInsets.systemBars,
                topBar = {
                    Column(
                        modifier = Modifier.hazeEffect(
                            state = hazeState,
                            style = HazeDefaults.style(backgroundColor = AppColors.Background)
                        )
                    ) {
                        TopAppBar(
                            title = {
                                Column {
                                    Text(
                                        text = "Испытание на герметичность",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Normal,
                                        color = AppColors.OnSurface,
                                    )
                                    Text(
                                        text = "Перепад давления · ΔP",
                                        fontSize = 11.sp,
                                        color = AppColors.OnSurfaceVar,
                                    )
                                }
                            },
                            actions = {
                                if (historyCount > 0) {
                                    IconButton(onClick = {
                                        focusManager.clearFocus()
                                        if (imeVisible) {
                                            pendingHistoryOpen = true
                                        } else {
                                            component.onHistoryRequested()
                                        }
                                    }) {
                                        Icon(
                                            imageVector = Icons.Default.History,
                                            contentDescription = "История",
                                            tint = AppColors.OnSurface,
                                        )
                                    }
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                        )
                        HorizontalDivider(color = AppColors.OutlineVar)
                        ProgressBar(filled = filledCount, total = 7)
                        HorizontalDivider(color = AppColors.OutlineVar)
                    }
                },
            ) { innerPadding ->
                Box(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .hazeSource(state = hazeState),
                        contentPadding = PaddingValues(
                            top = innerPadding.calculateTopPadding(),
                            bottom = innerPadding.calculateBottomPadding() + 12.dp,
                            start = 14.dp,
                            end = 14.dp
                        )
                    ) {
                        item {
                            Spacer(Modifier.height(10.dp))
                            InputCard(
                                varMain = "T",
                                varSub = "нач",
                                description = "температура в начале",
                                value = state.tStartText.toDisplayString(),
                                unitLabel = FieldKey.TStart.units[state.tUnitIdx],
                                hasUnitDropdown = true,
                                onValueChange = component::onTStartChanged,
                                onUnitTapped = {
                                    focusManager.clearFocus()
                                    if (imeVisible) pendingUnitKey = FieldKey.TStart
                                    else component.onUnitChipTapped(FieldKey.TStart)
                                },
                                kelvinHint = if (state.tUnitIdx == 0) state.tStartKelvin.toDisplayString()
                                    .ifEmpty { null } else null,
                                imeAction = ImeAction.Next,
                                onImeAction = { focusTEnd.requestFocus() },
                                modifier = Modifier
                                    .animateItem()
                                    .focusRequester(focusTStart),
                            )
                        }

                        item {
                            Spacer(Modifier.height(10.dp))
                            InputCard(
                                varMain = "T",
                                varSub = "кон",
                                description = "температура в конце",
                                value = state.tEndText.toDisplayString(),
                                unitLabel = FieldKey.TEnd.units[state.tUnitIdx],
                                hasUnitDropdown = true,
                                onValueChange = component::onTEndChanged,
                                onUnitTapped = {
                                    focusManager.clearFocus()
                                    if (imeVisible) pendingUnitKey = FieldKey.TEnd
                                    else component.onUnitChipTapped(FieldKey.TEnd)
                                },
                                kelvinHint = if (state.tUnitIdx == 0) state.tEndKelvin.toDisplayString()
                                    .ifEmpty { null } else null,
                                imeAction = ImeAction.Next,
                                onImeAction = { focusPStart.requestFocus() },
                                modifier = Modifier
                                    .animateItem()
                                    .focusRequester(focusTEnd),
                            )
                        }

                        item {
                            Spacer(Modifier.height(10.dp))
                            InputCard(
                                varMain = "P", varSub = "нач",
                                description = "давление в начале",
                                value = state.pStart.toDisplayString(),
                                unitLabel = pUnitLabel,
                                hasUnitDropdown = true,
                                onValueChange = component::onPStartChanged,
                                onUnitTapped = { onPressureUnitTapped(FieldKey.PStart) },
                                imeAction = ImeAction.Next,
                                onImeAction = { focusPStartBar.requestFocus() },
                                modifier = Modifier
                                    .animateItem()
                                    .focusRequester(focusPStart),
                            )
                        }

                        item {
                            Spacer(Modifier.height(10.dp))
                            InputCard(
                                varMain = "P", varSub = "б.нач",
                                description = "давление барометрическое в начале",
                                value = state.pStartBar.toDisplayString(),
                                unitLabel = pUnitLabel,
                                hasUnitDropdown = true,
                                onValueChange = component::onPStartBarChanged,
                                onUnitTapped = { onPressureUnitTapped(FieldKey.PStartBar) },
                                imeAction = ImeAction.Next,
                                onImeAction = { focusPEnd.requestFocus() },
                                modifier = Modifier
                                    .animateItem()
                                    .focusRequester(focusPStartBar),
                            )
                        }

                        item {
                            Spacer(Modifier.height(10.dp))
                            InputCard(
                                varMain = "P", varSub = "кон",
                                description = "давление в конце",
                                value = state.pEnd.toDisplayString(),
                                unitLabel = pUnitLabel,
                                hasUnitDropdown = true,
                                onValueChange = component::onPEndChanged,
                                onUnitTapped = { onPressureUnitTapped(FieldKey.PEnd) },
                                imeAction = ImeAction.Next,
                                onImeAction = { focusPEndBar.requestFocus() },
                                modifier = Modifier
                                    .animateItem()
                                    .focusRequester(focusPEnd),
                            )
                        }

                        item {
                            Spacer(Modifier.height(10.dp))
                            InputCard(
                                varMain = "P", varSub = "б.кон",
                                description = "давление барометрическое в конце",
                                value = state.pEndBar.toDisplayString(),
                                unitLabel = pUnitLabel,
                                hasUnitDropdown = true,
                                onValueChange = component::onPEndBarChanged,
                                onUnitTapped = { onPressureUnitTapped(FieldKey.PEndBar) },
                                imeAction = ImeAction.Next,
                                onImeAction = { focusTime.requestFocus() },
                                modifier = Modifier
                                    .animateItem()
                                    .focusRequester(focusPEndBar),
                            )
                        }

                        item {
                            Spacer(Modifier.height(10.dp))
                            InputCard(
                                varMain = "t",
                                varSub = null,
                                description = "время испытания",
                                value = state.time.toDisplayString(),
                                unitLabel = "ч",
                                hasUnitDropdown = false,
                                onValueChange = component::onTimeChanged,
                                onUnitTapped = {},
                                imeAction = ImeAction.Done,
                                onImeAction = { focusManager.clearFocus() },
                                modifier = Modifier
                                    .animateItem()
                                    .focusRequester(focusTime),
                            )
                        }

                        item(key = "result_block") {
                            AnimatedVisibility(
                                visible = hasResult,
                                enter = fadeIn(tween(250)) + expandVertically(tween(300)),
                                exit = fadeOut(tween(200)) + shrinkVertically(tween(200)),
                            ) {
                                Column(modifier = Modifier.alpha(if (showFloating) 0f else 1f)) {
                                    Spacer(Modifier.height(10.dp))
                                    ResultBlock(
                                        result = lastResult.value?.toDouble() ?: 0.0,
                                        justSaved = justSaved,
                                        onSave = {
                                            component.onSaveRequested(
                                                lastResult.value?.toDouble() ?: 0.0
                                            )
                                            justSaved = true
                                        },
                                    )
                                }
                            }
                        }

                        item {
                            Spacer(Modifier.height(10.dp))
                            FormulaCard(state = state, modifier = Modifier.animateItem())
                        }

                        item {
                            Spacer(Modifier.height(8.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null,
                                    ) { showAbout = true }
                                    .padding(vertical = 16.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = "by PavelShe11",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.5.sp,
                                    color = AppColors.OnSurface.copy(alpha = 0.12f),
                                )
                            }
                        }
                    }

                    val appearProgress by animateFloatAsState(
                        targetValue = if (hasResult) 1f else 0f,
                        animationSpec = tween(durationMillis = 300),
                        label = "floatingAppear",
                    )
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(horizontal = 14.dp, vertical = floatingBottomPadding)
                            .onSizeChanged { floatingHeightPx = it.height }
                            .graphicsLayer {
                                alpha = if (showFloating) appearProgress else 0f
                                translationY = (1f - appearProgress) * size.height
                            },
                    ) {
                        ResultBlock(
                            result = lastResult.value?.toDouble() ?: 0.0,
                            hazeState = hazeState,
                            justSaved = justSaved,
                            onSave = {
                                component.onSaveRequested(lastResult.value?.toDouble() ?: 0.0)
                                justSaved = true
                            },
                        )
                    }
                }
            }

            val slot by component.unitSheet.subscribeAsState()
            slot.child?.instance?.let { UnitSheetContent(it) }
            if (showAbout) {
                AboutSheetContent(
                    appInfo = appInfo,
                    onDismiss = { showAbout = false },
                )
            }
        }
    }
}

@Composable
private fun ProgressBar(filled: Int, total: Int) {
    val fraction = filled.toFloat() / total
    val animatedFraction by animateFloatAsState(
        targetValue = fraction,
        animationSpec = tween(durationMillis = 400),
    )
    val done = filled == total

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = if (done) "✓ Все значения введены" else "Заполнено $filled из $total",
                fontSize = 11.sp,
                color = if (done) AppColors.Primary else AppColors.OnSurfaceVar,
            )
            Text(
                text = "${(fraction * 100).toInt()}%",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = if (done) AppColors.Primary else AppColors.Outline,
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(AppColors.OutlineVar),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedFraction)
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(AppColors.Primary),
            )
        }
    }
}

@Composable
private fun InputCard(
    varMain: String,
    varSub: String?,
    description: String,
    value: String,
    unitLabel: String,
    hasUnitDropdown: Boolean,
    onValueChange: (String) -> Unit,
    onUnitTapped: () -> Unit,
    modifier: Modifier = Modifier,
    kelvinHint: String? = null,
    imeAction: ImeAction = ImeAction.Next,
    onImeAction: () -> Unit = {},
) {
    var isFocused by remember { mutableStateOf(false) }
    var tfValue by remember { mutableStateOf(TextFieldValue(value)) }
    var textLayout by remember { mutableStateOf<TextLayoutResult?>(null) }
    val scrollState = rememberScrollState()
    var boxWidthPx by remember { mutableIntStateOf(0) }

    LaunchedEffect(value) {
        val incomingBD = value.toBigDecimalOrNull()
        val localBD = tfValue.text.toBigDecimalOrNull()
        if (incomingBD != localBD) {
            tfValue = TextFieldValue(value, TextRange(value.length))
        }
    }

    LaunchedEffect(tfValue.selection, textLayout) {
        val layout = textLayout ?: return@LaunchedEffect
        if (tfValue.text.isEmpty()) return@LaunchedEffect
        val cursor = tfValue.selection.end.coerceIn(0, tfValue.text.length)
        val cursorX = layout.getCursorRect(cursor).left.toInt()
        val visStart = scrollState.value
        val visEnd = visStart + boxWidthPx
        val pad = 32
        when {
            cursorX < visStart + pad -> scrollState.animateScrollTo((cursorX - pad).coerceAtLeast(0))
            cursorX > visEnd - pad   -> scrollState.animateScrollTo((cursorX - boxWidthPx + pad).coerceAtLeast(0))
        }
    }

    val borderColor by animateColorAsState(
        targetValue = if (isFocused) AppColors.Primary else AppColors.OutlineVar,
        animationSpec = tween(durationMillis = 180),
        label = "border",
    )
    val glowElevation by animateDpAsState(
        targetValue = if (isFocused) 8.dp else 0.dp,
        animationSpec = tween(durationMillis = 180),
        label = "glow",
    )

    BasicTextField(
        value = tfValue,
        onValueChange = { v ->
            if (v.text.isEmpty() || v.text.matches(Regex("-?\\d*\\.?\\d*"))) {
                tfValue = v
                onValueChange(v.text)
            }
        },
        modifier = modifier
            .fillMaxWidth()
            .onFocusChanged { isFocused = it.isFocused },
        textStyle = TextStyle(
            fontSize = 42.sp,
            lineHeight = 42.sp,
            fontWeight = if (value.isNotEmpty()) FontWeight.Normal else FontWeight.Light,
            color = if (value.isNotEmpty()) AppColors.OnSurface else AppColors.Outline,
        ),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Decimal,
            imeAction = imeAction
        ),
        keyboardActions = KeyboardActions(onAny = { onImeAction() }),
        singleLine = true,
        cursorBrush = SolidColor(AppColors.Primary),
        decorationBox = { innerTextField ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = glowElevation,
                        shape = RoundedCornerShape(18.dp),
                        clip = false,
                        ambientColor = AppColors.ActiveGlow,
                        spotColor = AppColors.ActiveGlow,
                    )
                    .clip(RoundedCornerShape(18.dp))
                    .background(AppColors.Surface)
                    .border(2.dp, borderColor, RoundedCornerShape(18.dp))
                    .padding(horizontal = 14.dp),
            ) {
                Row(
                    modifier = Modifier.padding(top = 10.dp, bottom = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    VarChip(main = varMain, sub = varSub)
                    Text(
                        text = description,
                        fontSize = 12.sp,
                        color = AppColors.OnSurfaceVar,
                    )
                }
                Row(
                    modifier = Modifier.padding(bottom = if (kelvinHint != null) 4.dp else 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        if (value.isEmpty()) {
                            Text(
                                text = "—",
                                fontSize = 42.sp,
                                lineHeight = 42.sp,
                                fontWeight = FontWeight.Light,
                                color = AppColors.Outline,
                            )
                        }
                        innerTextField()
                    }
                    UnitChip(
                        label = unitLabel,
                        hasDropdown = hasUnitDropdown,
                        isFocused = isFocused,
                        onClick = onUnitTapped,
                    )
                }
                if (kelvinHint != null) {
                    Row(
                        modifier = Modifier.padding(bottom = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.SubdirectoryArrowRight,
                            contentDescription = null,
                            modifier = Modifier.size(11.dp),
                            tint = AppColors.Primary.copy(alpha = 0.75f),
                        )
                        Text(
                            text = buildAnnotatedString {
                                pushStyle(
                                    SpanStyle(
                                        color = AppColors.Primary.copy(alpha = 0.75f),
                                        letterSpacing = 0.2.sp
                                    )
                                )
                                append("в формулу подставится: ")
                                pop()
                                pushStyle(
                                    SpanStyle(
                                        color = AppColors.Primary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                                append("$kelvinHint К")
                                pop()
                            },
                            fontSize = 12.sp,
                        )
                    }
                }
            }
        },
    )
}

@Composable
private fun VarChip(main: String, sub: String?) {
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

@Composable
private fun UnitChip(label: String, hasDropdown: Boolean, isFocused: Boolean, onClick: () -> Unit) {
    var isFlashing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val bgColor by animateColorAsState(
        targetValue = when {
            isFlashing -> AppColors.Primary.copy(alpha = 0.22f)
            isFocused -> AppColors.Primary.copy(alpha = 0.09f)
            else -> AppColors.SurfaceHighest
        },
        animationSpec = tween(150),
        label = "unitBg",
    )
    val borderColor by animateColorAsState(
        targetValue = if (isFlashing || isFocused) AppColors.Primary.copy(alpha = 0.45f) else AppColors.OutlineVar,
        animationSpec = tween(150),
        label = "unitBorder",
    )
    val contentColor by animateColorAsState(
        targetValue = if (isFlashing || isFocused) AppColors.Primary else AppColors.OnSurface,
        animationSpec = tween(150),
        label = "unitText",
    )
    val chevronColor by animateColorAsState(
        targetValue = if (isFlashing || isFocused) AppColors.Primary else AppColors.OnSurfaceVar,
        animationSpec = tween(150),
        label = "chevronColor",
    )
    val chevronRotation by animateFloatAsState(
        targetValue = if (isFlashing) 180f else 0f,
        animationSpec = tween(200),
        label = "chevronRot",
    )

    Row(
        modifier = Modifier
            .clip(CircleShape)
            .background(bgColor)
            .border(1.dp, borderColor, CircleShape)
            .then(
                if (hasDropdown) Modifier.clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                ) {
                    scope.launch {
                        isFlashing = true
                        delay(300.milliseconds)
                        isFlashing = false
                    }
                    onClick()
                } else Modifier
            )
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = contentColor,
        )
        if (hasDropdown) {
            Icon(
                imageVector = Icons.Default.ExpandMore,
                contentDescription = null,
                modifier = Modifier
                    .size(13.dp)
                    .rotate(chevronRotation),
                tint = chevronColor,
            )
        }
    }
}

@Composable
private fun ResultBlock(
    result: Double,
    onSave: () -> Unit,
    justSaved: Boolean,
    hazeState: HazeState? = null,
) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (hazeState != null) Modifier.hazeEffect(
                        state = hazeState,
                        style = HazeDefaults.style(
                            backgroundColor = AppColors.ResultGradientStart,
                            tint = HazeTint(
                                Brush.linearGradient(
                                    listOf(
                                        AppColors.ResultGradientStart.copy(0.7f),
                                        AppColors.ResultGradientEnd.copy(0.7f)
                                    )
                                ),
                                blendMode = BlendMode.SrcOver
                            )
                        )
                    ) else Modifier.background(
                        Brush.linearGradient(
                            listOf(AppColors.ResultGradientStart, AppColors.ResultGradientEnd)
                        )
                    )
                )
                .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "ПАДЕНИЕ ДАВЛЕНИЯ",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.2.sp,
                    color = AppColors.Primary.copy(alpha = 0.55f),
                )
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = "%.3f".format(result),
                        fontSize = 44.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 44.sp,
                        color = AppColors.Primary,
                    )
                    Text(
                        text = "% / ч",
                        fontSize = 17.sp,
                        color = AppColors.Primary.copy(alpha = 0.7f),
                        modifier = Modifier.padding(bottom = 6.dp),
                    )
                }
            }
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    text = "ΔP",
                    fontSize = 15.sp,
                    fontStyle = FontStyle.Italic,
                    fontWeight = FontWeight.SemiBold,
                    color = AppColors.Primary,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(AppColors.Primary.copy(alpha = 0.12f))
                        .border(1.5.dp, AppColors.Primary.copy(alpha = 0.3f), CircleShape)
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                )
                IconButton(
                    onClick = onSave,
                    modifier = Modifier.size(28.dp),
                ) {
                    Icon(
                        imageVector = if (justSaved) Icons.Default.Check else Icons.Outlined.BookmarkBorder,
                        contentDescription = if (justSaved) "Сохранено" else "Сохранить",
                        tint = if (justSaved) AppColors.Primary else AppColors.Primary.copy(alpha = 0.55f),
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun FormulaCard(state: CalcState, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(AppColors.SurfaceHighest),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(7.dp),
            ) {
                Canvas(modifier = Modifier.size(14.dp)) {
                    val sw = 2f / 24f * size.width
                    fun x(v: Float) = v / 24f * size.width
                    fun y(v: Float) = v / 24f * size.height
                    drawLine(
                        AppColors.OnSurfaceVar,
                        Offset(x(4f), y(7f)),
                        Offset(x(20f), y(7f)),
                        strokeWidth = sw,
                        cap = StrokeCap.Round
                    )
                    drawLine(
                        AppColors.OnSurfaceVar,
                        Offset(x(4f), y(12f)),
                        Offset(x(14f), y(12f)),
                        strokeWidth = sw,
                        cap = StrokeCap.Round
                    )
                    drawLine(
                        AppColors.OnSurfaceVar,
                        Offset(x(4f), y(17f)),
                        Offset(x(17f), y(17f)),
                        strokeWidth = sw,
                        cap = StrokeCap.Round
                    )
                }
                Text(
                    text = "ФОРМУЛА РАСЧЁТА",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.1.sp,
                    color = AppColors.OnSurfaceVar,
                )
            }
            Text(
                text = "ΔP",
                fontSize = 15.sp,
                fontStyle = FontStyle.Italic,
                fontWeight = FontWeight.SemiBold,
                color = AppColors.RVar,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(AppColors.RVar.copy(alpha = 0.12f))
                    .border(1.5.dp, AppColors.RVar.copy(alpha = 0.30f), CircleShape)
                    .padding(horizontal = 14.dp, vertical = 6.dp),
            )
        }

        HorizontalDivider(color = AppColors.OutlineVar)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 14.dp, end = 14.dp, top = 18.dp, bottom = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(0.dp),
        ) {
            DeltaPAbstractFormula()

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 14.dp)
                    .height(1.dp)
                    .background(
                        Brush.horizontalGradient(
                            0f to Color.Transparent,
                            0.2f to AppColors.OutlineVar,
                            0.8f to AppColors.OutlineVar,
                            1f to Color.Transparent,
                        )
                    ),
            )

            DeltaPFormulaWithValues(
                t = state.time.toDisplayString(),
                pStart = state.pStart.toDisplayString(),
                pEnd = state.pEnd.toDisplayString(),
                pStartBar = state.pStartBar.toDisplayString(),
                pEndBar = state.pEndBar.toDisplayString(),
                tStartK = state.tStartKelvin.toDisplayString(),
                tEndK = state.tEndKelvin.toDisplayString(),
                result = state.result?.toDouble(),
            )
        }
    }
}
