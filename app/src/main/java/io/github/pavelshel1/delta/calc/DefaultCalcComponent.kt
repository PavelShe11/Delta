package io.github.pavelshel1.delta.calc

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.slot.ChildSlot
import com.arkivanov.decompose.router.slot.SlotNavigation
import com.arkivanov.decompose.router.slot.activate
import com.arkivanov.decompose.router.slot.childSlot
import com.arkivanov.decompose.router.slot.dismiss
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import io.github.pavelshel1.delta.unitsheet.DefaultUnitSheetComponent
import io.github.pavelshel1.delta.unitsheet.FieldKey
import io.github.pavelshel1.delta.unitsheet.UnitSheetComponent

class DefaultCalcComponent(
    componentContext: ComponentContext,
    private val onHistory: () -> Unit,
    private val onSave: (Double) -> Unit = {},
) : CalcComponent, ComponentContext by componentContext {

    private val _state = MutableValue(CalcState())
    override val state: Value<CalcState> = _state

    private val sheetNavigation = SlotNavigation<FieldKey>()

    override val unitSheet: Value<ChildSlot<*, UnitSheetComponent>> = childSlot(
        source = sheetNavigation,
        serializer = null,
        handleBackButton = false,
        childFactory = { fieldKey, ctx ->
            val currentIdx = when (fieldKey) {
                FieldKey.TStart -> _state.value.tStartUnitIdx
                FieldKey.TEnd   -> _state.value.tEndUnitIdx
                else            -> 0
            }
            DefaultUnitSheetComponent(
                componentContext = ctx,
                fieldKey = fieldKey,
                currentIdx = currentIdx,
                onSelectAction = { idx ->
                    _state.value = when (fieldKey) {
                        FieldKey.TStart -> _state.value.copy(tStartUnitIdx = idx)
                        FieldKey.TEnd   -> _state.value.copy(tEndUnitIdx = idx)
                        else            -> _state.value
                    }
                    sheetNavigation.dismiss()
                },
                onDismissAction = { sheetNavigation.dismiss() },
            )
        },
    )

    override fun onHistoryRequested() = onHistory()
    override fun onSaveRequested(result: Double) = onSave(result)
    override fun onUnitChipTapped(fieldKey: FieldKey) = sheetNavigation.activate(fieldKey)
    override fun onTStartChanged(text: String) { _state.value = _state.value.copy(tStartText = text) }
    override fun onTEndChanged(text: String)   { _state.value = _state.value.copy(tEndText = text) }
    override fun onPStartChanged(text: String) { _state.value = _state.value.copy(pStartText = text) }
    override fun onPEndChanged(text: String)   { _state.value = _state.value.copy(pEndText = text) }
    override fun onTimeChanged(text: String)   { _state.value = _state.value.copy(timeText = text) }
}
