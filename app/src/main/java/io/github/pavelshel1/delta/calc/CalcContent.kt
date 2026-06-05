package io.github.pavelshel1.delta.calc

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalOverscrollConfiguration
import androidx.compose.foundation.LocalOverscrollFactory
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.relocation.BringIntoViewResponder
import androidx.compose.foundation.relocation.bringIntoViewResponder
import androidx.compose.ui.geometry.Rect as GeometryRect
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import io.github.pavelshel1.delta.formula.DeltaPAbstractFormula
import io.github.pavelshel1.delta.formula.DeltaPFormulaWithValues
import io.github.pavelshel1.delta.ui.theme.AppColors
import io.github.pavelshel1.delta.unitsheet.FieldKey
import io.github.pavelshel1.delta.unitsheet.UnitSheetContent

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalcContent(component: CalcComponent, modifier: Modifier = Modifier) {
    val state by component.state.subscribeAsState()
    val focusManager = LocalFocusManager.current

    val filledCount = listOf(
        state.tStartText, state.tEndText,
        state.pStartText, state.pEndText, state.timeText,
    ).count { it.isNotEmpty() }

    val result = calcDeltaP(state)
    val listState = rememberLazyListState(7)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AppColors.Background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

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
                    IconButton(onClick = {
                        focusManager.clearFocus()
                        component.onHistoryRequested()
                    }) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "История",
                            tint = AppColors.OnSurface,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AppColors.Background),
            )

            ProgressBar(filled = filledCount, total = 5)

            CompositionLocalProvider(LocalOverscrollFactory provides null) {
                LazyColumn(
                    reverseLayout = true,
                    state = listState,
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(
                        start = 14.dp,
                        end = 14.dp,
                        top = 12.dp,
                        bottom = 16.dp
                    )
                ) {
                    item {
                        FormulaCard(state = state, modifier = Modifier.animateItem())
                    }

                    stickyHeader(key = "result_block") {
                        val lastResult = remember { mutableStateOf<Double?>(null) }
                        if (result != null) lastResult.value = result
                        AnimatedVisibility(
                            visible = result != null,
                            enter = fadeIn(tween(250)) + slideInVertically(tween(300)) { it },
                            exit = fadeOut(tween(200))
                        ) {
                            Column {
                                ResultBlock(result = lastResult.value ?: 0.0)
                                Spacer(Modifier.height(10.dp))
                            }
                        }
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
                            modifier = Modifier.animateItem()
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
                            modifier = Modifier.animateItem()
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
                            modifier = Modifier.animateItem()
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
                            onUnitTapped = { component.onUnitChipTapped(FieldKey.TEnd) },
                            modifier = Modifier.animateItem()
                        )
                    }

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
                            onUnitTapped = { component.onUnitChipTapped(FieldKey.TStart) },
                            modifier = Modifier.animateItem()
                        )
                    }
                }
            }
        }

        val slot by component.unitSheet.subscribeAsState()
        slot.child?.instance?.let { UnitSheetContent(it) }
    }
}

@Composable
private fun ProgressBar(filled: Int, total: Int) {
    val fraction = filled.toFloat() / total
    val done = filled == total

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppColors.Background)
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
                    .fillMaxWidth(fraction)
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
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(AppColors.Surface)
            .border(2.dp, AppColors.OutlineVar, RoundedCornerShape(18.dp))
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
            modifier = Modifier.padding(bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            BasicTextField(
                value = value,
                onValueChange = { v ->
                    if (v.isEmpty() || v.matches(Regex("\\d*\\.?\\d*"))) onValueChange(v)
                },
                modifier = Modifier.weight(1f),
                textStyle = TextStyle(
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Normal,
                    color = if (value.isNotEmpty()) AppColors.OnSurface else AppColors.Outline,
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                cursorBrush = SolidColor(AppColors.Primary),
                decorationBox = { innerTextField ->
                    Box {
                        if (value.isEmpty()) {
                            Text("—", fontSize = 42.sp, color = AppColors.Outline)
                        }
                        innerTextField()
                    }
                },
            )
            UnitChip(label = unitLabel, hasDropdown = hasUnitDropdown, onClick = onUnitTapped)
        }
    }
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
private fun UnitChip(label: String, hasDropdown: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .clip(CircleShape)
            .background(AppColors.SurfaceHighest)
            .border(1.dp, AppColors.OutlineVar, CircleShape)
            .then(if (hasDropdown) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = AppColors.OnSurface,
        )
        if (hasDropdown) {
            Text(
                text = "∨",
                fontSize = 11.sp,
                color = AppColors.OnSurfaceVar,
            )
        }
    }
}

@Composable
private fun ResultBlock(result: Double) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(AppColors.ResultGradientStart, AppColors.ResultGradientEnd),
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
                .border(
                    1.dp,
                    AppColors.OutlineVar,
                    RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
                )
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "ФОРМУЛА РАСЧЁТА",
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.1.sp,
                color = AppColors.OnSurfaceVar,
            )
            Text(
                text = "ΔP",
                fontSize = 14.sp,
                fontStyle = FontStyle.Italic,
                fontWeight = FontWeight.SemiBold,
                color = AppColors.RVar,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(AppColors.RVar.copy(alpha = 0.12f))
                    .border(1.dp, AppColors.RVar.copy(alpha = 0.30f), CircleShape)
                    .padding(horizontal = 10.dp, vertical = 4.dp),
            )
        }

        HorizontalDivider(color = AppColors.OutlineVar)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(0.dp),
        ) {
            DeltaPAbstractFormula()

            HorizontalDivider(
                color = AppColors.OutlineVar,
                modifier = Modifier.padding(vertical = 14.dp, horizontal = 16.dp),
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
