package io.github.pavelshel1.delta.unitsheet

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
sealed interface FieldKey : Parcelable {
    data object TStart : FieldKey
    data object TEnd   : FieldKey
    data object PStart : FieldKey
    data object PEnd   : FieldKey
    data object Time   : FieldKey
}
