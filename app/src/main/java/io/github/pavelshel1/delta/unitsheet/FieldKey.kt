package io.github.pavelshel1.delta.unitsheet

import android.os.Parcelable
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Parcelize
sealed interface FieldKey : Parcelable {
    val varMain: String
    val varSub: String?
    val units: List<String>

    data object TStart : FieldKey {
        @IgnoredOnParcel override val varMain = "T"
        @IgnoredOnParcel override val varSub  = "нач"
        @IgnoredOnParcel override val units   = listOf("°C", "К")
    }
    data object TEnd : FieldKey {
        @IgnoredOnParcel override val varMain = "T"
        @IgnoredOnParcel override val varSub  = "кон"
        @IgnoredOnParcel override val units   = listOf("°C", "К")
    }
    data object PStart : FieldKey {
        @IgnoredOnParcel override val varMain = "P"
        @IgnoredOnParcel override val varSub  = "нач"
        @IgnoredOnParcel override val units   = listOf("МПа", "кПа", "бар", "атм")
    }
    data object PEnd : FieldKey {
        @IgnoredOnParcel override val varMain = "P"
        @IgnoredOnParcel override val varSub  = "кон"
        @IgnoredOnParcel override val units   = listOf("МПа", "кПа", "бар", "атм")
    }
    data object Time : FieldKey {
        @IgnoredOnParcel override val varMain = "t"
        @IgnoredOnParcel override val varSub  = null
        @IgnoredOnParcel override val units   = listOf("ч", "мин", "с")
    }
}
