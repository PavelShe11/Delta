package io.github.pavelshel1.delta.mail

import com.arkivanov.decompose.value.Value

interface MailDialogComponent {
    val initialTo: String
    val initialSubject: String
    /** История получателей для подсказок, самый свежий — первый. */
    val recipients: List<String>
    val attachmentCount: Int
    val status: Value<MailSendStatus>
    val onDismissRequested: () -> Unit
    fun onSendRequested(to: String, subject: String, body: String)
}

sealed interface MailSendStatus {
    data object Idle : MailSendStatus
    data object Sending : MailSendStatus
    data object Sent : MailSendStatus
    data class Failed(val message: String) : MailSendStatus
}
