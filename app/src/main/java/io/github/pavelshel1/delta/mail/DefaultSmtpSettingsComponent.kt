package io.github.pavelshel1.delta.mail

import com.arkivanov.decompose.ComponentContext

class DefaultSmtpSettingsComponent(
    componentContext: ComponentContext,
    override val initial: SmtpSettings,
    override val onSaveRequested: (SmtpSettings) -> Unit,
    override val onCancelRequested: () -> Unit,
) : SmtpSettingsComponent, ComponentContext by componentContext
