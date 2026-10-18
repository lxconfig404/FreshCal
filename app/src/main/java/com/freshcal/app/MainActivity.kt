package com.freshcal.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.math.BigDecimal
import java.math.MathContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                CalculatorScreen()
            }
        }
    }
}

private val OPS = setOf('+', '−', '×', '÷')

private class Parser(private val s: String) {
    private var pos = 0

    fun parse(): BigDecimal {
        val v = expr()
        if (pos < s.length) throw IllegalArgumentException("bad input")
        return v
    }

    private fun expr(): BigDecimal {
        var v = term()
        while (pos < s.length && (s[pos] == '+' || s[pos] == '−')) {
            val op = s[pos++]
            val r = term()
            v = if (op == '+') v.add(r) else v.subtract(r)
        }
        return v
    }

    private fun term(): BigDecimal {
        var v = unary()
        while (pos < s.length && (s[pos] == '×' || s[pos] == '÷')) {
            val op = s[pos++]
            val r = unary()
            v = if (op == '×') {
                v.multiply(r)
            } else {
                if (r.signum() == 0) throw ArithmeticException("div by zero")
                v.divide(r, MathContext.DECIMAL64)
            }
        }
        return v
    }

    private fun unary(): BigDecimal {
        if (pos < s.length && s[pos] == '−') {
            pos++
            return unary().negate()
        }
        return number()
    }

    private fun number(): BigDecimal {
        val start = pos
        while (pos < s.length && (s[pos].isDigit() || s[pos] == '.')) pos++
        if (start == pos) throw IllegalArgumentException("number expected")
        var v = BigDecimal(s.substring(start, pos))
        while (pos < s.length && s[pos] == '%') {
            pos++
            v = v.divide(BigDecimal(100))
        }
        return v
    }
}

private fun formatNumber(v: BigDecimal): String {
    val r = if (v.signum() == 0) BigDecimal.ZERO else v.stripTrailingZeros()
    return r.toPlainString().replace('-', '−')
}

private fun evaluate(e: String): String? {
    val t = e.trimEnd { it in OPS }
    if (t.isEmpty()) return null
    return try {
        formatNumber(Parser(t).parse())
    } catch (ex: Exception) {
        null
    }
}

private val KEY_ROWS = listOf(
    listOf("C", "⌫", "%", "÷"),
    listOf("7", "8", "9", "×"),
    listOf("4", "5", "6", "−"),
    listOf("1", "2", "3", "+"),
    listOf("00", "0", ".", "=")
)

@Composable
fun CalculatorScreen() {
    var expr by remember { mutableStateOf("") }
    var done by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf(false) }

    val preview: String? =
        if (!done && !error && expr.any { it in OPS || it == '%' }) evaluate(expr) else null

    fun press(k: String) {
        error = false
        val cur = expr.takeLastWhile { it.isDigit() || it == '.' }
        when {
            k == "C" -> {
                expr = ""
                done = false
            }
            k == "⌫" -> {
                expr = if (done) "" else expr.dropLast(1)
                done = false
            }
            k == "=" -> {
                if (expr.isNotEmpty()) {
                    val r = evaluate(expr)
                    if (r != null) {
                        expr = r
                        done = true
                    } else {
                        error = true
                    }
                }
            }
            k == "%" -> {
                if (expr.isNotEmpty() && expr.last().isDigit()) expr += "%"
                done = false
            }
            k == "." -> {
                if (done) {
                    expr = "0."
                } else if (expr.endsWith("%")) {
                    // ignore
                } else if (cur.contains('.')) {
                    // ignore
                } else if (cur.isEmpty()) {
                    expr += "0."
                } else {
                    expr += "."
                }
                done = false
            }
            k[0].isDigit() -> {
                if (done) expr = ""
                done = false
                val cur2 = expr.takeLastWhile { it.isDigit() || it == '.' }
                if (expr.endsWith("%")) {
                    // ignore
                } else if (expr.length >= 40) {
                    // ignore
                } else if (cur2 == "0") {
                    if (!k.all { it == '0' }) expr = expr.dropLast(1) + k
                } else if (cur2.isEmpty() && k == "00") {
                    expr += "0"
                } else {
                    expr += k
                }
            }
            else -> {
                val op = k[0]
                if (expr.isEmpty()) {
                    if (op == '−') expr = "−"
                } else if (expr == "−") {
                    // ignore
                } else if (expr.last() in OPS) {
                    expr = expr.dropLast(1) + op
                } else {
                    expr += op
                }
                done = false
            }
        }
    }

    val accent = Color(0xFF1E8E6E)
    val accentSoft = Color(0xFFD9F2EA)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF6F6F8))
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = expr.ifEmpty { "0" },
                fontSize = 44.sp,
                color = Color(0xFF1B1B1F),
                textAlign = TextAlign.End,
                maxLines = 3
            )
            Text(
                text = if (error) "Error" else (preview ?: ""),
                fontSize = 26.sp,
                color = if (error) Color(0xFFC0392B) else Color(0xFF7A7A85),
                textAlign = TextAlign.End,
                maxLines = 1,
                modifier = Modifier.height(36.dp)
            )
        }
        Spacer(Modifier.height(16.dp))
        KEY_ROWS.forEach { row ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(76.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                row.forEach { k ->
                    val isOp = k.length == 1 && k[0] in OPS
                    val bg = when {
                        k == "=" -> accent
                        isOp || k == "%" -> accentSoft
                        else -> Color.White
                    }
                    val fg = when {
                        k == "=" -> Color.White
                        k == "C" -> Color(0xFFC0392B)
                        isOp || k == "%" -> accent
                        else -> Color(0xFF1B1B1F)
                    }
                    CalcButton(
                        label = k,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        bg = bg,
                        fg = fg,
                        onClick = { press(k) }
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
fun CalcButton(
    label: String,
    modifier: Modifier,
    bg: Color,
    fg: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bg)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(text = label, fontSize = 28.sp, color = fg)
    }
}
