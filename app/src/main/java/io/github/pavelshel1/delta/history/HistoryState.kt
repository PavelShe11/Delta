package io.github.pavelshel1.delta.history

data class HistoryState(
    val entries: List<HistoryEntry> = emptyList(),
    val selectedIds: List<Long> = emptyList(),
) {
    val isSelectionMode: Boolean get() = selectedIds.isNotEmpty()
}
