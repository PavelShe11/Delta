package io.github.pavelshel1.delta.mail

import io.github.pavelshel1.delta.history.EXPORT_MIME_TYPE
import java.io.File
import java.util.Date
import java.util.Properties
import javax.mail.Message
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.AddressException
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeBodyPart
import javax.mail.internet.MimeMessage
import javax.mail.internet.MimeMultipart

/** Аналог Send-MailMessage: relay без авторизации и TLS, HTML-тело в UTF-8, одно вложение. */
object SmtpMailSender {

    private const val TIMEOUT_MS = "15000"

    /** Блокирующий вызов — только с Dispatchers.IO. */
    fun send(settings: SmtpSettings, to: String, subject: String, body: String, attachment: File) {
        Transport.send(buildMessage(settings, to, subject, body, attachment))
    }

    internal fun buildMessage(
        settings: SmtpSettings,
        to: String,
        subject: String,
        body: String,
        attachment: File,
    ): MimeMessage {
        val props = Properties().apply {
            put("mail.smtp.host", settings.host)
            put("mail.smtp.port", settings.port.toString())
            put("mail.smtp.connectiontimeout", TIMEOUT_MS)
            put("mail.smtp.timeout", TIMEOUT_MS)
            put("mail.smtp.writetimeout", TIMEOUT_MS)
        }
        return MimeMessage(Session.getInstance(props)).apply {
            setFrom(InternetAddress(settings.from, true))
            setRecipients(Message.RecipientType.TO, parseRecipients(to))
            setSubject(subject, "UTF-8")
            sentDate = Date()
            setContent(MimeMultipart().apply {
                addBodyPart(MimeBodyPart().apply { setText(bodyToHtml(body), "UTF-8", "html") })
                addBodyPart(MimeBodyPart().apply { attachFile(attachment, EXPORT_MIME_TYPE, "base64") })
            })
        }
    }

    /** Несколько адресов через «,» или «;» — как массив To в PowerShell. */
    internal fun parseRecipients(to: String): Array<out InternetAddress> {
        val addresses = InternetAddress.parse(to.replace(';', ','), true).onEach { it.validate() }
        if (addresses.isEmpty()) throw AddressException("Не указан получатель")
        return addresses
    }

    internal fun bodyToHtml(text: String): String =
        text.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\r\n", "\n")
            .replace("\n", "<br>")
}
