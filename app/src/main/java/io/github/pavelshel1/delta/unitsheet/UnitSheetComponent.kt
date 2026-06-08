package io.github.pavelshel1.delta.unitsheet

interface UnitSheetComponent {
    val fieldKey: FieldKey
    val units: List<String>
    val currentUnit: String
    fun onSelect(unit: String)
    fun onDismiss()
}
