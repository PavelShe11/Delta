package io.github.pavelshel1.delta.calc

import com.arkivanov.essenty.statekeeper.StateKeeper
import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import io.github.pavelshel1.delta.unitsheet.FieldKey
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

class CalcStoreFactory(
    private val storeFactory: StoreFactory,
) {

    fun create(stateKeeper: StateKeeper): CalcStore =
        object : CalcStore, Store<CalcStore.Intent, CalcState, CalcStore.Label>
        by storeFactory.create(
            name            = STORE_NAME,
            initialState    = stateKeeper.consume(STATE_NAME, CalcStateSnapshot.serializer())
                                  ?.toCalcState() ?: CalcState(),
            executorFactory = ::ExecutorImpl,
            reducer         = ReducerImpl,
        ) {}.also {
            stateKeeper.register(STATE_NAME, CalcStateSnapshot.serializer()) {
                it.state.toSnapshot()
            }
        }

    companion object {
        private const val STORE_NAME = "CalcStore"
        private const val STATE_NAME = "CalcStoreState"
    }

    private sealed interface Msg {
        data class TStartChanged(val celsius: String, val kelvin: String) : Msg
        data class TEndChanged(val celsius: String, val kelvin: String)   : Msg
        data class PStartChanged(val text: String) : Msg
        data class PEndChanged(val text: String)   : Msg
        data class TimeChanged(val text: String)   : Msg
        data class UnitSelected(val fieldKey: FieldKey, val unitIdx: Int) : Msg
        data class EntryLoaded(val entry: CalcEntry) : Msg
        data object RecalcResult : Msg
    }

    private class ExecutorImpl :
        CoroutineExecutor<CalcStore.Intent, Nothing, CalcState, Msg, CalcStore.Label>() {

        private var recalcJob: Job? = null

        private fun scheduleRecalc() {
            recalcJob?.cancel()
            recalcJob = scope.launch {
                delay(500.milliseconds)
                dispatch(Msg.RecalcResult)
            }
        }

        override fun executeIntent(intent: CalcStore.Intent) {
            when (intent) {

                is CalcStore.Intent.ChangeTStart -> {
                    if (state().tStartUnitIdx == 0) {
                        dispatch(Msg.TStartChanged(celsius = intent.text, kelvin = celsiusToKelvin(intent.text)))
                    } else {
                        dispatch(Msg.TStartChanged(celsius = kelvinToCelsius(intent.text), kelvin = intent.text))
                    }
                    scheduleRecalc()
                }

                is CalcStore.Intent.ChangeTEnd -> {
                    if (state().tEndUnitIdx == 0) {
                        dispatch(Msg.TEndChanged(celsius = intent.text, kelvin = celsiusToKelvin(intent.text)))
                    } else {
                        dispatch(Msg.TEndChanged(celsius = kelvinToCelsius(intent.text), kelvin = intent.text))
                    }
                    scheduleRecalc()
                }

                is CalcStore.Intent.ChangePStart -> { dispatch(Msg.PStartChanged(intent.text)); scheduleRecalc() }
                is CalcStore.Intent.ChangePEnd   -> { dispatch(Msg.PEndChanged(intent.text));   scheduleRecalc() }
                is CalcStore.Intent.ChangeTime   -> { dispatch(Msg.TimeChanged(intent.text));   scheduleRecalc() }

                is CalcStore.Intent.SelectUnit ->
                    dispatch(Msg.UnitSelected(intent.fieldKey, intent.unitIdx))

                is CalcStore.Intent.Save -> scope.launch {
                    val s = state()
                    s.result ?: return@launch
                    publish(CalcStore.Label.SavedToHistory)
                }

                is CalcStore.Intent.LoadEntry ->
                    dispatch(Msg.EntryLoaded(intent.entry))
            }
        }
    }

    private object ReducerImpl : Reducer<CalcState, Msg> {
        override fun CalcState.reduce(msg: Msg): CalcState = when (msg) {
            is Msg.TStartChanged -> copy(tStartCelsius = msg.celsius.toBigDecimalOrNull(), tStartKelvin = msg.kelvin.toBigDecimalOrNull())
            is Msg.TEndChanged   -> copy(tEndCelsius   = msg.celsius.toBigDecimalOrNull(), tEndKelvin   = msg.kelvin.toBigDecimalOrNull())
            is Msg.PStartChanged -> copy(pStart = msg.text.toBigDecimalOrNull())
            is Msg.PEndChanged   -> copy(pEnd   = msg.text.toBigDecimalOrNull())
            is Msg.TimeChanged   -> copy(time   = msg.text.toBigDecimalOrNull())

            is Msg.RecalcResult  -> withResult()

            is Msg.UnitSelected -> when (msg.fieldKey) {
                FieldKey.TStart -> copy(tStartUnitIdx = msg.unitIdx)
                FieldKey.TEnd   -> copy(tEndUnitIdx   = msg.unitIdx)
                else            -> this
            }

            is Msg.EntryLoaded -> copy(
                tStartCelsius = msg.entry.tStartCelsius.toBigDecimalOrNull(),
                tStartKelvin  = msg.entry.tStartKelvin.toBigDecimalOrNull(),
                tStartUnitIdx = msg.entry.tStartUnitIdx,
                tEndCelsius   = msg.entry.tEndCelsius.toBigDecimalOrNull(),
                tEndKelvin    = msg.entry.tEndKelvin.toBigDecimalOrNull(),
                tEndUnitIdx   = msg.entry.tEndUnitIdx,
                pStart        = msg.entry.pStart.toBigDecimal(),
                pEnd          = msg.entry.pEnd.toBigDecimal(),
                time          = msg.entry.time.toBigDecimal(),
            ).withResult()
        }
    }
}
