package io.github.pavelshel1.delta.calc

import android.R
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.SubdirectoryArrowRight
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
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalDensity
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import dev.chrisbanes.haze.HazeDefaults
import dev.chrisbanes.haze.HazeEffectScope
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.LocalHazeStyle
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import io.github.pavelshel1.delta.formula.DeltaPAbstractFormula
import io.github.pavelshel1.delta.formula.DeltaPFormulaWithValues
import io.github.pavelshel1.delta.ui.theme.AppColors
import io.github.pavelshel1.delta.unitsheet.FieldKey
import io.github.pavelshel1.delta.unitsheet.UnitSheetContent
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

private val TEMP_UNIT_LABELS = listOf("К", "°C")

private fun calcDeltaP(state: CalcState): Double? {
    val t = state.timeText.toDoubleOrNull() ?: return null
    val pN = state.pStartText.toDoubleOrNull() ?: return null
    val pK = state.pEndText.toDoubleOrNull() ?: return null
    val tN = state.tStartText.toDoubleOrNull() ?: return null
    val tK = state.tEndText.toDoubleOrNull() ?: return null
    if (t == 0.0 || pN == 0.0 || tK == 0.0) return null
    val tNK = if (state.tStartUnitIdx == 1) tN + 273.0 else tN
    val tKK = if (state.tEndUnitIdx == 1) tK + 273.0 else tK
    return 100.0 / t * (1.0 - pK * tNK / (pN * tKK))
}

private fun toKelvin(s: String, isCelsius: Boolean): String {
    if (s.isEmpty()) return ""
    val num = s.toDoubleOrNull() ?: return s
    return if (isCelsius) (num + 273.0).toBigDecimal().stripTrailingZeros().toPlainString() else s
}

