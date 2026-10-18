package io.github.pavelshel1.delta.mail

interface SmtpSettingsComponent {
    val initial: SmtpSettings
    val onSaveRequested: (SmtpSettings) -> Unit
    val onCancelRequested: () -> Unit
}
