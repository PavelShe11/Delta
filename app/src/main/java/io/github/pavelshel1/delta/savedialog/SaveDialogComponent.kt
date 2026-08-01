package io.github.pavelshel1.delta.savedialog

interface SaveDialogComponent {
    val onSaveRequested: (name: String?, comment: String?) -> Unit
    val onCancelRequested: () -> Unit
}
