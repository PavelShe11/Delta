package io.github.pavelshel1.delta.history

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class HistoryEntry(
    val id: Long,
    val latex: String,
    val resultLatex: String,
    val timestampMs: Long,
) : Parcelable
