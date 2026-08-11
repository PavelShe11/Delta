package io.github.pavelshel1.delta.history

import org.dhatim.fastexcel.reader.ReadableWorkbook
import org.dhatim.fastexcel.reader.ReadingOptions
import org.dhatim.fastexcel.reader.Row
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.stream.Collectors

class HistoryExcelExporterTest {

    private fun entry(id: Long, name: String?) = HistoryEntry(
        id = id,
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
        name = name,
    )

    private fun export(entries: List<HistoryEntry>): ByteArray {
        val out = ByteArrayOutputStream()
        HistoryExcelExporter.export(entries, out)
        return out.toByteArray()
    }

    // Реализация кладёт в колонку 4 и строковые метки ("Рнач.="), и числа (в блоке значений) —
    // getCellAsString() кидает ExcelReaderException на нестроковой ячейке, поэтому runCatching.
    private fun valueRow(rows: List<Row>, leftLabel: String): Row =
        rows.first { runCatching { it.getCellAsString(4).orElse(null) }.getOrNull() == leftLabel }

    // Формульная строка — единственная в листе, где заполнена колонка 20 формулой; искать по
    // содержимому, а не по номеру строки, чтобы тест не зависел от индексации ридера.
    private fun formulaRow(rows: List<Row>): Row =
        rows.first { it.hasCell(20) && it.getCell(20).formula != null }

    // Первые 3 строки с текстом в колонке 1 — это ровно строки заголовка; искать по содержимому,
    // а не по индексу, чтобы тест не зависел от отступа рамки (пустая строка-паддинг сверху и
    // строка-разделитель после заголовка не имеют значения в колонке 1 и отсеиваются сами).
    private fun titleRows(rows: List<Row>): List<Row> =
        rows.filter { runCatching { it.getCellAsString(1).orElse("") }.getOrDefault("").isNotEmpty() }.take(3)

    @Test
    fun `substituted formula row contains the entry's raw values`() {
        ReadableWorkbook(ByteArrayInputStream(export(listOf(entry(1, "Тест1"))))).use { wb ->
            val row = formulaRow(wb.firstSheet.read())
            assertEquals(0, row.getCellAsNumber(4).get().compareTo(5.1.toBigDecimal()))   // Ркон.
            assertEquals(0, row.getCellAsNumber(6).get().compareTo(1.0.toBigDecimal()))   // Рбар.кон.
            assertEquals(0, row.getCellAsNumber(8).get().compareTo(293.0.toBigDecimal())) // Тнач.
            assertEquals(0, row.getCellAsNumber(10).get().compareTo(5.2.toBigDecimal()))  // Рнач.
            assertEquals(0, row.getCellAsNumber(12).get().compareTo(1.0.toBigDecimal()))  // Рбар.нач.
            assertEquals(0, row.getCellAsNumber(14).get().compareTo(295.0.toBigDecimal())) // Ткон.
            assertEquals(0, row.getCellAsNumber(18).get().compareTo(24.0.toBigDecimal()))  // t
        }
    }

    @Test
    fun `result cell is a live formula referencing the same row`() {
        ReadableWorkbook(ByteArrayInputStream(export(listOf(entry(1, "Тест1"))))).use { wb ->
            val row = formulaRow(wb.firstSheet.read())
            assertEquals("ROUND((1-(E10+G10)*I10/((K10+M10)*O10))*100/S10,3)", row.getCell(20).formula)
        }
    }

    @Test
    fun `value block uses the entry's pressure unit, not a hardcoded one`() {
        ReadableWorkbook(ByteArrayInputStream(export(listOf(entry(1, "Тест1"))))).use { wb ->
            val rows = wb.firstSheet.read()

            val pStartRow = valueRow(rows, "Рнач.=")
            assertEquals(0, pStartRow.getCellAsNumber(6).get().compareTo(5.2.toBigDecimal()))
            assertEquals("МПа", pStartRow.getCellAsString(8).get())

            val tStartRow = valueRow(rows, "Тнач.=")
            assertEquals(0, tStartRow.getCellAsNumber(6).get().compareTo(293.0.toBigDecimal()))
            assertEquals("К", tStartRow.getCellAsString(8).get())
        }
    }

    @Test
    fun `one sheet is created per entry, named after the entry`() {
        ReadableWorkbook(ByteArrayInputStream(export(listOf(entry(1, "Тест1"), entry(2, "Тест2"))))).use { wb ->
            val names = wb.sheets.map { it.name }.collect(Collectors.toList())
            assertEquals(listOf("Тест1", "Тест2"), names)
        }
    }

    @Test
    fun `duplicate or blank names get disambiguated sheet names`() {
        val entries = listOf(entry(1, "Тест"), entry(2, "Тест"), entry(3, null))
        ReadableWorkbook(ByteArrayInputStream(export(entries))).use { wb ->
            val names = wb.sheets.map { it.name }.collect(Collectors.toList())
            assertEquals(listOf("Тест", "Тест (2)", "Акт"), names)
        }
    }

    @Test
    fun `sheet starts with the act title block, scheme line named after the entry`() {
        ReadableWorkbook(ByteArrayInputStream(export(listOf(entry(1, "Схема №3"))))).use { wb ->
            val title = titleRows(wb.firstSheet.read())
            assertEquals("Для акта", title[0].getCellAsString(1).get())
            assertEquals("проведения испытания на герметичность", title[1].getCellAsString(1).get())
            assertEquals("Схема №3", title[2].getCellAsString(1).get())
        }
    }

    @Test
    fun `title falls back to the default act name when the entry has none`() {
        ReadableWorkbook(ByteArrayInputStream(export(listOf(entry(1, null))))).use { wb ->
            val title = titleRows(wb.firstSheet.read())
            assertEquals("Акт", title[2].getCellAsString(1).get())
        }
    }

    @Test
    fun `pressure values keep 2 decimals and the result keeps 3, unlike the unformatted Kelvin cells`() {
        val opts = ReadingOptions(/* withCellFormat = */ true, /* cellInErrorIfParseError = */ false)
        ReadableWorkbook(ByteArrayInputStream(export(listOf(entry(1, "Тест1")))), opts).use { wb ->
            val rows = wb.firstSheet.read()

            val formula = formulaRow(rows)
            assertEquals("0.00", formula.getCell(4).dataFormatString)  // Ркон.
            assertEquals("0.000", formula.getCell(20).dataFormatString) // результат

            val pStartRow = valueRow(rows, "Рнач.=")
            assertEquals("0.00", pStartRow.getCell(6).dataFormatString)

            val tStartRow = valueRow(rows, "Тнач.=")
            assertEquals(null, tStartRow.getCell(6).dataFormatString)
        }
    }
}
