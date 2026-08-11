package io.github.pavelshel1.delta.history

import org.dhatim.fastexcel.BorderSide
import org.dhatim.fastexcel.BorderStyle
import org.dhatim.fastexcel.Workbook
import org.dhatim.fastexcel.Worksheet
import java.io.OutputStream

object HistoryExcelExporter {

    private const val ABSTRACT_FORMULA =
        "ΔР = [1 – (Ркон.+ Рбар.кон.)х Тнач. / ((Рнач.+ Рбар.нач.)х Ткон.)] х 100 / t "

    private const val DEFAULT_ACT_NAME = "Акт"
    private const val FIRST_COL = 1
    private const val TITLE_LAST_COL = 21

    private const val FRAME_PADDING = 1
    private const val CONTENT_TOP_ROW = FRAME_PADDING

    private const val BODY_ROW_OFFSET = CONTENT_TOP_ROW + 4

    private const val PRESSURE_FORMAT = "0.00"
    private const val RESULT_FORMAT = "0.000"

    private val COLUMN_WIDTHS = mapOf(
        1 to 5.3, 2 to 3.0, 3 to 0.7, 4 to 6.3, 5 to 2.0, 6 to 5.1, 7 to 2.0,
        8 to 5.0, 9 to 2.9, 10 to 6.6, 11 to 2.6, 12 to 5.1, 13 to 1.7, 14 to 6.1,
        15 to 2.7, 16 to 4.6, 17 to 2.0, 18 to 4.3, 19 to 2.1, 20 to 7.0, 21 to 3.6,
    )

    fun export(entries: List<HistoryEntry>, out: OutputStream) {
        val workbook = Workbook(out, "Delta", "1.0")
        val usedNames = mutableSetOf<String>()

        entries.forEachIndexed { index, entry ->
            val sheet = workbook.newWorksheet(sheetName(entry, index, usedNames))
            COLUMN_WIDTHS.forEach { (col, width) -> sheet.width(col, width) }
            writeAct(sheet, entry)
        }

        workbook.finish()
    }

    private fun sheetName(entry: HistoryEntry, index: Int, used: MutableSet<String>): String {
        val base = entry.name?.trim()?.takeIf { it.isNotBlank() } ?: DEFAULT_ACT_NAME
        val sanitized = base.replace(Regex("[\\\\/?*\\[\\]:]"), " ").trim().ifEmpty { DEFAULT_ACT_NAME }

        val candidate = sanitized.take(31)
        if (used.add(candidate)) return candidate

        val suffix = " (${index + 1})"
        val disambiguated = sanitized.take((31 - suffix.length).coerceAtLeast(0)) + suffix
        used.add(disambiguated)
        return disambiguated
    }

    private fun writeAct(sheet: Worksheet, entry: HistoryEntry) {
        writeTitle(sheet, entry)
        sheet.value(BODY_ROW_OFFSET, FIRST_COL, "Падение давления составило:")
        sheet.value(BODY_ROW_OFFSET + 2, FIRST_COL, ABSTRACT_FORMULA)
        writeSubstitutedFormulaRow(sheet, entry)
        writeValueRow(
            sheet, row = BODY_ROW_OFFSET + 6,
            leftLabel = "Рнач.=", leftValue = entry.pStart.toDouble(),
            rightLabel = "Ркон.=", rightValue = entry.pEnd.toDouble(),
            unit = entry.pUnit, isPressure = true,
        )
        writeValueRow(
            sheet, row = BODY_ROW_OFFSET + 8,
            leftLabel = "Рнач. бар.=", leftValue = entry.pStartBar.toDouble(),
            rightLabel = "Ркон. бар.=", rightValue = entry.pEndBar.toDouble(),
            unit = entry.pUnit, isPressure = true,
        )
        writeValueRow(
            sheet, row = BODY_ROW_OFFSET + 10,
            leftLabel = "Тнач.=", leftValue = entry.tStartK.toDouble(),
            rightLabel = "Ткон.=", rightValue = entry.tEndK.toDouble(),
            unit = "К", isPressure = false,
        )

        writeFrame(sheet, lastContentRow = BODY_ROW_OFFSET + 10)
    }

