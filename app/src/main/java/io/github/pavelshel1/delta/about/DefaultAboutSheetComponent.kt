package io.github.pavelshel1.delta.about

import com.arkivanov.decompose.ComponentContext
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject

@AssistedInject
class DefaultAboutSheetComponent(
    @Assisted componentContext: ComponentContext,
    override val appInfo: AppInfo,
    @Assisted override val onDismissRequested: () -> Unit,
    @Assisted override val onSmtpSettingsRequested: () -> Unit,
) : AboutSheetComponent, ComponentContext by componentContext {

    @AssistedFactory
    fun interface Factory {
        fun create(
            componentContext: ComponentContext,
            onDismissRequested: () -> Unit,
            onSmtpSettingsRequested: () -> Unit,
        ): DefaultAboutSheetComponent
    }
}
