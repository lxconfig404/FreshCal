package com.freshcal.app

import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private const val HISTORY_PATH =
    "M13,3c-4.97,0 -9,4.03 -9,9H1l3.89,3.89 0.07,0.14L9,12H6c0,-3.87 3.13,-7 7,-7s7,3.13 7,7 -3.13,7 -7,7c-1.93,0 -3.68,-0.79 -4.94,-2.06l-1.42,1.42C8.27,19.99 10.51,21 13,21c4.97,0 9,-4.03 9,-9s-4.03,-9 -9,-9zM12,8v5l4.28,2.54 0.72,-1.21 -3.5,-2.08V8H12z"

private val HistoryIcon: ImageVector by lazy {
    ImageVector.Builder(
        name = "History",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).addPath(
        pathData = PathParser().parsePathString(HISTORY_PATH).toNodes(),
        fill = SolidColor(Color.Black)
    ).build()
}

// ---------------------------------------------------------------- Calculator

@Composable
fun CalculatorScreen(
    calc: CalcState,
    settings: AppSettings,
    onEval: (String, String) -> Unit,
    onOpenHistory: () -> Unit,
    onOpenTools: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val cs = MaterialTheme.colorScheme
    val haptic = LocalHapticFeedback.current
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "FreshCal",
                style = MaterialTheme.typography.titleLarge,
                color = cs.onSurface,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp)
            )
            IconButton(onClick = onOpenHistory) {
                Icon(HistoryIcon, contentDescription = "History", tint = cs.onSurfaceVariant)
            }
            IconButton(onClick = onOpenTools) {
                Icon(Icons.Rounded.Build, contentDescription = "Tools", tint = cs.onSurfaceVariant)
            }
            IconButton(onClick = onOpenSettings) {
                Icon(Icons.Rounded.Settings, contentDescription = "Settings", tint = cs.onSurfaceVariant)
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .pointerInput(Unit) {
                    detectTapGestures(onLongPress = {
                        if (!calc.error && calc.expr.isNotEmpty()) {
                            val text = calc.preview() ?: calc.expr
                            clipboard.setText(AnnotatedString(text))
                            if (Build.VERSION.SDK_INT < 33) {
                                Toast.makeText(context, "Copied", Toast.LENGTH_SHORT).show()
                            }
                        }
                    })
                },
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.End
        ) {
            val len = calc.expr.length
            val size = when {
                len <= 9 -> 60.sp
                len <= 14 -> 46.sp
                len <= 20 -> 36.sp
                else -> 28.sp
            }
            Text(
                text = calc.expr.ifEmpty { "0" },
                fontSize = size,
                fontWeight = FontWeight.Light,
                color = cs.onSurface,
                textAlign = TextAlign.End,
                maxLines = 3
            )
            val second = if (calc.error) "Error" else (calc.preview() ?: "")
            Text(
                text = second,
                fontSize = 26.sp,
                color = if (calc.error) cs.error else cs.primary,
                textAlign = TextAlign.End,
                maxLines = 1,
                modifier = Modifier
                    .padding(top = 4.dp)
                    .height(36.dp)
            )
        }

        Spacer(Modifier.height(12.dp))

        Column(
            modifier = Modifier
                .weight(1.7f)
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            KEY_ROWS.forEach { row ->
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    row.forEach { k ->
                        val isOp = k.length == 1 && k[0] in OPS
                        val bg = when {
                            k == "=" -> cs.primary
                            k == "C" -> cs.tertiaryContainer
                            isOp || k == "%" || k == "⌫" -> cs.secondaryContainer
                            else -> cs.surfaceContainerHigh
                        }
                        val fg = when {
                            k == "=" -> cs.onPrimary
                            k == "C" -> cs.onTertiaryContainer
                            isOp || k == "%" || k == "⌫" -> cs.onSecondaryContainer
                            else -> cs.onSurface
                        }
                        CalcButton(
                            label = k,
                            bg = bg,
                            fg = fg,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            onClick = {
                                if (settings.haptic) {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                }
                                calc.press(k, onEval)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CalcButton(
    label: String,
    bg: Color,
    fg: Color,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(26.dp))
            .background(bg)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = if (label == "=") 34.sp else 28.sp,
            fontWeight = FontWeight.Medium,
            color = fg
        )
    }
}

// ------------------------------------------------------------------- Common

@Composable
fun ScreenHeader(
    title: String,
    onBack: () -> Unit,
    actions: @Composable RowScope.() -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.weight(1f)
        )
        actions()
    }
}

@Composable
fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 16.dp, bottom = 4.dp, start = 8.dp)
    )
}

