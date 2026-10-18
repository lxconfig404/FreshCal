package com.freshcal.app

import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class AppSettings(private val prefs: SharedPreferences) {
    // themeMode: 0 = system, 1 = light, 2 = dark
    var themeMode by mutableIntStateOf(prefs.getInt("theme_mode", 0))
        private set

    // palette: 0..4 = built-in colors, 5 = dynamic (wallpaper, Android 12+)
    var palette by mutableIntStateOf(prefs.getInt("palette", 0))
        private set

    var pureBlack by mutableStateOf(prefs.getBoolean("pure_black", true))
        private set

    var haptic by mutableStateOf(prefs.getBoolean("haptic", true))
        private set

    fun updateThemeMode(v: Int) {
        themeMode = v
        prefs.edit().putInt("theme_mode", v).apply()
    }

    fun updatePalette(v: Int) {
        palette = v
        prefs.edit().putInt("palette", v).apply()
    }

    fun updatePureBlack(v: Boolean) {
        pureBlack = v
        prefs.edit().putBoolean("pure_black", v).apply()
    }

    fun updateHaptic(v: Boolean) {
        haptic = v
        prefs.edit().putBoolean("haptic", v).apply()
    }
}

class HistoryItem(val expr: String, val result: String)

class HistoryStore(private val prefs: SharedPreferences) {
    val items = mutableStateListOf<HistoryItem>()

    init {
        val raw = prefs.getString("history", "") ?: ""
        if (raw.isNotEmpty()) {
            raw.split('\n').forEach { line ->
                val p = line.split('\t')
                if (p.size == 2) items.add(HistoryItem(p[0], p[1]))
            }
        }
    }

    fun add(expr: String, result: String) {
        val first = items.firstOrNull()
        if (first != null && first.expr == expr && first.result == result) return
        items.add(0, HistoryItem(expr, result))
        while (items.size > MAX_ITEMS) items.removeAt(items.lastIndex)
        save()
    }

    fun clear() {
        items.clear()
        save()
    }

    private fun save() {
        val text = items.joinToString("\n") { it.expr + "\t" + it.result }
        prefs.edit().putString("history", text).apply()
    }

    companion object {
        const val MAX_ITEMS = 100
    }
}
