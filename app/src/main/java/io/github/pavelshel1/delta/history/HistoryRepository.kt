package io.github.pavelshel1.delta.history

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.github.pavelshel1.delta.db.DeltaDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@SingleIn(AppScope::class)
@Inject
class HistoryRepository(db: DeltaDatabase) {

    private val queries = db.historyEntryQueries

    fun entries(): Flow<List<HistoryEntry>> =
        queries.selectAll()
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { rows -> rows.map { it.toDomain() } }

    fun insert(entry: HistoryEntry): Long = queries.transactionWithResult {
        queries.insert(
            t           = entry.t,
            pStart      = entry.pStart,
            pEnd        = entry.pEnd,
            pStartBar   = entry.pStartBar,
            pEndBar     = entry.pEndBar,
            tStartK     = entry.tStartK,
            tEndK       = entry.tEndK,
            result      = entry.result,
            timestampMs = entry.timestampMs,
            tUnit       = entry.tUnit,
            pUnit       = entry.pUnit,
            name        = entry.name,
            comment     = entry.comment,
        )
        queries.lastInsertRowId().executeAsOne()
    }

    fun deleteById(id: Long) = queries.deleteById(id)

    fun deleteAll() = queries.deleteAll()

    fun count(): Long = queries.count().executeAsOne()

    private fun io.github.pavelshel1.delta.db.HistoryEntry.toDomain() = HistoryEntry(
        id          = id,
        t           = t,
        pStart      = pStart,
        pEnd        = pEnd,
        pStartBar   = pStartBar,
        pEndBar     = pEndBar,
        tStartK     = tStartK,
        tEndK       = tEndK,
        result      = result,
        timestampMs = timestampMs,
        tUnit       = tUnit,
        pUnit       = pUnit,
        name        = name,
        comment     = comment,
    )
}
