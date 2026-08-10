package io.github.pavelshel1.delta.history

interface DeleteConfirmComponent {
    val count: Int
    val onConfirmed: () -> Unit
    val onDismissRequested: () -> Unit
}
