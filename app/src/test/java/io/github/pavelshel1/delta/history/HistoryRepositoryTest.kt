package io.github.pavelshel1.delta.history

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import io.github.pavelshel1.delta.db.DeltaDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class HistoryRepositoryTest {

    private lateinit var repo: HistoryRepository

    @Before
    fun setUp() {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        DeltaDatabase.Schema.create(driver)
        val db = DeltaDatabase(driver)
        repo = HistoryRepository(db)
    }

    @Test
    fun `insert and observe entry`() = runTest {
        val entry = HistoryEntry(
            id = 0L,
            t = "4", pStart = "1", pEnd = "2",
            pStartBar = "1", pEndBar = "1",
            tStartK = "293", tEndK = "298",
            result = 0.167, timestampMs = 1000L,
        )
        repo.insert(entry)
        val list = repo.entries().first()
        assertEquals(1, list.size)
        assertEquals("4", list[0].t)
        assertEquals(0.167, list[0].result, 1e-9)
    }

    @Test
    fun `delete by id removes entry`() = runTest {
        repo.insert(HistoryEntry(0L, "1", "1", "1", "1", "1", "273", "273", 1.0, 2000L))
        val id = repo.entries().first().first().id
        repo.deleteById(id)
        assertTrue(repo.entries().first().isEmpty())
    }

    @Test
    fun `deleteAll clears table`() = runTest {
        repeat(3) { repo.insert(HistoryEntry(0L, "t", "p", "p", "p", "p", "273", "273", 0.0, it.toLong())) }
        repo.deleteAll()
        assertTrue(repo.entries().first().isEmpty())
    }
}
