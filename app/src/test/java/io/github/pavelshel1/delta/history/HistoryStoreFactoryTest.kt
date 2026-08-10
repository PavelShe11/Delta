package io.github.pavelshel1.delta.history

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.arkivanov.mvikotlin.extensions.coroutines.states
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import io.github.pavelshel1.delta.db.DeltaDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test

// repository.entries() maps its rows on the real Dispatchers.IO, so state updates land
// asynchronously on a real thread. Subscribe to the store's state Flow (which replays the
// current state immediately, then future updates) and wait for a matching one — this avoids
// both a virtual test clock (nothing to advance) and races on a plain snapshot read.
private suspend fun HistoryStore.awaitState(
    timeoutMs: Long = 5000,
    predicate: (HistoryState) -> Boolean,
): HistoryState = withTimeout(timeoutMs) { states.first(predicate) }

@OptIn(ExperimentalCoroutinesApi::class)
class HistoryStoreFactoryTest {

    companion object {
        // MVIKotlin lazily probes the Android main-thread id the first time ANY store is
        // created in the JVM ("[MVIKotlin]: Unable to determine Main thread id" — expected
        // outside Robolectric). That one-time probe has raced with the very first test's
        // own timeout budget when this class ran first in a shared test JVM. Absorb it here,
        // once, outside any single test's timing.
        @BeforeClass
        @JvmStatic
        fun warmUpMviKotlin() {
            Dispatchers.setMain(Dispatchers.Default)
            val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
            DeltaDatabase.Schema.create(driver)
            val store = HistoryStoreFactory(DefaultStoreFactory(), HistoryRepository(DeltaDatabase(driver))).create()
            store.dispose()
            driver.close()
            Dispatchers.resetMain()
        }
    }

    // Each store's executor keeps a perpetual repository.entries().collect{} running
    // (init block in HistoryStoreFactory.ExecutorImpl), and each test opens its own
    // in-memory JDBC SQLite connection — leaving either undisposed leaks real threads
    // and connections across tests, starving the shared pool under a full-suite run.
    private val createdStores = mutableListOf<HistoryStore>()
    private val createdDrivers = mutableListOf<JdbcSqliteDriver>()

    @Before fun setUp()    { Dispatchers.setMain(Dispatchers.Default) }
    @After  fun tearDown() {
        createdStores.forEach { it.dispose() }
        createdStores.clear()
        createdDrivers.forEach { it.close() }
        createdDrivers.clear()
        Dispatchers.resetMain()
    }

    private fun entry(name: String) = HistoryEntry(
        id = 0L,
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

    private fun store(entryCount: Int = 3): HistoryStore {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY).also { createdDrivers += it }
        DeltaDatabase.Schema.create(driver)
        val repository = HistoryRepository(DeltaDatabase(driver))
        repeat(entryCount) { i -> repository.insert(entry("e$i")) }
        return HistoryStoreFactory(DefaultStoreFactory(), repository).create().also { createdStores += it }
    }

    @Test
    fun `entries load from repository`() = runBlocking {
        val s = store(2)
        val state = s.awaitState { it.entries.size == 2 }
        assertEquals(2, state.entries.size)
    }

    @Test
    fun `initial selection is empty`() = runBlocking {
        val s = store()
        val state = s.awaitState { it.entries.isNotEmpty() }
        assertTrue(state.selectedIds.isEmpty())
        assertFalse(state.isSelectionMode)
    }

    @Test
    fun `long press enters selection mode with the pressed entry selected`() = runBlocking {
        val s = store()
        val loaded = s.awaitState { it.entries.isNotEmpty() }
        val entry = loaded.entries.first()

        s.accept(HistoryStore.Intent.EntryLongPressed(entry))
        val state = s.awaitState { it.selectedIds.isNotEmpty() }

        assertEquals(listOf(entry.id), state.selectedIds)
        assertTrue(state.isSelectionMode)
    }

    @Test
    fun `tapping toggles selection on and off`() = runBlocking {
        val s = store()
        val loaded = s.awaitState { it.entries.size >= 2 }
        val first = loaded.entries[0]
        val second = loaded.entries[1]
        s.accept(HistoryStore.Intent.EntryLongPressed(first))
        s.awaitState { it.selectedIds.isNotEmpty() }

        s.accept(HistoryStore.Intent.EntryToggled(second))
        val bothSelected = s.awaitState { it.selectedIds.size == 2 }
        assertEquals(setOf(first.id, second.id), bothSelected.selectedIds.toSet())

        s.accept(HistoryStore.Intent.EntryToggled(second))
        val oneSelected = s.awaitState { it.selectedIds.size == 1 }
        assertEquals(listOf(first.id), oneSelected.selectedIds)
    }

    @Test
    fun `selection cleared exits selection mode`() = runBlocking {
        val s = store()
        val loaded = s.awaitState { it.entries.isNotEmpty() }
        s.accept(HistoryStore.Intent.EntryLongPressed(loaded.entries.first()))
        s.awaitState { it.selectedIds.isNotEmpty() }

        s.accept(HistoryStore.Intent.SelectionCleared)
        val state = s.awaitState { it.selectedIds.isEmpty() }

        assertTrue(state.selectedIds.isEmpty())
        assertFalse(state.isSelectionMode)
    }

    @Test
    fun `select all toggled selects every entry, toggled again deselects all`() = runBlocking {
        val s = store(3)
        val loaded = s.awaitState { it.entries.size == 3 }
        s.accept(HistoryStore.Intent.EntryLongPressed(loaded.entries.first()))
        s.awaitState { it.selectedIds.isNotEmpty() }

        s.accept(HistoryStore.Intent.SelectAllToggled)
        val allSelected = s.awaitState { it.selectedIds.size == 3 }
        assertEquals(loaded.entries.map { it.id }.toSet(), allSelected.selectedIds.toSet())

        s.accept(HistoryStore.Intent.SelectAllToggled)
        val state = s.awaitState { it.selectedIds.isEmpty() }
        assertFalse(state.isSelectionMode)
    }

    @Test
    fun `delete selected clears selection and removes only the selected entries`() = runBlocking {
        val s = store(3)
        val loaded = s.awaitState { it.entries.size == 3 }
        val (first, second) = loaded.entries
        s.accept(HistoryStore.Intent.EntryLongPressed(first))
        s.awaitState { it.selectedIds.isNotEmpty() }
        s.accept(HistoryStore.Intent.EntryToggled(second))
        s.awaitState { it.selectedIds.size == 2 }

        s.accept(HistoryStore.Intent.DeleteSelected)
        val state = s.awaitState { it.entries.size == 1 }

        assertTrue(state.selectedIds.isEmpty())
        assertEquals(1, state.entries.size)
    }

    @Test
    fun `single delete also drops the entry from selection`() = runBlocking {
        val s = store(2)
        val loaded = s.awaitState { it.entries.size == 2 }
        val entry = loaded.entries.first()
        s.accept(HistoryStore.Intent.EntryLongPressed(entry))
        s.awaitState { it.selectedIds.isNotEmpty() }

        s.accept(HistoryStore.Intent.Delete(entry.id))
        val state = s.awaitState { it.entries.size == 1 }

        assertEquals(1, state.entries.size)
        assertTrue(state.selectedIds.isEmpty())
    }
}
