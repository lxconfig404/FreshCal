package com.freshcal.app

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.math.BigDecimal
import java.math.MathContext

internal val OPS = setOf('+', '−', '×', '÷')

internal val KEY_ROWS = listOf(
    listOf("C", "⌫", "%", "÷"),
    listOf("7", "8", "9", "×"),
    listOf("4", "5", "6", "−"),
    listOf("1", "2", "3", "+"),
    listOf("00", "0", ".", "=")
)

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
        while (pos < s.length && (s[pos] in '0'..'9' || s[pos] == '.')) pos++
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

internal fun evaluate(e: String): String? {
    val t = e.trimEnd { it in OPS }
    if (t.isEmpty()) return null
    return try {
        formatNumber(Parser(t).parse())
    } catch (ex: Exception) {
        null
    }
}

class CalcState {
    var expr by mutableStateOf("")
    var done by mutableStateOf(false)
    var error by mutableStateOf(false)

    fun preview(): String? {
        if (done || error) return null
        if (!expr.any { it in OPS || it == '%' }) return null
        return evaluate(expr)
    }

    fun press(k: String, onResult: (String, String) -> Unit) {
        error = false
        val cur = expr.takeLastWhile { it in '0'..'9' || it == '.' }
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
                        val t = expr.trimEnd { it in OPS }
                        if (t.drop(1).any { it in OPS } || t.contains('%')) onResult(t, r)
                        expr = r
                        done = true
                    } else {
                        error = true
                    }
                }
            }
            k == "%" -> {
                if (expr.isNotEmpty() && expr.last() in '0'..'9') expr += "%"
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
            k[0] in '0'..'9' -> {
                if (done) expr = ""
                done = false
                val cur2 = expr.takeLastWhile { it in '0'..'9' || it == '.' }
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
}
