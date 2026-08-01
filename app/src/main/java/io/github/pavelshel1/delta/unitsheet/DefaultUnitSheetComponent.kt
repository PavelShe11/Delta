package io.github.pavelshel1.delta.unitsheet

import com.arkivanov.decompose.ComponentContext

class DefaultUnitSheetComponent(
    componentContext: ComponentContext,
    override val fieldKey: FieldKey,
    override val units: List<String>,
    override val currentUnit: String,
    override val onUnitSelected: (String) -> Unit,
    override val onDismissRequested: () -> Unit,
) : UnitSheetComponent, ComponentContext by componentContext
