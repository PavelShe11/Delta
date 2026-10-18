package io.github.pavelshel1.delta.mail

import io.github.pavelshel1.delta.history.EXPORT_MIME_TYPE
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import javax.mail.Message
import javax.mail.internet.AddressException
import javax.mail.internet.MimeMultipart

class SmtpMailSenderTest {

    private val settings = SmtpSettings(from = "delta@corp.local", host = "10.0.0.5", port = 25)

    private fun attachment(): File =
        File.createTempFile("delta_history_", ".xlsx").apply {
            writeBytes(byteArrayOf(1, 2, 3))
            deleteOnExit()
        }

    @Test
    fun `buildMessage targets relay and encodes cyrillic subject and html body with xlsx attachment`() {
        val message = SmtpMailSender.buildMessage(
            settings = settings,
            to = "a@corp.local; b@corp.local",
            subject = "Акт испытания на герметичность",
            body = "Строка 1\nСтрока 2 <важно>",
            attachment = attachment(),
        ).apply { saveChanges() }

        assertEquals("10.0.0.5", message.session.getProperty("mail.smtp.host"))
        assertEquals("25", message.session.getProperty("mail.smtp.port"))
        assertEquals("delta@corp.local", message.from.single().toString())
        assertEquals(
            listOf("a@corp.local", "b@corp.local"),
            message.getRecipients(Message.RecipientType.TO).map { it.toString() },
        )
        assertEquals("Акт испытания на герметичность", message.subject)
        assertTrue(message.getHeader("Subject").single().startsWith("=?UTF-8?"))

        val parts = message.content as MimeMultipart
        assertTrue(parts.getBodyPart(0).isMimeType("text/html"))
        assertEquals("Строка 1<br>Строка 2 &lt;важно&gt;", parts.getBodyPart(0).content)
        assertTrue(parts.getBodyPart(1).isMimeType(EXPORT_MIME_TYPE))
        assertTrue(parts.getBodyPart(1).fileName.endsWith(".xlsx"))
    }

    @Test(expected = AddressException::class)
    fun `parseRecipients rejects address without domain`() {
        SmtpMailSender.parseRecipients("ivanov")
    }

    @Test(expected = AddressException::class)
    fun `parseRecipients rejects blank input`() {
        SmtpMailSender.parseRecipients("  ")
    }

    @Test
    fun `bodyToHtml escapes markup and keeps line breaks`() {
        assertEquals("a &amp; b<br>&lt;c&gt;<br>d", SmtpMailSender.bodyToHtml("a & b\r\n<c>\nd"))
    }
}
