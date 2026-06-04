package io.github.pavelshel1.delta.calc

data class CalcState(
    val tStartText: String = "",
    val tEndText: String = "",
    val pStartText: String = "1",
    val pEndText: String = "1",
    val timeText: String = "4",
    val tStartUnitIdx: Int = 1,   // °C по умолчанию
    val tEndUnitIdx: Int = 1,     // °C по умолчанию
)
