package io.github.pavelshel1.delta.history

import com.arkivanov.mvikotlin.core.store.Store

interface HistoryStore : Store<HistoryStore.Intent, HistoryState, Nothing> {

    sealed interface Intent {
        data class EntryLongPressed(val entry: HistoryEntry) : Intent
        data class EntryToggled(val entry: HistoryEntry) : Intent
        data object SelectionCleared : Intent
        data object SelectAllToggled : Intent
        data class Delete(val id: Long) : Intent
        data object DeleteSelected : Intent
    }
}