private val LocalHazeState = compositionLocalOf { HazeState() }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalcContent(component: CalcComponent, historyCount: Int = 0, modifier: Modifier = Modifier) {
    val state by component.state.subscribeAsState()
    val focusManager = LocalFocusManager.current
    var pendingHistoryOpen by remember { mutableStateOf(false) }

    val filledCount = listOf(
        state.tStartText, state.tEndText,
        state.pStartText, state.pEndText, state.timeText,
    ).count { it.isNotEmpty() }

    val result = calcDeltaP(state)
    val lastResult = remember { mutableStateOf<Double?>(null) }
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

    val focusTStart = remember { FocusRequester() }
    val focusTEnd = remember { FocusRequester() }
    val focusPStart = remember { FocusRequester() }
    val focusPEnd = remember { FocusRequester() }
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
                        ProgressBar(filled = filledCount, total = 5)
                        HorizontalDivider(color = AppColors.OutlineVar)
                    }
                },
            ) { innerPadding ->
                Box(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize().hazeSource(state = hazeState),
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
                                value = state.tStartText,
                                unitLabel = TEMP_UNIT_LABELS[state.tStartUnitIdx],
                                hasUnitDropdown = true,
                                onValueChange = component::onTStartChanged,
                                onUnitTapped = {
                                    focusManager.clearFocus()
                                    if (imeVisible) pendingUnitKey = FieldKey.TStart
                                    else component.onUnitChipTapped(FieldKey.TStart)
                                },
                                kelvinHint = if (state.tStartUnitIdx == 1) toKelvin(
                                    state.tStartText,
                                    true
                                ).ifEmpty { null } else null,
                                imeAction = ImeAction.Next,
                                onImeAction = { focusTEnd.requestFocus() },
                                modifier = Modifier.animateItem().focusRequester(focusTStart),
                            )
                        }

                        item {
                            Spacer(Modifier.height(10.dp))
                            InputCard(
                                varMain = "T",
                                varSub = "кон",
                                description = "температура в конце",
                                value = state.tEndText,
                                unitLabel = TEMP_UNIT_LABELS[state.tEndUnitIdx],
                                hasUnitDropdown = true,
                                onValueChange = component::onTEndChanged,
                                onUnitTapped = {
                                    focusManager.clearFocus()
                                    if (imeVisible) pendingUnitKey = FieldKey.TEnd
                                    else component.onUnitChipTapped(FieldKey.TEnd)
                                },
                                kelvinHint = if (state.tEndUnitIdx == 1) toKelvin(
                                    state.tEndText,
                                    true
                                ).ifEmpty { null } else null,
                                imeAction = ImeAction.Next,
                                onImeAction = { focusPStart.requestFocus() },
                                modifier = Modifier.animateItem().focusRequester(focusTEnd),
                            )
                        }

                        item {
                            Spacer(Modifier.height(10.dp))
                            InputCard(
                                varMain = "P", varSub = "нач",
                                description = "давление в начале",
                                value = state.pStartText,
                                unitLabel = "бар",
                                hasUnitDropdown = false,
                                onValueChange = component::onPStartChanged,
                                onUnitTapped = {},
                                imeAction = ImeAction.Next,
                                onImeAction = { focusPEnd.requestFocus() },
                                modifier = Modifier.animateItem().focusRequester(focusPStart),
                            )
                        }

                        item {
                            Spacer(Modifier.height(10.dp))
                            InputCard(
                                varMain = "P", varSub = "кон",
                                description = "давление в конце",
                                value = state.pEndText,
                                unitLabel = "бар",
                                hasUnitDropdown = false,
                                onValueChange = component::onPEndChanged,
                                onUnitTapped = {},
                                imeAction = ImeAction.Next,
                                onImeAction = { focusTime.requestFocus() },
                                modifier = Modifier.animateItem().focusRequester(focusPEnd),
                            )
                        }

                        item {
                            Spacer(Modifier.height(10.dp))
                            InputCard(
                                varMain = "t",
                                varSub = null,
                                description = "время испытания",
                                value = state.timeText,
                                unitLabel = "ч",
                                hasUnitDropdown = false,
                                onValueChange = component::onTimeChanged,
                                onUnitTapped = {},
                                imeAction = ImeAction.Done,
                                onImeAction = { focusManager.clearFocus() },
                                modifier = Modifier.animateItem().focusRequester(focusTime),
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
                                        result = lastResult.value ?: 0.0,
                                        justSaved = justSaved,
                                        onSave = {
                                            component.onSaveRequested(lastResult.value ?: 0.0)
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
                            result = lastResult.value ?: 0.0,
                            hazeState = hazeState,
                            justSaved = justSaved,
                            onSave = {
                                component.onSaveRequested(lastResult.value ?: 0.0)
                                justSaved = true
                            },
                        )
                    }
                }
            }

            val slot by component.unitSheet.subscribeAsState()
            slot.child?.instance?.let { UnitSheetContent(it) }
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
        value = value,
        onValueChange = { v ->
            if (v.isEmpty() || v.matches(Regex("\\d*\\.?\\d*"))) onValueChange(v)
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
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = imeAction),
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
                    Canvas(modifier = Modifier.size(20.dp)) {
                        val s = size.width / 24f
                        if (justSaved) {
                            val sw = 2.5f * s
                            drawLine(AppColors.Primary, Offset(5f * s, 13f * s), Offset(9f * s, 17f * s), sw, StrokeCap.Round)
                            drawLine(AppColors.Primary, Offset(9f * s, 17f * s), Offset(19f * s, 7f * s), sw, StrokeCap.Round)
                        } else {
                            val path = Path().apply {
                                moveTo(19f * s, 21f * s)
                                lineTo(12f * s, 16f * s)
                                lineTo(5f * s, 21f * s)
                                lineTo(5f * s, 5f * s)
                                lineTo(7f * s, 3f * s)
                                lineTo(17f * s, 3f * s)
                                lineTo(19f * s, 5f * s)
                                close()
                            }
                            drawPath(
                                path = path,
                                color = AppColors.Primary.copy(alpha = 0.55f),
                                style = Stroke(width = 2f * s, cap = StrokeCap.Round, join = StrokeJoin.Round),
                            )
                        }
                    }
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
                    drawLine(AppColors.OnSurfaceVar, Offset(x(4f), y(7f)),  Offset(x(20f), y(7f)),  strokeWidth = sw, cap = StrokeCap.Round)
                    drawLine(AppColors.OnSurfaceVar, Offset(x(4f), y(12f)), Offset(x(14f), y(12f)), strokeWidth = sw, cap = StrokeCap.Round)
                    drawLine(AppColors.OnSurfaceVar, Offset(x(4f), y(17f)), Offset(x(17f), y(17f)), strokeWidth = sw, cap = StrokeCap.Round)
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
                t = state.timeText,
                pStart = state.pStartText,
                pEnd = state.pEndText,
                tStartK = toKelvin(state.tStartText, state.tStartUnitIdx == 1),
                tEndK = toKelvin(state.tEndText, state.tEndUnitIdx == 1),
            )
        }
    }
}
