package io.github.pavelshel1.delta.history

import com.arkivanov.decompose.ComponentContext

class DefaultHistoryComponent(
    componentContext: ComponentContext,
    private val onDismissAction: () -> Unit,
) : HistoryComponent, ComponentContext by componentContext {

    override fun onDismiss() = onDismissAction()
}
