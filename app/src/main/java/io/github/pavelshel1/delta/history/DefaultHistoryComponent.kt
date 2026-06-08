package io.github.pavelshel1.delta.history

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DefaultHistoryComponent(
    componentContext: ComponentContext,
    private val repository: HistoryRepository,
    private val onDismissAction: () -> Unit,
) : HistoryComponent, ComponentContext by componentContext {

    private val scope = coroutineScope(Dispatchers.Main.immediate)

    override val entries: StateFlow<List<HistoryEntry>> =
        repository.entries().stateIn(scope, SharingStarted.Eagerly, emptyList())

    override fun onDismiss() = onDismissAction()

    override fun onDelete(id: Long) {
        scope.launch(Dispatchers.IO) { repository.deleteById(id) }
    }

    override fun onClear() {
        scope.launch(Dispatchers.IO) { repository.deleteAll() }
    }
}
