package io.github.pavelshel1.delta.history

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.slot.ChildSlot
import com.arkivanov.decompose.router.slot.SlotNavigation
import com.arkivanov.decompose.router.slot.activate
import com.arkivanov.decompose.router.slot.childSlot
import com.arkivanov.decompose.router.slot.dismiss
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.core.rx.observer
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject

@AssistedInject
class DefaultHistoryComponent(
    @Assisted componentContext: ComponentContext,
    historyStoreFactory: HistoryStoreFactory,
    @Assisted override val onDismissRequested: () -> Unit,
    @Assisted private val onEntrySelected: (HistoryEntry) -> Unit,
) : HistoryComponent, ComponentContext by componentContext {

    private val store: HistoryStore = instanceKeeper.getStore { historyStoreFactory.create() }

    private val _state = MutableValue(store.state)
    override val state: Value<HistoryState> = _state

    init {
        store.states(observer(onNext = { _state.value = it }))
            .also { disposable -> lifecycle.doOnDestroy(disposable::dispose) }
    }

    override fun onEntryTapped(entry: HistoryEntry) {
        if (state.value.isSelectionMode) {
            store.accept(HistoryStore.Intent.EntryToggled(entry))
        } else {
            onEntrySelected(entry)
        }
    }

    override fun onEntryLongPressed(entry: HistoryEntry) {
        store.accept(HistoryStore.Intent.EntryLongPressed(entry))
    }

    override fun onSelectionCleared() {
        store.accept(HistoryStore.Intent.SelectionCleared)
    }

    override fun onSelectAllToggled() {
        store.accept(HistoryStore.Intent.SelectAllToggled)
    }

    private val deleteConfirmNavigation = SlotNavigation<List<Long>>()

    override val deleteConfirm: Value<ChildSlot<*, DeleteConfirmComponent>> = childSlot(
        source = deleteConfirmNavigation,
        key = "DeleteConfirm",
        serializer = null,
        handleBackButton = false,
        childFactory = { ids, ctx ->
            DefaultDeleteConfirmComponent(
                componentContext = ctx,
                count = ids.size,
                onConfirmed = {
                    if (ids.size == 1) {
                        store.accept(HistoryStore.Intent.Delete(ids.first()))
                    } else {
                        store.accept(HistoryStore.Intent.DeleteSelected)
                    }
                    deleteConfirmNavigation.dismiss()
                },
                onDismissRequested = { deleteConfirmNavigation.dismiss() },
            )
        },
    )

    override fun onDeleteRequested(id: Long) {
        deleteConfirmNavigation.activate(listOf(id))
    }

    override fun onDeleteSelectedRequested() {
        deleteConfirmNavigation.activate(state.value.selectedIds)
    }

    private val entryMenuNavigation = SlotNavigation<HistoryEntry>()

    override val entryMenu: Value<ChildSlot<*, EntryMenuComponent>> = childSlot(
        source = entryMenuNavigation,
        key = "EntryMenu",
        serializer = null,
        handleBackButton = false,
        childFactory = { entry, ctx ->
            DefaultEntryMenuComponent(
                componentContext = ctx,
                entry = entry,
                onDismissRequested = { entryMenuNavigation.dismiss() },
            )
        },
    )

    override fun onEntryMenuRequested(entry: HistoryEntry) = entryMenuNavigation.activate(entry)

    @AssistedFactory
    fun interface Factory {
        fun create(
            componentContext: ComponentContext,
            onDismissRequested: () -> Unit,
            onEntrySelected: (HistoryEntry) -> Unit,
        ): DefaultHistoryComponent
    }
}
