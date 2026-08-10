package io.github.pavelshel1.delta.history

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.util.concurrent.TimeUnit
import kotlin.io.path.createTempDirectory

class HistoryExcelShareTest {

    @Test
    fun `cleanupOldExports removes only files older than 3 days`() {
        val dir = createTempDirectory().toFile()
        val old = File(dir, "old.xlsx").apply { writeText("x") }
        val fresh = File(dir, "fresh.xlsx").apply { writeText("x") }
        val now = System.currentTimeMillis()
        old.setLastModified(now - TimeUnit.DAYS.toMillis(4))
        fresh.setLastModified(now - TimeUnit.DAYS.toMillis(1))

        cleanupOldExports(dir, now)

        assertFalse(old.exists())
        assertTrue(fresh.exists())
    }
}
