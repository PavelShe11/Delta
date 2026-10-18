package io.github.pavelshel1.delta.about

interface AboutSheetComponent {
    val appInfo: AppInfo
    val onDismissRequested: () -> Unit
    /** Скрытый вход админа в SMTP-настройки — 7 тапов по чипу «Версия». */
    val onSmtpSettingsRequested: () -> Unit
}
