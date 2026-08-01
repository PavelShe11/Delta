package io.github.pavelshel1.delta.history

import kotlinx.coroutines.flow.StateFlow

interface HistoryComponent {
    val entries: StateFlow<List<HistoryEntry>>
    val onDismissRequested: () -> Unit
    val onEntrySelected: (entry: HistoryEntry) -> Unit
    fun onDelete(id: Long)
    fun onClear()
}
