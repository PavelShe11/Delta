package io.github.pavelshel1.delta.history

import kotlinx.coroutines.flow.StateFlow

interface HistoryComponent {
    val entries: StateFlow<List<HistoryEntry>>
    fun onDismiss()
    fun onDelete(id: Long)
    fun onClear()
}
