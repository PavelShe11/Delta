package io.github.pavelshel1.delta.calc

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import kotlinx.serialization.Serializable

data class CalcState(
    val tStartCelsius: BigDecimal? = null,
    val tStartKelvin: BigDecimal? = null,

    val tEndCelsius: BigDecimal? = null,
    val tEndKelvin: BigDecimal? = null,
    val tUnitIdx: Int        = 0,
    val pUnitIdx: Int        = 0,

    val pStart: BigDecimal?    = null,
    val pEnd: BigDecimal?      = null,
    val pStartBar: BigDecimal? = BigDecimal.ONE,
    val pEndBar: BigDecimal?   = BigDecimal.ONE,
    val time: BigDecimal?      = BigDecimal.valueOf(4),

    val result: BigDecimal? = null,
) {
    val tStartText: BigDecimal? get() = if (tUnitIdx == 0) tStartCelsius else tStartKelvin
    val tEndText: BigDecimal?   get() = if (tUnitIdx == 0) tEndCelsius else tEndKelvin

    val filledCount: Int get() = listOf(
        tStartText, tEndText, pStart, pEnd, pStartBar, pEndBar, time
    ).count { it != null }
}

internal fun BigDecimal?.toDisplayString(): String =
    this?.stripTrailingZeros()?.toPlainString() ?: ""

private val KELVIN_OFFSET = BigDecimal("273")

internal fun celsiusToKelvin(s: String): String {
    val num = s.toBigDecimalOrNull() ?: return ""
    return num.add(KELVIN_OFFSET).stripTrailingZeros().toPlainString()
}

internal fun kelvinToCelsius(s: String): String {
    val num = s.toBigDecimalOrNull() ?: return ""
    return num.subtract(KELVIN_OFFSET).stripTrailingZeros().toPlainString()
}

private val MC = MathContext.DECIMAL64

internal fun CalcState.computeResult(): BigDecimal? {
    val t    = time      ?: return null
    val pN   = pStart    ?: return null
    val pK   = pEnd      ?: return null
    val pNB  = pStartBar ?: return null
    val pKB  = pEndBar   ?: return null
    val tNk  = tStartKelvin ?: return null
    val tKK  = tEndKelvin   ?: return null
    val pNabs = pN.add(pNB)
    val pKabs = pK.add(pKB)
    if (t.compareTo(BigDecimal.ZERO) == 0 ||
        pNabs.compareTo(BigDecimal.ZERO) == 0 ||
        tKK.compareTo(BigDecimal.ZERO) == 0) return null
    val denom = pNabs.multiply(tKK, MC)
    val ratio = pKabs.multiply(tNk, MC).divide(denom, MC)
    return BigDecimal("100").divide(t, MC).multiply(BigDecimal.ONE.subtract(ratio, MC), MC)
        .setScale(3, RoundingMode.HALF_UP)
}

internal fun CalcState.withResult(): CalcState = copy(result = computeResult())

@Serializable
data class CalcStateSnapshot(
    val tStartCelsius: String? = null,
    val tStartKelvin: String?  = null,
    val tEndCelsius: String?   = null,
    val tEndKelvin: String?    = null,
    val tUnitIdx: Int          = 0,
    val pUnitIdx: Int          = 0,
    val pStart: String?        = null,
    val pEnd: String?          = null,
    val pStartBar: String?     = null,
    val pEndBar: String?       = null,
    val time: String?          = null,
)

internal fun CalcState.toSnapshot() = CalcStateSnapshot(
    tStartCelsius = tStartCelsius?.toPlainString(),
    tStartKelvin  = tStartKelvin?.toPlainString(),
    tEndCelsius   = tEndCelsius?.toPlainString(),
    tEndKelvin    = tEndKelvin?.toPlainString(),
    tUnitIdx      = tUnitIdx,
    pUnitIdx      = pUnitIdx,
    pStart        = pStart?.toPlainString(),
    pEnd          = pEnd?.toPlainString(),
    pStartBar     = pStartBar?.toPlainString(),
    pEndBar       = pEndBar?.toPlainString(),
    time          = time?.toPlainString(),
)

internal fun CalcStateSnapshot.toCalcState() = CalcState(
    tStartCelsius = tStartCelsius?.toBigDecimalOrNull(),
    tStartKelvin  = tStartKelvin?.toBigDecimalOrNull(),
    tEndCelsius   = tEndCelsius?.toBigDecimalOrNull(),
    tEndKelvin    = tEndKelvin?.toBigDecimalOrNull(),
    tUnitIdx      = tUnitIdx,
    pUnitIdx      = pUnitIdx,
    pStart        = pStart?.toBigDecimalOrNull(),
    pEnd          = pEnd?.toBigDecimalOrNull(),
    pStartBar     = pStartBar?.toBigDecimalOrNull() ?: BigDecimal.ONE,
    pEndBar       = pEndBar?.toBigDecimalOrNull()   ?: BigDecimal.ONE,
    time          = time?.toBigDecimalOrNull(),
).withResult()
