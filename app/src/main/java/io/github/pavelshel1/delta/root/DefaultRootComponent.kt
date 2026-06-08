package io.github.pavelshel1.delta.root

import android.content.Context
import android.os.Parcelable
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.childContext
import com.arkivanov.decompose.router.slot.ChildSlot
import com.arkivanov.decompose.router.slot.SlotNavigation
import com.arkivanov.decompose.router.slot.activate
import com.arkivanov.decompose.router.slot.childSlot
import com.arkivanov.decompose.router.slot.dismiss
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import io.github.pavelshel1.delta.calc.CalcComponent
import io.github.pavelshel1.delta.calc.DefaultCalcComponent
import io.github.pavelshel1.delta.db.DeltaDatabase
import io.github.pavelshel1.delta.history.DefaultHistoryComponent
import io.github.pavelshel1.delta.history.HistoryComponent
import io.github.pavelshel1.delta.history.HistoryEntry
import io.github.pavelshel1.delta.history.HistoryRepository
import io.github.pavelshel1.delta.unitsheet.FieldKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize

class DefaultRootComponent(
    componentContext: ComponentContext,
    context: Context,
) : RootComponent, ComponentContext by componentContext {

    private val driver = AndroidSqliteDriver(DeltaDatabase.Schema, context, "delta.db")
    private val database = DeltaDatabase(driver)
    private val repository = HistoryRepository(database)
    private val scope = coroutineScope(Dispatchers.Main.immediate)

    private val slotNavigation = SlotNavigation<SlotConfig>()

    override val calc: CalcComponent =
        DefaultCalcComponent(
            componentContext = childContext(key = "calc"),
            onHistory = ::openHistory,
            onSaveEntry = { entry ->
                scope.launch(Dispatchers.IO) {
                    repository.insert(
                        HistoryEntry(
                            id          = 0L,
                            t           = entry.time,
                            pStart      = entry.pStart,
                            pEnd        = entry.pEnd,
                            pStartBar   = entry.pStartBar,
                            pEndBar     = entry.pEndBar,
                            tStartK     = entry.tStartKelvin,
                            tEndK       = entry.tEndKelvin,
                            result      = entry.result,
                            timestampMs = System.currentTimeMillis(),
                            tUnit       = FieldKey.TStart.units[entry.tUnitIdx],
                            pUnit       = FieldKey.PStart.units[entry.pUnitIdx],
                        )
                    )
                }
            },
        )

    override val historySlot: Value<ChildSlot<*, HistoryComponent>> = childSlot(
        source = slotNavigation,
        serializer = null,
        handleBackButton = true,
        childFactory = { _, ctx ->
            DefaultHistoryComponent(
                componentContext      = ctx,
                repository            = repository,
                onDismissAction       = ::closeHistory,
                onEntrySelectedAction = { entry ->
                    closeHistory()
                    calc.onEntrySelected(entry)
                },
            )
        },
    )

    private val _historyCount = MutableValue(0)
    override val historyCount: Value<Int> = _historyCount

    init {
        scope.launch {
            repository.entries().map { it.size }.collect { _historyCount.value = it }
        }
    }

    private fun openHistory() = slotNavigation.activate(SlotConfig.History)
    private fun closeHistory() = slotNavigation.dismiss()

    @Parcelize
    private sealed interface SlotConfig : Parcelable {
        data object History : SlotConfig
    }
}
