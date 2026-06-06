package io.github.pavelshel1.delta.calc

import java.math.BigDecimal
import java.math.MathContext
import kotlinx.serialization.Serializable

data class CalcState(
    val tStartCelsius: BigDecimal? = null,
    val tStartKelvin: BigDecimal? = null,
    val tStartUnitIdx: Int    = 0,

    val tEndCelsius: BigDecimal? = null,
    val tEndKelvin: BigDecimal? = null,
    val tEndUnitIdx: Int    = 0,

    val pStart: BigDecimal? = BigDecimal.ONE,
    val pEnd: BigDecimal?    = BigDecimal.ONE,
    val time: BigDecimal?    = BigDecimal.valueOf(4),

    val result: BigDecimal? = null,
) {
    val tStartText: BigDecimal? get() = if (tStartUnitIdx == 0) tStartCelsius else tStartKelvin
    val tEndText: BigDecimal?   get() = if (tEndUnitIdx == 0) tEndCelsius else tEndKelvin

    val filledCount: Int get() = listOf(
        tStartText, tEndText, pStart, pEnd, time
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
    val t   = time           ?: return null
    val pN  = pStart         ?: return null
    val pK  = pEnd           ?: return null
    val tNk = tStartKelvin   ?: return null
    val tKK = tEndKelvin     ?: return null
    if (t.compareTo(BigDecimal.ZERO) == 0 ||
        pN.compareTo(BigDecimal.ZERO) == 0 ||
        tKK.compareTo(BigDecimal.ZERO) == 0) return null
    val denom = pN.multiply(tKK, MC)
    val ratio = pK.multiply(tNk, MC).divide(denom, MC)
    return BigDecimal("100").divide(t, MC).multiply(BigDecimal.ONE.subtract(ratio, MC), MC)
}

internal fun CalcState.withResult(): CalcState = copy(result = computeResult())

// ── StateKeeper snapshot (BigDecimal → String для сериализации) ───────────

@Serializable
data class CalcStateSnapshot(
    val tStartCelsius: String? = null,
    val tStartKelvin: String?  = null,
    val tStartUnitIdx: Int     = 0,
    val tEndCelsius: String?   = null,
    val tEndKelvin: String?    = null,
    val tEndUnitIdx: Int       = 0,
    val pStart: String?        = null,
    val pEnd: String?          = null,
    val time: String?          = null,
)

internal fun CalcState.toSnapshot() = CalcStateSnapshot(
    tStartCelsius = tStartCelsius?.toPlainString(),
    tStartKelvin  = tStartKelvin?.toPlainString(),
    tStartUnitIdx = tStartUnitIdx,
    tEndCelsius   = tEndCelsius?.toPlainString(),
    tEndKelvin    = tEndKelvin?.toPlainString(),
    tEndUnitIdx   = tEndUnitIdx,
    pStart        = pStart?.toPlainString(),
    pEnd          = pEnd?.toPlainString(),
    time          = time?.toPlainString(),
)

internal fun CalcStateSnapshot.toCalcState() = CalcState(
    tStartCelsius = tStartCelsius?.toBigDecimalOrNull(),
    tStartKelvin  = tStartKelvin?.toBigDecimalOrNull(),
    tStartUnitIdx = tStartUnitIdx,
    tEndCelsius   = tEndCelsius?.toBigDecimalOrNull(),
    tEndKelvin    = tEndKelvin?.toBigDecimalOrNull(),
    tEndUnitIdx   = tEndUnitIdx,
    pStart        = pStart?.toBigDecimalOrNull(),
    pEnd          = pEnd?.toBigDecimalOrNull(),
    time          = time?.toBigDecimalOrNull(),
).withResult()