// ------------------------------------------------------------------ History

@Composable
fun HistoryScreen(
    store: HistoryStore,
    onBack: () -> Unit,
    onUse: (HistoryItem) -> Unit
) {
    val cs = MaterialTheme.colorScheme
    var confirm by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        ScreenHeader(title = "History", onBack = onBack) {
            if (store.items.isNotEmpty()) {
                IconButton(onClick = { confirm = true }) {
                    Icon(Icons.Rounded.Delete, contentDescription = "Clear history")
                }
            }
        }
        if (store.items.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No history yet", color = cs.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(store.items) { item ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(cs.surfaceContainerLow)
                            .clickable { onUse(item) }
                            .padding(16.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = item.expr,
                            color = cs.onSurfaceVariant,
                            fontSize = 16.sp,
                            textAlign = TextAlign.End
                        )
                        Text(
                            text = "= " + item.result,
                            color = cs.onSurface,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.End
                        )
                    }
                }
            }
        }
    }

    if (confirm) {
        AlertDialog(
            onDismissRequest = { confirm = false },
            title = { Text("Clear history?") },
            text = { Text("All saved calculations will be deleted.") },
            confirmButton = {
                TextButton(onClick = {
                    store.clear()
                    confirm = false
                }) { Text("Clear") }
            },
            dismissButton = {
                TextButton(onClick = { confirm = false }) { Text("Cancel") }
            }
        )
    }
}

// ----------------------------------------------------------------- Settings

@Composable
fun OptionRow(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = null)
        Spacer(Modifier.width(12.dp))
        Text(label)
    }
}

@Composable
fun SwitchRow(title: String, subtitle: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onChange(!checked) }
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title)
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(Modifier.width(12.dp))
        Switch(checked = checked, onCheckedChange = onChange)
    }
}

@Composable
fun SettingsScreen(settings: AppSettings, onBack: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    val ctx = LocalContext.current

    Column(modifier = Modifier.fillMaxSize()) {
        ScreenHeader(title = "Settings", onBack = onBack)
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 4.dp)
        ) {
            SectionTitle("Appearance")
            OptionRow("System default", settings.themeMode == 0) { settings.updateThemeMode(0) }
            OptionRow("Light", settings.themeMode == 1) { settings.updateThemeMode(1) }
            OptionRow("Dark", settings.themeMode == 2) { settings.updateThemeMode(2) }

            SectionTitle("Color theme")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 4.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                for (i in SEEDS.indices) {
                    Swatch(
                        label = SEEDS[i].name,
                        color = swatchColor(i),
                        selected = settings.palette == i,
                        onClick = { settings.updatePalette(i) }
                    )
                }
                if (dynamicAvailable()) {
                    val dynColor = androidx.compose.material3.dynamicLightColorScheme(ctx).primary
                    Swatch(
                        label = "Dynamic",
                        color = dynColor,
                        selected = settings.palette == DYNAMIC,
                        onClick = { settings.updatePalette(DYNAMIC) }
                    )
                }
            }
            if (dynamicAvailable()) {
                Text(
                    text = "Dynamic follows your wallpaper colors (Android 12+).",
                    style = MaterialTheme.typography.bodySmall,
                    color = cs.onSurfaceVariant,
                    modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                )
            }

            SectionTitle("Dark mode")
            SwitchRow(
                title = "Pure black",
                subtitle = "True black background in dark mode. Off = tinted dark colors.",
                checked = settings.pureBlack,
                onChange = { settings.updatePureBlack(it) }
            )

            SectionTitle("Feedback")
            SwitchRow(
                title = "Vibration",
                subtitle = "Light vibration when a key is pressed.",
                checked = settings.haptic,
                onChange = { settings.updateHaptic(it) }
            )

            SectionTitle("About")
            Text(
                text = "FreshCal 1.1",
                color = cs.onSurfaceVariant,
                modifier = Modifier.padding(start = 8.dp, bottom = 24.dp)
            )
        }
    }
}

@Composable
fun Swatch(label: String, color: Color, selected: Boolean, onClick: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(color)
                .border(
                    border = BorderStroke(
                        width = if (selected) 3.dp else 1.dp,
                        color = if (selected) cs.primary else cs.outlineVariant
                    ),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (selected) {
                Icon(
                    Icons.Rounded.Check,
                    contentDescription = null,
                    tint = if (color.luminanceIsLow()) Color.White else Color.Black
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(text = label, style = MaterialTheme.typography.labelMedium)
    }
}

private fun Color.luminanceIsLow(): Boolean = (red * 0.299f + green * 0.587f + blue * 0.114f) < 0.6f
