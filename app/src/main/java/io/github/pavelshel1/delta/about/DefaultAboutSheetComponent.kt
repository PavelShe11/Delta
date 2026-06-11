package io.github.pavelshel1.delta.about

import com.arkivanov.decompose.ComponentContext

class DefaultAboutSheetComponent(
    componentContext: ComponentContext,
    override val appInfo: AppInfo,
    private val onDismissAction: () -> Unit,
) : AboutSheetComponent, ComponentContext by componentContext {

    override fun onDismiss() = onDismissAction()
}