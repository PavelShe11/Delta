package io.github.pavelshel1.delta.mail

import io.github.pavelshel1.delta.history.HistoryEntry
import org.junit.Assert.assertEquals
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Locale

class MailDialogTest {

    private val oct12Noon = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.US).parse("12.10.2026 12:00")!!.time

    private fun entry(name: String?, timestampMs: Long) = HistoryEntry(
        id = 1,
        t = "24",
        pStart = "5.2",
        pEnd = "5.1",
        pStartBar = "1",
        pEndBar = "1",
        tStartK = "293",
        tEndK = "295",
        result = 0.68,
        timestampMs = timestampMs,
        name = name,
    )

    @Test
    fun `subject for single named entry contains name and entry date`() {
        assertEquals(
            "Акт испытания на герметичность — Схема №3 Установка УПС — 12.10.2026",
            defaultMailSubject(listOf(entry("Схема №3 Установка УПС", oct12Noon))),
        )
    }

    @Test
    fun `subject for single unnamed entry skips name`() {
        assertEquals(
            "Акт испытания на герметичность — 12.10.2026",
            defaultMailSubject(listOf(entry("  ", oct12Noon))),
        )
    }

    @Test
    fun `subject for several entries shows count and current date`() {
        assertEquals(
            "Акты испытаний на герметичность (3 шт.) — 12.10.2026",
            defaultMailSubject(List(3) { entry(null, 0L) }, nowMs = oct12Noon),
        )
    }

    @Test
    fun `matchingRecipients filters by substring ignoring case and hides exact match`() {
        val all = listOf("ivanov@corp.local", "petrov@corp.local", "IVAnova@corp.local")
        assertEquals(listOf("ivanov@corp.local", "IVAnova@corp.local"), matchingRecipients(all, "iva"))
        assertEquals(all, matchingRecipients(all, ""))
        assertEquals(emptyList<String>(), matchingRecipients(all, "petrov@corp.local"))
    }
}
