package io.github.pavelshel1.delta.calc

import com.arkivanov.essenty.statekeeper.StateKeeper
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import io.github.pavelshel1.delta.unitsheet.FieldKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

private object NoOpStateKeeper : StateKeeper {
    override fun <T : Any> consume(key: String, strategy: DeserializationStrategy<T>): T? = null
    override fun <T : Any> register(key: String, strategy: SerializationStrategy<T>, supplier: () -> T?) = Unit
    override fun unregister(key: String) = Unit
    override fun isRegistered(key: String): Boolean = false
}

// Индексы: 0=°C, 1=К
private const val CELSIUS = 0
private const val KELVIN  = 1

// Индексы: 0=МПа, 1=кгс/см²
private const val MPA = 0
private const val KGS = 1

@OptIn(ExperimentalCoroutinesApi::class)
class CalcStoreTest {

    private val dispatcher = StandardTestDispatcher()

    @Before fun setUp()    { Dispatchers.setMain(dispatcher) }
    @After  fun tearDown() { Dispatchers.resetMain() }

    private fun store() = CalcStoreFactory(DefaultStoreFactory()).create(NoOpStateKeeper)

    // --- Начальное состояние ---

    @Test
    fun `initial result is null when temperature fields empty`() = runTest {
        assertNull(store().state.result)
    }

    @Test
    fun `initial tStartText is null`() = runTest {
        assertNull(store().state.tStartText)
    }

    // --- Реактивность: ввод °C пересчитывает К ---

    @Test
    fun `typing celsius updates kelvin reactively`() = runTest {
        val s = store()                          // default: °C (idx=0)
        s.accept(CalcStore.Intent.ChangeTStart("100"))
        advanceUntilIdle()
        assertEquals("373", s.state.tStartKelvin.toDisplayString())
        assertEquals("100", s.state.tStartCelsius.toDisplayString())
    }

    @Test
    fun `typing kelvin updates celsius reactively`() = runTest {
        val s = store()
        s.accept(CalcStore.Intent.SelectUnit(FieldKey.TStart, KELVIN))
        advanceUntilIdle()
        s.accept(CalcStore.Intent.ChangeTStart("373"))
        advanceUntilIdle()
        assertEquals("373", s.state.tStartKelvin.toDisplayString())
        assertEquals("100", s.state.tStartCelsius.toDisplayString())
    }

    // --- Переключение единиц подставляет сохранённое значение ---

    @Test
    fun `switching to kelvin shows stored kelvin value`() = runTest {
        val s = store()
        s.accept(CalcStore.Intent.ChangeTStart("100"))  // вводим 100°C → К=373
        advanceUntilIdle()
        s.accept(CalcStore.Intent.SelectUnit(FieldKey.TStart, KELVIN))
        advanceUntilIdle()
        assertEquals("373", s.state.tStartText.toDisplayString())
    }

    @Test
    fun `switching back to celsius shows stored celsius value`() = runTest {
        val s = store()
        s.accept(CalcStore.Intent.ChangeTStart("100"))
        advanceUntilIdle()
        s.accept(CalcStore.Intent.SelectUnit(FieldKey.TStart, KELVIN))
        advanceUntilIdle()
        s.accept(CalcStore.Intent.SelectUnit(FieldKey.TStart, CELSIUS))
        advanceUntilIdle()
        assertEquals("100", s.state.tStartText.toDisplayString())
    }

    @Test
    fun `editing kelvin then switching to celsius keeps celsius value`() = runTest {
        val s = store()
        s.accept(CalcStore.Intent.SelectUnit(FieldKey.TStart, KELVIN))
        advanceUntilIdle()
        s.accept(CalcStore.Intent.ChangeTStart("300"))  // 300К → °C=27
        advanceUntilIdle()
        s.accept(CalcStore.Intent.SelectUnit(FieldKey.TStart, CELSIUS))
        advanceUntilIdle()
        assertEquals("27", s.state.tStartText.toDisplayString())
    }

    // --- Результат вычисляется по кельвинам ---

    @Test
    fun `result computed using kelvin values`() = runTest {
        val s = store()
        s.accept(CalcStore.Intent.ChangeTStart("20"))  // 20°C → tNK=293
        s.accept(CalcStore.Intent.ChangeTEnd("18"))    // 18°C → tKK=291
        s.accept(CalcStore.Intent.ChangePStart("1"))
        s.accept(CalcStore.Intent.ChangePEnd("1"))
        advanceUntilIdle()
        // abs pressures: pNabs=1+1=2, pKabs=1+1=2
        val expected = 100.0 / 4.0 * (1.0 - 2.0 * 293.0 / (2.0 * 291.0))
        assertEquals(expected, s.state.result!!.toDouble(), 1e-9)
    }

    @Test
    fun `result null when time is zero`() = runTest {
        val s = store()
        s.accept(CalcStore.Intent.ChangeTStart("20"))
        s.accept(CalcStore.Intent.ChangeTEnd("18"))
        s.accept(CalcStore.Intent.ChangePStart("1"))
        s.accept(CalcStore.Intent.ChangePEnd("1"))
        s.accept(CalcStore.Intent.ChangeTime("0"))
        advanceUntilIdle()
        assertNull(s.state.result)
    }

