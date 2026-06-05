package io.github.pavelshel1.delta

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.defaultComponentContext
import io.github.pavelshel1.delta.root.DefaultRootComponent
import io.github.pavelshel1.delta.root.RootContent
import io.github.pavelshel1.delta.ui.theme.DeltaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
        )

        val root = DefaultRootComponent(
            componentContext = defaultComponentContext(),
        )

        setContent {
            DeltaTheme {
                RootContent(
                    component = root,
                    modifier = Modifier
                        .imePadding()
                        .fillMaxSize(),
                )
            }
        }
    }
}
