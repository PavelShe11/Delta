package io.github.pavelshel1.delta.history

import org.dhatim.fastexcel.Workbook
import java.io.OutputStream
import java.util.Date

object HistoryExcelExporter {

    private const val CELSIUS_KELVIN_OFFSET = 273.0
    private const val DATE_CELL_FORMAT = "dd.mm.yyyy hh:mm"

    private val HEADERS = listOf(
        "Имя", "Комментарий", "Дата", "Ед.давления",
        "Pнач", "Pб.нач", "Pкон", "Pб.кон",
        "Tнач,°C", "Tкон,°C", "Tнач,K", "Tкон,K",
        "t,ч", "ΔP",
    )

    fun export(entries: List<HistoryEntry>, out: OutputStream) {
        val workbook = Workbook(out, "Delta", "1.0")
        val sheet = workbook.newWorksheet("Перепад давления ΔP")

        HEADERS.forEachIndexed { col, title -> sheet.value(0, col, title) }

        entries.forEachIndexed { index, entry ->
            val row = index + 1
            val r = row + 1

            sheet.value(row, 0, entry.name ?: "")
            sheet.value(row, 1, entry.comment ?: "")
            sheet.value(row, 2, Date(entry.timestampMs))
            sheet.style(row, 2).format(DATE_CELL_FORMAT).set()
            sheet.value(row, 3, entry.pUnit)
            sheet.value(row, 4, entry.pStart.toDouble())
            sheet.value(row, 5, entry.pStartBar.toDouble())
            sheet.value(row, 6, entry.pEnd.toDouble())
            sheet.value(row, 7, entry.pEndBar.toDouble())
            sheet.value(row, 8, entry.tStartK.toDouble() - CELSIUS_KELVIN_OFFSET)
            sheet.value(row, 9, entry.tEndK.toDouble() - CELSIUS_KELVIN_OFFSET)
            sheet.value(row, 12, entry.t.toDouble())

            sheet.formula(row, 10, "273+I$r")
            sheet.formula(row, 11, "273+J$r")
            sheet.formula(row, 13, "ROUND(100/M$r*(1-((G$r+H$r)*K$r)/((E$r+F$r)*L$r)),3)")
        }

        workbook.finish()
    }
}