    @Test
    fun `result updates when switching from celsius to kelvin and editing`() = runTest {
        val s = store()
        s.accept(CalcStore.Intent.ChangeTStart("20"))
        s.accept(CalcStore.Intent.ChangeTEnd("18"))
        s.accept(CalcStore.Intent.ChangePStart("1"))
        s.accept(CalcStore.Intent.ChangePEnd("1"))
        advanceUntilIdle()
        s.accept(CalcStore.Intent.SelectUnit(FieldKey.TStart, KELVIN))
        advanceUntilIdle()
        s.accept(CalcStore.Intent.ChangeTStart("300"))
        advanceUntilIdle()
        // abs pressures: pNabs=1+1=2, pKabs=1+1=2
        val expected = 100.0 / 4.0 * (1.0 - 2.0 * 300.0 / (2.0 * 291.0))
        assertEquals(expected, s.state.result!!.toDouble(), 1e-9)
    }

    // --- Синхронизация единиц температуры ---

    @Test
    fun `temperature unit defaults to Celsius (index 0)`() = runTest {
        assertEquals(CELSIUS, store().state.tUnitIdx)
    }

    @Test
    fun `selecting temperature unit on TStart syncs TEnd`() = runTest {
        val s = store()
        s.accept(CalcStore.Intent.SelectUnit(FieldKey.TStart, KELVIN))
        advanceUntilIdle()
        assertEquals(KELVIN, s.state.tUnitIdx)
    }

    @Test
    fun `selecting temperature unit on TEnd syncs TStart`() = runTest {
        val s = store()
        s.accept(CalcStore.Intent.SelectUnit(FieldKey.TEnd, KELVIN))
        advanceUntilIdle()
        assertEquals(KELVIN, s.state.tUnitIdx)
    }

    // --- Синхронизация единиц давления ---

    @Test
    fun `selecting pressure unit on PStart syncs all pressure fields`() = runTest {
        val s = store()
        s.accept(CalcStore.Intent.SelectUnit(FieldKey.PStart, KGS))
        advanceUntilIdle()
        assertEquals(KGS, s.state.pUnitIdx)
    }

    @Test
    fun `selecting pressure unit on PEnd syncs all pressure fields`() = runTest {
        val s = store()
        s.accept(CalcStore.Intent.SelectUnit(FieldKey.PEnd, KGS))
        advanceUntilIdle()
        assertEquals(KGS, s.state.pUnitIdx)
    }

    @Test
    fun `selecting pressure unit on PStartBar syncs all pressure fields`() = runTest {
        val s = store()
        s.accept(CalcStore.Intent.SelectUnit(FieldKey.PStartBar, KGS))
        advanceUntilIdle()
        assertEquals(KGS, s.state.pUnitIdx)
    }

    @Test
    fun `selecting pressure unit on PEndBar syncs all pressure fields`() = runTest {
        val s = store()
        s.accept(CalcStore.Intent.SelectUnit(FieldKey.PEndBar, KGS))
        advanceUntilIdle()
        assertEquals(KGS, s.state.pUnitIdx)
    }

    @Test
    fun `pressure unit defaults to MPa (index 0)`() = runTest {
        assertEquals(MPA, store().state.pUnitIdx)
    }

    @Test
    fun `formula result is unit-independent when all pressure fields use same unit`() = runTest {
        // Вводим в МПа
        val sMpa = store()
        sMpa.accept(CalcStore.Intent.ChangeTStart("20"))
        sMpa.accept(CalcStore.Intent.ChangeTEnd("18"))
        sMpa.accept(CalcStore.Intent.ChangePStart("1"))
        sMpa.accept(CalcStore.Intent.ChangePEnd("0.9"))
        sMpa.accept(CalcStore.Intent.ChangePStartBar("0.101325"))
        sMpa.accept(CalcStore.Intent.ChangePEndBar("0.101325"))
        advanceUntilIdle()
        val resultMpa = sMpa.state.result!!.toDouble()

        // Те же данные, затем переключаем в кгс/см² — конвертация автоматическая
        val sKgs = store()
        sKgs.accept(CalcStore.Intent.ChangeTStart("20"))
        sKgs.accept(CalcStore.Intent.ChangeTEnd("18"))
        sKgs.accept(CalcStore.Intent.ChangePStart("1"))
        sKgs.accept(CalcStore.Intent.ChangePEnd("0.9"))
        sKgs.accept(CalcStore.Intent.ChangePStartBar("0.101325"))
        sKgs.accept(CalcStore.Intent.ChangePEndBar("0.101325"))
        sKgs.accept(CalcStore.Intent.SelectUnit(FieldKey.PStart, KGS))
        advanceUntilIdle()
        val resultKgs = sKgs.state.result!!.toDouble()

        assertEquals(resultMpa, resultKgs, 1e-6)
    }

    // --- filledCount ---

    @Test
    fun `filledCount uses active field text`() = runTest {
        val s = store()
        // Начальное: time=4, pStartBar=1, pEndBar=1 → 3
        assertEquals(3, s.state.filledCount)
        s.accept(CalcStore.Intent.ChangeTStart("20"))
        advanceUntilIdle()
        assertEquals(4, s.state.filledCount)
        s.accept(CalcStore.Intent.ChangeTEnd("18"))
        advanceUntilIdle()
        assertEquals(5, s.state.filledCount)
    }
}
