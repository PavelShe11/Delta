package io.github.pavelshel1.delta.history

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@SingleIn(AppScope::class)
@Inject
class HistoryStoreFactory(
    private val storeFactory: StoreFactory,
    private val repository: HistoryRepository,
) {

    fun create(): HistoryStore =
        object : HistoryStore, Store<HistoryStore.Intent, HistoryState, Nothing>
        by storeFactory.create(
            name            = STORE_NAME,
            initialState    = HistoryState(),
            executorFactory = { ExecutorImpl(repository) },
            reducer         = ReducerImpl,
        ) {}

    companion object {
        private const val STORE_NAME = "HistoryStore"
    }

    private sealed interface Msg {
        data class EntriesLoaded(val entries: List<HistoryEntry>) : Msg
        data class SelectionSet(val ids: List<Long>) : Msg
    }

    private class ExecutorImpl(
        private val repository: HistoryRepository,
    ) : CoroutineExecutor<HistoryStore.Intent, Nothing, HistoryState, Msg, Nothing>() {

        init {
            scope.launch {
                repository.entries().collect { dispatch(Msg.EntriesLoaded(it)) }
            }
        }

        override fun executeIntent(intent: HistoryStore.Intent) {
            when (intent) {
                is HistoryStore.Intent.EntryLongPressed ->
                    dispatch(Msg.SelectionSet(listOf(intent.entry.id)))

                is HistoryStore.Intent.EntryToggled -> {
                    val current = state().selectedIds
                    val id = intent.entry.id
                    dispatch(Msg.SelectionSet(if (id in current) current - id else current + id))
                }

                is HistoryStore.Intent.SelectionCleared ->
                    dispatch(Msg.SelectionSet(emptyList()))

                is HistoryStore.Intent.SelectAllToggled -> {
                    val s = state()
                    val allIds = s.entries.map { it.id }
                    dispatch(Msg.SelectionSet(if (s.selectedIds.size == allIds.size) emptyList() else allIds))
                }

                is HistoryStore.Intent.Delete ->
                    scope.launch(Dispatchers.IO) { repository.deleteById(intent.id) }

                is HistoryStore.Intent.DeleteSelected -> {
                    val ids = state().selectedIds
                    dispatch(Msg.SelectionSet(emptyList()))
                    scope.launch(Dispatchers.IO) { repository.deleteByIds(ids) }
                }
            }
        }
    }

    private object ReducerImpl : Reducer<HistoryState, Msg> {
        override fun HistoryState.reduce(msg: Msg): HistoryState = when (msg) {
            is Msg.EntriesLoaded -> copy(
                entries = msg.entries,
                selectedIds = selectedIds.filter { id -> msg.entries.any { it.id == id } },
            )
            is Msg.SelectionSet -> copy(selectedIds = msg.ids)
        }
    }
}
