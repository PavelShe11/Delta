package io.github.pavelshel1.delta.mail

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test

class SmtpSettingsTest {

    @Test
    fun `pushRecipient puts newest first without duplicates`() {
        val list = pushRecipient(pushRecipient(emptyList(), "a@corp.local"), "b@corp.local")
        assertEquals(listOf("b@corp.local", "a@corp.local"), list)
        assertEquals(listOf("A@corp.local", "b@corp.local"), pushRecipient(list, " A@corp.local "))
    }

    @Test
    fun `pushRecipient caps list at 20 addresses`() {
        val full = (1..20).map { "u$it@corp.local" }
        val updated = pushRecipient(full, "new@corp.local")
        assertEquals(20, updated.size)
        assertEquals("new@corp.local", updated.first())
        assertFalse("u20@corp.local" in updated)
    }

    @Test
    fun `pushRecipient ignores blank input`() {
        assertEquals(listOf("a@corp.local"), pushRecipient(listOf("a@corp.local"), "  "))
    }

    @Test
    fun `parseSmtpSettings trims and accepts valid relay config`() {
        assertEquals(
            SmtpSettings(from = "delta@corp.local", host = "10.0.0.5", port = 25),
            parseSmtpSettings(" delta@corp.local ", " 10.0.0.5 ", "25"),
        )
    }

    @Test
    fun `parseSmtpSettings rejects invalid input`() {
        assertNull(parseSmtpSettings("delta", "10.0.0.5", "25"))
        assertNull(parseSmtpSettings("delta@corp.local", " ", "25"))
        assertNull(parseSmtpSettings("delta@corp.local", "10.0.0.5", ""))
        assertNull(parseSmtpSettings("delta@corp.local", "10.0.0.5", "0"))
        assertNull(parseSmtpSettings("delta@corp.local", "10.0.0.5", "65536"))
    }
}
