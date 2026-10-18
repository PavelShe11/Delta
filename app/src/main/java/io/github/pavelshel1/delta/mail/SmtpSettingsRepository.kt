package io.github.pavelshel1.delta.mail

import android.content.Context
import androidx.core.content.edit
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

const val DEFAULT_SMTP_PORT = 25
private const val MAX_RECIPIENTS = 20
private val EMAIL_REGEX = Regex("^[^@\\s]+@[^@\\s]+$")

data class SmtpSettings(
    val from: String = "",
    val host: String = "",
    val port: Int = DEFAULT_SMTP_PORT,
) {
    val isComplete: Boolean get() = from.isNotBlank() && host.isNotBlank()
}

@SingleIn(AppScope::class)
@Inject
class SmtpSettingsRepository(context: Context) {

    private val prefs = context.getSharedPreferences("smtp", Context.MODE_PRIVATE)

    fun settings(): SmtpSettings = SmtpSettings(
        from = prefs.getString(KEY_FROM, null).orEmpty(),
        host = prefs.getString(KEY_HOST, null).orEmpty(),
        port = prefs.getInt(KEY_PORT, DEFAULT_SMTP_PORT),
    )

    val isConfigured: Boolean get() = settings().isComplete

    fun save(settings: SmtpSettings) = prefs.edit {
        putString(KEY_FROM, settings.from)
        putString(KEY_HOST, settings.host)
        putInt(KEY_PORT, settings.port)
    }

    /** Последние получатели, самый свежий — первый. */
    fun recipients(): List<String> =
        prefs.getString(KEY_RECIPIENTS, null).orEmpty().lines().filter { it.isNotBlank() }

    fun rememberRecipient(to: String) = prefs.edit {
        putString(KEY_RECIPIENTS, pushRecipient(recipients(), to).joinToString("\n"))
    }

    private companion object {
        const val KEY_FROM = "from"
        const val KEY_HOST = "host"
        const val KEY_PORT = "port"
        const val KEY_RECIPIENTS = "recipients"
    }
}

internal fun pushRecipient(current: List<String>, to: String, max: Int = MAX_RECIPIENTS): List<String> {
    val value = to.trim()
    if (value.isEmpty()) return current
    return (listOf(value) + current.filterNot { it.equals(value, ignoreCase = true) }).take(max)
}

/** `null` — данные невалидны, сохранять нельзя. */
internal fun parseSmtpSettings(from: String, host: String, port: String): SmtpSettings? {
    val portNumber = port.trim().toIntOrNull()?.takeIf { it in 1..65535 } ?: return null
    if (!EMAIL_REGEX.matches(from.trim()) || host.isBlank()) return null
    return SmtpSettings(from = from.trim(), host = host.trim(), port = portNumber)
}
