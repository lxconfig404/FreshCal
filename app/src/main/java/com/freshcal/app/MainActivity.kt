package com.freshcal.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

enum class Screen { CALC, HISTORY, TOOLS, SETTINGS }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = getSharedPreferences("freshcal", MODE_PRIVATE)
        val settings = AppSettings(prefs)
        val historyStore = HistoryStore(prefs)

        setContent {
            val dark = resolveDark(settings)
            DisposableEffect(dark) {
                enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.auto(
                        android.graphics.Color.TRANSPARENT,
                        android.graphics.Color.TRANSPARENT
                    ) { dark },
                    navigationBarStyle = SystemBarStyle.auto(
                        android.graphics.Color.TRANSPARENT,
                        android.graphics.Color.TRANSPARENT
                    ) { dark }
                )
                onDispose { }
            }
            FreshCalTheme(dark = dark, settings = settings) {
                AppRoot(settings, historyStore)
            }
        }
    }
}

@Composable
fun AppRoot(settings: AppSettings, historyStore: HistoryStore) {
    var screen by remember { mutableStateOf(Screen.CALC) }
    val calc = remember { CalcState() }

    BackHandler(enabled = screen != Screen.CALC) { screen = Screen.CALC }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()) {
            when (screen) {
                Screen.CALC -> CalculatorScreen(
                    calc = calc,
                    settings = settings,
                    onEval = { e, r -> historyStore.add(e, r) },
                    onOpenHistory = { screen = Screen.HISTORY },
                    onOpenTools = { screen = Screen.TOOLS },
                    onOpenSettings = { screen = Screen.SETTINGS }
                )
                Screen.HISTORY -> HistoryScreen(
                    store = historyStore,
                    onBack = { screen = Screen.CALC },
                    onUse = { item ->
                        calc.expr = item.result
                        calc.done = true
                        calc.error = false
                        screen = Screen.CALC
                    }
                )
                Screen.TOOLS -> ToolsScreen(onBack = { screen = Screen.CALC })
                Screen.SETTINGS -> SettingsScreen(
                    settings = settings,
                    onBack = { screen = Screen.CALC }
                )
            }
        }
    }
}
