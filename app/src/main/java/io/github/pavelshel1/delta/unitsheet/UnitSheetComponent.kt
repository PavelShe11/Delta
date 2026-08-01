package io.github.pavelshel1.delta.unitsheet

interface UnitSheetComponent {
    val fieldKey: FieldKey
    val units: List<String>
    val currentUnit: String
    val onUnitSelected: (String) -> Unit
    val onDismissRequested: () -> Unit
}
