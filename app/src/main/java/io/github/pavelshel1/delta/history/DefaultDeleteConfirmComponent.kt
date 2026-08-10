package io.github.pavelshel1.delta.history

import com.arkivanov.decompose.ComponentContext

class DefaultDeleteConfirmComponent(
    componentContext: ComponentContext,
    override val count: Int,
    override val onConfirmed: () -> Unit,
    override val onDismissRequested: () -> Unit,
) : DeleteConfirmComponent, ComponentContext by componentContext
