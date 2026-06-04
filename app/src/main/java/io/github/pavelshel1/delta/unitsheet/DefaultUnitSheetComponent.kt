package io.github.pavelshel1.delta.unitsheet

import com.arkivanov.decompose.ComponentContext

class DefaultUnitSheetComponent(
    componentContext: ComponentContext,
    override val fieldKey: FieldKey,
    override val currentIdx: Int,
    private val onSelectAction: (Int) -> Unit,
    private val onDismissAction: () -> Unit,
) : UnitSheetComponent, ComponentContext by componentContext {

    override fun onSelect(idx: Int) = onSelectAction(idx)
    override fun onDismiss() = onDismissAction()
}
