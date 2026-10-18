package io.github.pavelshel1.delta.history

import com.arkivanov.decompose.router.slot.ChildSlot
import com.arkivanov.decompose.value.Value
import io.github.pavelshel1.delta.mail.MailDialogComponent

interface HistoryComponent {
    val state: Value<HistoryState>
    val entryMenu: Value<ChildSlot<*, EntryMenuComponent>>
    val deleteConfirm: Value<ChildSlot<*, DeleteConfirmComponent>>
    val mailDialog: Value<ChildSlot<*, MailDialogComponent>>
    /** SMTP настроен администратором — показываем «Отправить по почте». */
    val isMailEnabled: Boolean
    val onDismissRequested: () -> Unit
    fun onEntryTapped(entry: HistoryEntry)
    fun onEntryLongPressed(entry: HistoryEntry)
    fun onSelectionCleared()
    fun onSelectAllToggled()
    fun onDeleteRequested(id: Long)
    fun onDeleteSelectedRequested()
    fun onEntryMenuRequested(entry: HistoryEntry)
    fun onMailRequested(entry: HistoryEntry)
    fun onMailSelectedRequested()
}
