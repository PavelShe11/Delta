package io.github.pavelshel1.delta.history

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@AssistedInject
class DefaultHistoryComponent(
    @Assisted componentContext: ComponentContext,
    private val repository: HistoryRepository,
    @Assisted override val onDismissRequested: () -> Unit,
    @Assisted override val onEntrySelected: (HistoryEntry) -> Unit,
) : HistoryComponent, ComponentContext by componentContext {

    private val scope = coroutineScope(Dispatchers.Main.immediate)

    override val entries: StateFlow<List<HistoryEntry>> =
        repository.entries().stateIn(scope, SharingStarted.Eagerly, emptyList())

    override fun onDelete(id: Long) {
        scope.launch(Dispatchers.IO) { repository.deleteById(id) }
    }

    override fun onClear() {
        scope.launch(Dispatchers.IO) { repository.deleteAll() }
    }

    @AssistedFactory
    fun interface Factory {
        fun create(
            componentContext: ComponentContext,
            onDismissRequested: () -> Unit,
            onEntrySelected: (HistoryEntry) -> Unit,
        ): DefaultHistoryComponent
    }
}
