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
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import io.github.pavelshel1.delta.about.AboutSheetComponent
import io.github.pavelshel1.delta.about.DefaultAboutSheetComponent
import io.github.pavelshel1.delta.history.HistoryEntry
import io.github.pavelshel1.delta.mail.DefaultSmtpSettingsComponent
import io.github.pavelshel1.delta.mail.SmtpSettingsComponent
import io.github.pavelshel1.delta.mail.SmtpSettingsRepository
import io.github.pavelshel1.delta.savedialog.DefaultSaveDialogComponent
import io.github.pavelshel1.delta.savedialog.SaveDialogComponent
import io.github.pavelshel1.delta.unitsheet.DefaultUnitSheetComponent
import io.github.pavelshel1.delta.unitsheet.FieldKey
import io.github.pavelshel1.delta.unitsheet.UnitSheetComponent

@AssistedInject
class DefaultCalcComponent(
    @Assisted componentContext: ComponentContext,
    @Assisted override val onHistoryRequested: () -> Unit,
    calcStoreFactory: CalcStoreFactory,
    private val aboutSheetComponentFactory: DefaultAboutSheetComponent.Factory,
    private val smtpSettingsRepository: SmtpSettingsRepository,
) : CalcComponent, ComponentContext by componentContext {

    @AssistedFactory
    fun interface Factory {
        fun create(
            componentContext: ComponentContext,
            onHistoryRequested: () -> Unit
        ): DefaultCalcComponent
    }

    private val store: CalcStore = instanceKeeper.getStore {
        calcStoreFactory.create(stateKeeper)
    }

    private val _state = MutableValue(store.state)
    override val state: Value<CalcState> = _state

    init {
        store.states(observer(onNext = { _state.value = it }))
            .also { disposable -> lifecycle.doOnDestroy(disposable::dispose) }
    }

    private val sheetNavigation = SlotNavigation<FieldKey>()

    override val unitSheet: Value<ChildSlot<*, UnitSheetComponent>> = childSlot(
        source = sheetNavigation,
        serializer = null,
        handleBackButton = false,
        childFactory = { fieldKey, ctx ->
            val unitIdx = when (fieldKey) {
                FieldKey.TStart, FieldKey.TEnd -> store.state.tUnitIdx
                FieldKey.PStart, FieldKey.PEnd, FieldKey.PStartBar, FieldKey.PEndBar -> store.state.pUnitIdx
                else -> 0
            }
            DefaultUnitSheetComponent(
                componentContext = ctx,
                fieldKey = fieldKey,
                units = fieldKey.units,
                currentUnit = fieldKey.units[unitIdx],
                onUnitSelected = { unit ->
                    val idx = fieldKey.units.indexOf(unit)
                    store.accept(CalcStore.Intent.SelectUnit(fieldKey, idx))
                    sheetNavigation.dismiss()
                },
                onDismissRequested = { sheetNavigation.dismiss() },
            )
        },
    )

    private val smtpSettingsNavigation = SlotNavigation<Unit>()

    override val smtpSettings: Value<ChildSlot<*, SmtpSettingsComponent>> = childSlot(
        source = smtpSettingsNavigation,
        key = "SmtpSettings",
        serializer = null,
        handleBackButton = false,
        childFactory = { _, ctx ->
            DefaultSmtpSettingsComponent(
                componentContext = ctx,
                initial = smtpSettingsRepository.settings(),
                onSaveRequested = { settings ->
                    smtpSettingsRepository.save(settings)
                    smtpSettingsNavigation.dismiss()
                },
                onCancelRequested = { smtpSettingsNavigation.dismiss() },
            )
        },
    )

    private val aboutNavigation = SlotNavigation<Unit>()

    override val aboutSheet: Value<ChildSlot<*, AboutSheetComponent>> = childSlot(
        source = aboutNavigation,
        key = "AboutSheet",
        serializer = null,
        handleBackButton = false,
        childFactory = { _, ctx ->
            aboutSheetComponentFactory.create(
                componentContext = ctx,
                onDismissRequested = { aboutNavigation.dismiss() },
                onSmtpSettingsRequested = {
                    aboutNavigation.dismiss()
                    smtpSettingsNavigation.activate(Unit)
                },
            )
        },
    )

    private val saveDialogNavigation = SlotNavigation<Unit>()

    override val saveDialog: Value<ChildSlot<*, SaveDialogComponent>> = childSlot(
        source = saveDialogNavigation,
        key = "SaveDialog",
        serializer = null,
        handleBackButton = false,
        childFactory = { _, ctx ->
            DefaultSaveDialogComponent(
                componentContext = ctx,
                onSaveRequested = { name, comment ->
                    store.accept(CalcStore.Intent.Save(name, comment))
                    saveDialogNavigation.dismiss()
                },
                onCancelRequested = { saveDialogNavigation.dismiss() },
            )
        },
    )

    override fun onSaveRequested(result: Double) = saveDialogNavigation.activate(Unit)
    override fun onUndoSave() = store.accept(CalcStore.Intent.UndoSave)
    override fun onAboutRequested() = aboutNavigation.activate(Unit)
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

    override fun onEntrySelected(entry: HistoryEntry) {
        val tUnitIdx = FieldKey.TStart.units.indexOf(entry.tUnit).coerceAtLeast(0)
        val pUnitIdx = FieldKey.PStart.units.indexOf(entry.pUnit).coerceAtLeast(0)
        store.accept(
            CalcStore.Intent.LoadEntry(
                CalcEntry(
                    tStartCelsius = kelvinToCelsius(entry.tStartK),
                    tStartKelvin = entry.tStartK,
                    tEndCelsius = kelvinToCelsius(entry.tEndK),
                    tEndKelvin = entry.tEndK,
                    tUnitIdx = tUnitIdx,
                    pUnitIdx = pUnitIdx,
                    pStart = entry.pStart,
                    pEnd = entry.pEnd,
                    pStartBar = entry.pStartBar,
                    pEndBar = entry.pEndBar,
                    time = entry.t,
                    result = entry.result,
                )
            )
        )
    }

}
