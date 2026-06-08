package io.github.pavelshel1.delta.calc

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.slot.ChildSlot
import com.arkivanov.decompose.router.slot.SlotNavigation
import com.arkivanov.decompose.router.slot.activate
import com.arkivanov.decompose.router.slot.childSlot
import com.arkivanov.decompose.router.slot.dismiss
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.core.rx.observer
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import io.github.pavelshel1.delta.unitsheet.DefaultUnitSheetComponent
import io.github.pavelshel1.delta.unitsheet.FieldKey
import io.github.pavelshel1.delta.unitsheet.UnitSheetComponent

class DefaultCalcComponent(
    componentContext: ComponentContext,
    storeFactory: StoreFactory = DefaultStoreFactory(),
    private val onHistory: () -> Unit,
    private val onSaveEntry: (CalcEntry) -> Unit = {},
) : CalcComponent, ComponentContext by componentContext {

    private val store: CalcStore = instanceKeeper.getStore {
        CalcStoreFactory(storeFactory).create(stateKeeper)
    }

    private val _state = MutableValue(store.state)
    override val state: Value<CalcState> = _state

    init {
        store.states(observer(onNext = { _state.value = it }))
            .also { disposable -> lifecycle.doOnDestroy(disposable::dispose) }
        store.labels(observer(onNext = { label ->
            when (label) {
                is CalcStore.Label.SavedToHistory -> onSaveEntry(label.entry)
            }
        })).also { disposable -> lifecycle.doOnDestroy(disposable::dispose) }
    }

    private val sheetNavigation = SlotNavigation<FieldKey>()

    override val unitSheet: Value<ChildSlot<*, UnitSheetComponent>> = childSlot(
        source = sheetNavigation,
        serializer = null,
        handleBackButton = false,
        childFactory = { fieldKey, ctx ->
            val unitIdx = when (fieldKey) {
                FieldKey.TStart, FieldKey.TEnd                                           -> store.state.tUnitIdx
                FieldKey.PStart, FieldKey.PEnd, FieldKey.PStartBar, FieldKey.PEndBar     -> store.state.pUnitIdx
                else                                                                      -> 0
            }
            DefaultUnitSheetComponent(
                componentContext = ctx,
                fieldKey         = fieldKey,
                units            = fieldKey.units,
                currentUnit      = fieldKey.units[unitIdx],
                onSelectAction   = { unit ->
                    val idx = fieldKey.units.indexOf(unit)
                    store.accept(CalcStore.Intent.SelectUnit(fieldKey, idx))
                    sheetNavigation.dismiss()
                },
                onDismissAction  = { sheetNavigation.dismiss() },
            )
        },
    )

    override fun onHistoryRequested() = onHistory()
    override fun onSaveRequested(result: Double) = store.accept(CalcStore.Intent.Save)
    override fun onUnitChipTapped(fieldKey: FieldKey) = sheetNavigation.activate(fieldKey)
    override fun onTStartChanged(text: String) {
        store.accept(CalcStore.Intent.ChangeTStart(text))
    }

    override fun onTEndChanged(text: String) {
        store.accept(CalcStore.Intent.ChangeTEnd(text))
    }

    override fun onPStartChanged(text: String) {
        store.accept(CalcStore.Intent.ChangePStart(text))
    }

    override fun onPEndChanged(text: String) {
        store.accept(CalcStore.Intent.ChangePEnd(text))
    }

    override fun onPStartBarChanged(text: String) {
        store.accept(CalcStore.Intent.ChangePStartBar(text))
    }

    override fun onPEndBarChanged(text: String) {
        store.accept(CalcStore.Intent.ChangePEndBar(text))
    }

    override fun onTimeChanged(text: String) {
        store.accept(CalcStore.Intent.ChangeTime(text))
    }
}
