package io.github.pavelshel1.delta.history

import com.arkivanov.decompose.ComponentContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class DefaultHistoryComponent(
    componentContext: ComponentContext,
    private val onDismissAction: () -> Unit,
) : HistoryComponent, ComponentContext by componentContext {

    private val _entries = MutableStateFlow(
        listOf(
            HistoryEntry(
                id = 1L,
                latex = """\Delta P = \dfrac{100}{4} \times\!\left[1-\dfrac{1\times293}{1\times298}\right]""",
                resultLatex = "0.167",
                timestampMs = System.currentTimeMillis(),
            ),
            HistoryEntry(
                id = 2L,
                latex = """\Delta P = \dfrac{100}{8} \times\!\left[1-\dfrac{1.2\times295}{1.0\times300}\right]""",
                resultLatex = "-0.250",
                timestampMs = System.currentTimeMillis() - 3_600_000,
            ),
        )
    )

    override val entries: StateFlow<List<HistoryEntry>> = _entries

    override fun onDismiss() = onDismissAction()

    override fun onDelete(id: Long) {
        _entries.update { list -> list.filter { it.id != id } }
    }

    override fun onClear() {
        _entries.update { emptyList() }
    }
}
