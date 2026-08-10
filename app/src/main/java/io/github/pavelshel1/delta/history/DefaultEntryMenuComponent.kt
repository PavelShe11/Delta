package io.github.pavelshel1.delta.history

import com.arkivanov.decompose.ComponentContext

class DefaultEntryMenuComponent(
    componentContext: ComponentContext,
    override val entry: HistoryEntry,
    override val onDismissRequested: () -> Unit,
) : EntryMenuComponent, ComponentContext by componentContext
