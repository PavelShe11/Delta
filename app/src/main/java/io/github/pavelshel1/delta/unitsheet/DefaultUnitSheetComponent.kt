package io.github.pavelshel1.delta.unitsheet

import com.arkivanov.decompose.ComponentContext

class DefaultUnitSheetComponent(
    componentContext: ComponentContext,
    override val fieldKey: FieldKey,
    override val units: List<String>,
    override val currentUnit: String,
    private val onSelectAction: (String) -> Unit,
    private val onDismissAction: () -> Unit,
) : UnitSheetComponent, ComponentContext by componentContext {

    override fun onSelect(unit: String) = onSelectAction(unit)
    override fun onDismiss() = onDismissAction()
}
