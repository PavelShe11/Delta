package io.github.pavelshel1.delta.history

import org.dhatim.fastexcel.reader.ReadableWorkbook
import org.dhatim.fastexcel.reader.ReadingOptions
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.time.ZoneId
import java.util.Date

class HistoryExcelExporterTest {

    private val entry = HistoryEntry(
        id = 1L,
        t = "24",
        pStart = "5.2",
        pEnd = "5.1",
        pStartBar = "1",
        pEndBar = "1",
        tStartK = "293",
        tEndK = "295",
        result = 0.68,
        timestampMs = 0L,
        pUnit = "МПа",
        name = "Тест1",
    )

    private fun export(): ByteArray {
        val out = ByteArrayOutputStream()
        HistoryExcelExporter.export(listOf(entry), out)
        return out.toByteArray()
    }

    @Test
    fun `sheet is named after the calculation`() {
        ReadableWorkbook(ByteArrayInputStream(export())).use { wb ->
            assertEquals("Перепад давления ΔP", wb.firstSheet.name)
        }
    }

    @Test
    fun `raw fields are written as plain values`() {
        ReadableWorkbook(ByteArrayInputStream(export())).use { wb ->
            val row = wb.firstSheet.openStream().skip(1).findFirst().get()
            assertEquals("Тест1", row.getCellAsString(0).get())
            assertEquals("МПа", row.getCellAsString(3).get())
            assertEquals(0, row.getCellAsNumber(4).get().compareTo(5.2.toBigDecimal()))
            assertEquals(0, row.getCellAsNumber(8).get().compareTo(20.0.toBigDecimal()))  // 293-273
            assertEquals(0, row.getCellAsNumber(9).get().compareTo(22.0.toBigDecimal()))  // 295-273
            assertEquals(0, row.getCellAsNumber(12).get().compareTo(24.0.toBigDecimal()))
        }
    }

    @Test
    fun `date column is a real Excel date with the expected cell format`() {
        val opts = ReadingOptions(/* withCellFormat = */ true, /* cellInErrorIfParseError = */ false)
        ReadableWorkbook(ByteArrayInputStream(export()), opts).use { wb ->
            val row = wb.firstSheet.openStream().skip(1).findFirst().get()
            val expected = Date(entry.timestampMs).toInstant()
                .atZone(ZoneId.systemDefault()).toLocalDateTime()
            assertEquals(expected, row.getCellAsDate(2).get())
            assertEquals("dd.mm.yyyy hh:mm", row.getCell(2).dataFormatString)
        }
    }

    @Test
    fun `Kelvin and delta P columns are formulas referencing the same row`() {
        ReadableWorkbook(ByteArrayInputStream(export())).use { wb ->
            val row = wb.firstSheet.openStream().skip(1).findFirst().get()
            assertEquals("273+I2", row.getCell(10).formula)
            assertEquals("273+J2", row.getCell(11).formula)
            assertEquals("ROUND(100/M2*(1-((G2+H2)*K2)/((E2+F2)*L2)),3)", row.getCell(13).formula)
        }
    }
}
