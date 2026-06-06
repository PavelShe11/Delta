package io.github.pavelshel1.delta.calc

import com.arkivanov.mvikotlin.core.store.Store
import io.github.pavelshel1.delta.unitsheet.FieldKey

interface CalcStore : Store<CalcStore.Intent, CalcState, CalcStore.Label> {

    sealed interface Intent {
        data class ChangeTStart(val text: String) : Intent
        data class ChangeTEnd(val text: String)   : Intent
        data class ChangePStart(val text: String) : Intent
        data class ChangePEnd(val text: String)   : Intent
        data class ChangeTime(val text: String)   : Intent
        data class SelectUnit(val fieldKey: FieldKey, val unitIdx: Int) : Intent
        data object Save : Intent
        data class LoadEntry(val entry: CalcEntry) : Intent
    }

    sealed interface Label {
        data object SavedToHistory : Label
    }
}

data class CalcEntry(
    val tStartCelsius: String,
    val tStartKelvin: String,
    val tStartUnitIdx: Int,
    val tEndCelsius: String,
    val tEndKelvin: String,
    val tEndUnitIdx: Int,
    val pStart: String,
    val pEnd: String,
    val time: String,
    val result: Double,
)