    private fun writeFrame(sheet: Worksheet, lastContentRow: Int) {
        val top = CONTENT_TOP_ROW - FRAME_PADDING
        val left = FIRST_COL - FRAME_PADDING
        val bottom = lastContentRow + FRAME_PADDING
        val right = TITLE_LAST_COL + FRAME_PADDING

        sheet.style(top, left)
            .borderStyle(BorderSide.TOP, BorderStyle.THIN)
            .borderStyle(BorderSide.LEFT, BorderStyle.THIN)
            .set()
        sheet.style(top, right)
            .borderStyle(BorderSide.TOP, BorderStyle.THIN)
            .borderStyle(BorderSide.RIGHT, BorderStyle.THIN)
            .set()
        sheet.style(bottom, left)
            .borderStyle(BorderSide.BOTTOM, BorderStyle.THIN)
            .borderStyle(BorderSide.LEFT, BorderStyle.THIN)
            .set()
        sheet.style(bottom, right)
            .borderStyle(BorderSide.BOTTOM, BorderStyle.THIN)
            .borderStyle(BorderSide.RIGHT, BorderStyle.THIN)
            .set()

        ((left + 1) until right).forEach { col ->
            sheet.style(top, col).borderStyle(BorderSide.TOP, BorderStyle.THIN).set()
            sheet.style(bottom, col).borderStyle(BorderSide.BOTTOM, BorderStyle.THIN).set()
        }
        ((top + 1) until bottom).forEach { row ->
            sheet.style(row, left).borderStyle(BorderSide.LEFT, BorderStyle.THIN).set()
            sheet.style(row, right).borderStyle(BorderSide.RIGHT, BorderStyle.THIN).set()
        }
    }

    private fun writeTitle(sheet: Worksheet, entry: HistoryEntry) {
        val schemeName = entry.name?.trim()?.takeIf { it.isNotBlank() } ?: DEFAULT_ACT_NAME
        listOf("Для акта", "проведения испытания на герметичность", schemeName).forEachIndexed { index, text ->
            val row = CONTENT_TOP_ROW + index
            sheet.value(row, FIRST_COL, text)
            sheet.range(row, FIRST_COL, row, TITLE_LAST_COL).style().merge().bold().horizontalAlignment("center").set()
        }
    }

    private fun writeSubstitutedFormulaRow(sheet: Worksheet, entry: HistoryEntry) {
        val row = BODY_ROW_OFFSET + 4
        val r = row + 1 // 1-based номер строки Excel для ссылок внутри формулы

        sheet.value(row, 1, "ΔР = ")
        sheet.value(row, 2, "[1-")
        sheet.value(row, 3, "(")
        sheet.value(row, 4, entry.pEnd.toDouble())
        sheet.value(row, 5, "+")
        sheet.value(row, 6, entry.pEndBar.toDouble())
        sheet.value(row, 7, ")х")
        sheet.value(row, 8, entry.tStartK.toDouble())
        sheet.value(row, 9, "/ ((")
        sheet.value(row, 10, entry.pStart.toDouble())
        sheet.value(row, 11, "+")
        sheet.value(row, 12, entry.pStartBar.toDouble())
        sheet.value(row, 13, ")х")
        sheet.value(row, 14, entry.tEndK.toDouble())
        sheet.value(row, 15, ")]х")
        sheet.value(row, 16, 100.0)
        sheet.value(row, 17, "/")
        sheet.value(row, 18, entry.t.toDouble())
        sheet.value(row, 19, "=")
        sheet.formula(row, 20, "ROUND((1-(E$r+G$r)*I$r/((K$r+M$r)*O$r))*100/S$r,3)")
        sheet.value(row, 21, "%/ч")

        sheet.range(row, 2, row, 19).style().horizontalAlignment("center").set()
        sheet.style(row, 20).bold().horizontalAlignment("center").format(RESULT_FORMAT).set()
        listOf(4, 6, 10, 12).forEach { col -> sheet.style(row, col).format(PRESSURE_FORMAT).set() }
    }

    private fun writeValueRow(
        sheet: Worksheet, row: Int,
        leftLabel: String, leftValue: Double,
        rightLabel: String, rightValue: Double,
        unit: String, isPressure: Boolean,
    ) {
        sheet.value(row, 4, leftLabel)
        sheet.style(row, 4).horizontalAlignment("right").set()

        sheet.value(row, 6, leftValue)
        val leftStyle = sheet.range(row, 6, row, 7).style().merge().bold().horizontalAlignment("center")
        (if (isPressure) leftStyle.format(PRESSURE_FORMAT) else leftStyle).set()

        sheet.value(row, 8, unit)
        sheet.style(row, 8).horizontalAlignment("left").set()

        sheet.value(row, 14, rightLabel)
        sheet.style(row, 14).horizontalAlignment("right").set()

        sheet.value(row, 15, rightValue)
        val rightStyle = sheet.range(row, 15, row, 16).style().merge().bold().horizontalAlignment("center")
        (if (isPressure) rightStyle.format(PRESSURE_FORMAT) else rightStyle).set()

        sheet.value(row, 17, unit)
        // "К" короче "кгс/см²" и т.п. — под неё достаточно двух колонок, остальным юнитам нужно три.
        val unitEndCol = if (unit == "К") 18 else 19
        sheet.range(row, 17, row, unitEndCol).style().merge().horizontalAlignment("left").set()
    }
}
