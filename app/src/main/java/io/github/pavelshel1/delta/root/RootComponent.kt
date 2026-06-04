package io.github.pavelshel1.delta.root

import com.arkivanov.decompose.router.slot.ChildSlot
import com.arkivanov.decompose.value.Value
import io.github.pavelshel1.delta.calc.CalcComponent
import io.github.pavelshel1.delta.history.HistoryComponent

interface RootComponent {

    /** Основной экран — всегда жив. */
    val calc: CalcComponent

    /** Модальный экран истории. `child` == null значит модалка закрыта. */
    val historySlot: Value<ChildSlot<*, HistoryComponent>>
}
