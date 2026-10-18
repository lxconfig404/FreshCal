package com.freshcal.app

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.core.graphics.ColorUtils

class Seed(
    val name: String,
    val hue: Float,
    val sat: Float,
    val nHue: Float,
    val nSat: Float,
    val tHue: Float,
    val tSat: Float
)

val SEEDS = listOf(
    Seed("Off-white", 195f, 0.16f, 40f, 0.30f, 38f, 0.75f),
    Seed("Green", 155f, 0.50f, 150f, 0.12f, 195f, 0.45f),
    Seed("Blue", 215f, 0.60f, 215f, 0.15f, 265f, 0.35f),
    Seed("Brown", 25f, 0.40f, 25f, 0.15f, 75f, 0.40f),
    Seed("Pink", 335f, 0.50f, 335f, 0.15f, 20f, 0.55f)
)

const val DYNAMIC = 5

fun dynamicAvailable(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

fun hsl(h: Float, s: Float, l: Float): Color =
    Color(
        ColorUtils.HSLToColor(
            floatArrayOf(((h % 360f) + 360f) % 360f, s.coerceIn(0f, 1f), l.coerceIn(0f, 1f))
        )
    )

fun swatchColor(index: Int): Color {
    val s = SEEDS[index]
    return if (index == 0) hsl(s.nHue, s.nSat, 0.92f) else hsl(s.hue, s.sat, 0.42f)
}

fun buildScheme(seed: Seed, dark: Boolean, pureBlack: Boolean): ColorScheme {
    val h = seed.hue
    val s = seed.sat
    val nh = seed.nHue
    val ns = seed.nSat
    val th = seed.tHue
    val ts = seed.tSat
    if (!dark) {
        return lightColorScheme(
            primary = hsl(h, s, 0.32f),
            onPrimary = Color.White,
            primaryContainer = hsl(h, s, 0.88f),
            onPrimaryContainer = hsl(h, s, 0.11f),
            secondary = hsl(h, s * 0.4f, 0.38f),
            onSecondary = Color.White,
            secondaryContainer = hsl(h, s * 0.5f, 0.88f),
            onSecondaryContainer = hsl(h, s * 0.6f, 0.12f),
            tertiary = hsl(th, ts, 0.34f),
            onTertiary = Color.White,
            tertiaryContainer = hsl(th, ts, 0.88f),
            onTertiaryContainer = hsl(th, ts, 0.12f),
            background = hsl(nh, ns, 0.97f),
            onBackground = hsl(nh, ns, 0.10f),
            surface = hsl(nh, ns, 0.97f),
            onSurface = hsl(nh, ns, 0.10f),
            surfaceVariant = hsl(nh, ns, 0.89f),
            onSurfaceVariant = hsl(nh, ns, 0.30f),
            outline = hsl(nh, ns, 0.46f),
            outlineVariant = hsl(nh, ns, 0.80f),
            surfaceContainerLowest = hsl(nh, ns, 1.0f),
            surfaceContainerLow = hsl(nh, ns, 0.95f),
            surfaceContainer = hsl(nh, ns, 0.93f),
            surfaceContainerHigh = hsl(nh, ns, 0.91f),
            surfaceContainerHighest = hsl(nh, ns, 0.89f)
        )
    }
    val bg = if (pureBlack) 0.0f else 0.08f
    val lowest = if (pureBlack) 0.0f else 0.06f
    val low = if (pureBlack) 0.05f else 0.10f
    val mid = if (pureBlack) 0.08f else 0.12f
    val high = if (pureBlack) 0.11f else 0.16f
    val highest = if (pureBlack) 0.14f else 0.20f
    return darkColorScheme(
        primary = hsl(h, s * 0.85f, 0.76f),
        onPrimary = hsl(h, s, 0.14f),
        primaryContainer = hsl(h, s * 0.8f, 0.26f),
        onPrimaryContainer = hsl(h, s, 0.90f),
        secondary = hsl(h, s * 0.45f, 0.76f),
        onSecondary = hsl(h, s * 0.5f, 0.15f),
        secondaryContainer = hsl(h, s * 0.4f, 0.26f),
        onSecondaryContainer = hsl(h, s * 0.5f, 0.90f),
        tertiary = hsl(th, ts, 0.76f),
        onTertiary = hsl(th, ts, 0.14f),
        tertiaryContainer = hsl(th, ts, 0.26f),
        onTertiaryContainer = hsl(th, ts, 0.90f),
        background = hsl(nh, ns, bg),
        onBackground = hsl(nh, ns, 0.90f),
        surface = hsl(nh, ns, bg),
        onSurface = hsl(nh, ns, 0.90f),
        surfaceVariant = hsl(nh, ns, highest),
        onSurfaceVariant = hsl(nh, ns, 0.78f),
        outline = hsl(nh, ns, 0.60f),
        outlineVariant = hsl(nh, ns, 0.30f),
        surfaceContainerLowest = hsl(nh, ns, lowest),
        surfaceContainerLow = hsl(nh, ns, low),
        surfaceContainer = hsl(nh, ns, mid),
        surfaceContainerHigh = hsl(nh, ns, high),
        surfaceContainerHighest = hsl(nh, ns, highest)
    )
}

@Composable
fun resolveDark(settings: AppSettings): Boolean = when (settings.themeMode) {
    1 -> false
    2 -> true
    else -> isSystemInDarkTheme()
}

@Composable
fun FreshCalTheme(
    dark: Boolean,
    settings: AppSettings,
    content: @Composable () -> Unit
) {
    val ctx = LocalContext.current
    val palette = settings.palette
    val pureBlack = settings.pureBlack
    val scheme = remember(dark, palette, pureBlack) {
        if (palette == DYNAMIC && dynamicAvailable()) {
            if (dark) {
                val base = dynamicDarkColorScheme(ctx)
                if (pureBlack) {
                    base.copy(
                        background = Color.Black,
                        surface = Color.Black,
                        surfaceContainerLowest = Color.Black,
                        surfaceContainerLow = Color(0xFF0D0D0D),
                        surfaceContainer = Color(0xFF141414),
                        surfaceContainerHigh = Color(0xFF1C1C1C),
                        surfaceContainerHighest = Color(0xFF242424)
                    )
                } else {
                    base
                }
            } else {
                dynamicLightColorScheme(ctx)
            }
        } else {
            buildScheme(SEEDS[palette.coerceIn(0, SEEDS.lastIndex)], dark, pureBlack)
        }
    }
    MaterialTheme(colorScheme = scheme, content = content)
}
