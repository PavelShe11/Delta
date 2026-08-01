package io.github.pavelshel1.delta.savedialog

import com.arkivanov.decompose.ComponentContext

class DefaultSaveDialogComponent(
    componentContext: ComponentContext,
    override val onSaveRequested: (name: String?, comment: String?) -> Unit,
    override val onCancelRequested: () -> Unit,
) : SaveDialogComponent, ComponentContext by componentContext
