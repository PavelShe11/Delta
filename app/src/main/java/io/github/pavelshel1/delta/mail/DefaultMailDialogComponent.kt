package io.github.pavelshel1.delta.mail

import android.content.Context
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import io.github.pavelshel1.delta.history.HistoryEntry
import io.github.pavelshel1.delta.history.exportHistoryToFile
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.mail.MessagingException
import javax.mail.SendFailedException
import javax.mail.internet.AddressException

@AssistedInject
class DefaultMailDialogComponent(
    @Assisted componentContext: ComponentContext,
    @Assisted private val entries: List<HistoryEntry>,
    @Assisted override val onDismissRequested: () -> Unit,
    private val context: Context,
    private val settingsRepository: SmtpSettingsRepository,
) : MailDialogComponent, ComponentContext by componentContext {

    private val scope = coroutineScope(Dispatchers.Main.immediate)

    override val recipients: List<String> = settingsRepository.recipients()
    override val initialTo: String = recipients.firstOrNull().orEmpty()
    override val initialSubject: String = defaultMailSubject(entries)
    override val attachmentCount: Int = entries.size

    private val _status = MutableValue<MailSendStatus>(MailSendStatus.Idle)
    override val status: Value<MailSendStatus> = _status

    override fun onSendRequested(to: String, subject: String, body: String) {
        if (_status.value == MailSendStatus.Sending) return
        _status.value = MailSendStatus.Sending
        scope.launch {
            _status.value = try {
                withContext(Dispatchers.IO) {
                    val file = exportHistoryToFile(context, entries)
                    SmtpMailSender.send(settingsRepository.settings(), to, subject, body, file)
                }
                settingsRepository.rememberRecipient(to)
                MailSendStatus.Sent
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                MailSendStatus.Failed(e.toUserMessage())
            }
        }
    }

    @AssistedFactory
    fun interface Factory {
        fun create(
            componentContext: ComponentContext,
            entries: List<HistoryEntry>,
            onDismissRequested: () -> Unit,
        ): DefaultMailDialogComponent
    }
}

internal fun defaultMailSubject(
    entries: List<HistoryEntry>,
    nowMs: Long = System.currentTimeMillis(),
): String {
    val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.US)
    val single = entries.singleOrNull()
        ?: return "Акты испытаний на герметичность (${entries.size} шт.) — ${dateFormat.format(Date(nowMs))}"
    val name = single.name?.trim()?.takeIf { it.isNotBlank() }
    return listOfNotNull("Акт испытания на герметичность", name, dateFormat.format(Date(single.timestampMs)))
        .joinToString(" — ")
}

// Порядок веток важен: AddressException и SendFailedException — наследники MessagingException.
private fun Exception.toUserMessage(): String = when (this) {
    is AddressException -> "Некорректный адрес: $message"
    is SendFailedException -> "Сервер отклонил письмо: $message"
    is MessagingException -> "Нет связи с SMTP-сервером: $message"
    else -> message ?: javaClass.simpleName
}
