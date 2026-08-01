package io.github.pavelshel1.delta.about

interface AboutSheetComponent {
    val appInfo: AppInfo
    val onDismissRequested: () -> Unit
}
