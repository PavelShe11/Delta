package io.github.pavelshel1.delta.calc

import com.arkivanov.decompose.router.slot.ChildSlot
import com.arkivanov.decompose.value.Value
import io.github.pavelshel1.delta.unitsheet.FieldKey
import io.github.pavelshel1.delta.unitsheet.UnitSheetComponent

interface CalcComponent {
    val state: Value<CalcState>
    val unitSheet: Value<ChildSlot<*, UnitSheetComponent>>
    fun onHistoryRequested()
    fun onSaveRequested(result: Double)
    fun onUnitChipTapped(fieldKey: FieldKey)
    fun onTStartChanged(text: String)
    fun onTEndChanged(text: String)
    fun onPStartChanged(text: String)
    fun onPEndChanged(text: String)
    fun onPStartBarChanged(text: String)
    fun onPEndBarChanged(text: String)
    fun onTimeChanged(text: String)
    fun onEntrySelected(entry: io.github.pavelshel1.delta.history.HistoryEntry)
}
