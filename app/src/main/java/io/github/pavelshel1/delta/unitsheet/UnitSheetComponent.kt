package io.github.pavelshel1.delta.unitsheet

interface UnitSheetComponent {
    val fieldKey: FieldKey
    val currentIdx: Int
    fun onSelect(idx: Int)
    fun onDismiss()
}
