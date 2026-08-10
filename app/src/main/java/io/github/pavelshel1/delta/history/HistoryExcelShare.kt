package io.github.pavelshel1.delta.history

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

private const val EXPORT_MIME_TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
private val EXPORT_MAX_AGE_MS = TimeUnit.DAYS.toMillis(3)

internal fun cleanupOldExports(dir: File, now: Long = System.currentTimeMillis()) {
    dir.listFiles()?.forEach { file ->
        if (now - file.lastModified() > EXPORT_MAX_AGE_MS) file.delete()
    }
}

suspend fun shareHistoryAsExcel(context: Context, entries: List<HistoryEntry>) {
    val uri = withContext(Dispatchers.IO) {
        val exportsDir = File(context.cacheDir, "exports").apply { mkdirs() }
        cleanupOldExports(exportsDir)

        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val file = File(exportsDir, "delta_history_$timestamp.xlsx")
        file.outputStream().use { out -> HistoryExcelExporter.export(entries, out) }

        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }

    val sendIntent = Intent(Intent.ACTION_SEND).apply {
        type = EXPORT_MIME_TYPE
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(sendIntent, "Поделиться историей"))
}
