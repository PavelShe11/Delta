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
import io.github.pavelshel1.delta.calc.CalcComponent
import io.github.pavelshel1.delta.calc.DefaultCalcComponent
import io.github.pavelshel1.delta.history.DefaultHistoryComponent
import io.github.pavelshel1.delta.history.HistoryComponent
import kotlinx.parcelize.Parcelize

class DefaultRootComponent(
    componentContext: ComponentContext,
) : RootComponent, ComponentContext by componentContext {

    private val slotNavigation = SlotNavigation<SlotConfig>()

    override val calc: CalcComponent =
        DefaultCalcComponent(
            componentContext = childContext(key = "calc"),
            onHistory = ::openHistory,
        )

    override val historySlot: Value<ChildSlot<*, HistoryComponent>> = childSlot(
        source = slotNavigation,
        serializer = null,
        handleBackButton = true,
        childFactory = { _, ctx ->
            DefaultHistoryComponent(
                componentContext = ctx,
                onDismissAction = ::closeHistory,
            )
        },
    )

    private val _historyCount = MutableValue(100)
    override val historyCount: Value<Int> = _historyCount

    private fun openHistory() = slotNavigation.activate(SlotConfig.History)
    private fun closeHistory() = slotNavigation.dismiss()

    @Parcelize
    private sealed interface SlotConfig : Parcelable {
        data object History : SlotConfig
    }
}
