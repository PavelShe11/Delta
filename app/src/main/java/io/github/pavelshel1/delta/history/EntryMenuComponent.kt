package io.github.pavelshel1.delta.history

interface EntryMenuComponent {
    val entry: HistoryEntry
    val onDismissRequested: () -> Unit
}
