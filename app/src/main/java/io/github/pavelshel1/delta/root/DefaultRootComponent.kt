package io.github.pavelshel1.delta.root

import android.os.Parcelable
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
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import io.github.pavelshel1.delta.calc.CalcComponent
import io.github.pavelshel1.delta.calc.DefaultCalcComponent
import io.github.pavelshel1.delta.history.DefaultHistoryComponent
import io.github.pavelshel1.delta.history.HistoryComponent
import io.github.pavelshel1.delta.history.HistoryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize

@AssistedInject
class DefaultRootComponent(
    @Assisted componentContext: ComponentContext,
    private val repository: HistoryRepository,
    private val calcComponentFactory: DefaultCalcComponent.Factory,
    private val historyComponentFactory: DefaultHistoryComponent.Factory,
) : RootComponent, ComponentContext by componentContext {

    private val scope = coroutineScope(Dispatchers.Main.immediate)

    private val slotNavigation = SlotNavigation<SlotConfig>()

    override val calc: CalcComponent =
        calcComponentFactory.create(
            componentContext = childContext(key = "calc"),
            onHistoryRequested = ::openHistory,
        )

    override val historySlot: Value<ChildSlot<*, HistoryComponent>> = childSlot(
        source = slotNavigation,
        serializer = null,
        handleBackButton = true,
        childFactory = { _, ctx ->
            historyComponentFactory.create(
                componentContext   = ctx,
                onDismissRequested = ::closeHistory,
                onEntrySelected    = { entry ->
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

    @AssistedFactory
    fun interface Factory {
        fun create(componentContext: ComponentContext): DefaultRootComponent
    }
}
