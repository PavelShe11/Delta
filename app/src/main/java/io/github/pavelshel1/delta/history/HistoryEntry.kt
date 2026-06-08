package io.github.pavelshel1.delta.history

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class HistoryEntry(
    val id: Long,
    val t: String,
    val pStart: String,
    val pEnd: String,
    val pStartBar: String,
    val pEndBar: String,
    val tStartK: String,
    val tEndK: String,
    val result: Double,
    val timestampMs: Long,
) : Parcelable
