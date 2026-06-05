package io.github.pavelshel1.delta.root

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import io.github.pavelshel1.delta.calc.CalcContent
import io.github.pavelshel1.delta.history.HistoryComponent
import io.github.pavelshel1.delta.history.HistoryContent

@Composable
fun RootContent(component: RootComponent, modifier: Modifier = Modifier) {
    val focusManager = LocalFocusManager.current

    Box(modifier = modifier
        .fillMaxSize()
        .clickable(indication = null, interactionSource = null) { focusManager.clearFocus() }) {

        val historyCount by component.historyCount.subscribeAsState()
        CalcContent(component.calc, historyCount = historyCount, modifier = Modifier.fillMaxSize())

        val slot by component.historySlot.subscribeAsState()
        val active = slot.child?.instance

        val cached = remember { mutableStateOf<HistoryComponent?>(null) }
        if (active != null) cached.value = active

        AnimatedVisibility(
            visible = active != null,
            enter = slideInVertically(animationSpec = tween(durationMillis = 300)) { it },
            exit = slideOutVertically(animationSpec = tween(durationMillis = 250)) { it },
        ) {
            cached.value?.let { HistoryContent(it, modifier = Modifier.fillMaxSize()) }
        }
    }
}
