package com.freshcal.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import java.math.BigDecimal
import java.math.RoundingMode

private fun fmt(v: Double?): String =
    if (v == null || !v.isFinite()) "—"
    else BigDecimal(v).setScale(2, RoundingMode.HALF_UP).toPlainString()

@Composable
fun ToolsScreen(onBack: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        ScreenHeader(title = "Tools", onBack = onBack)
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ToolCard("Percentage of a number") {
                var p by remember { mutableStateOf("") }
                var v by remember { mutableStateOf("") }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    NumField("Percent (%)", p, { p = it }, Modifier.weight(1f))
                    NumField("Of number", v, { v = it }, Modifier.weight(1f))
                }
                val a = p.toDoubleOrNull()
                val b = v.toDoubleOrNull()
                ResultLine("Result", fmt(if (a != null && b != null) a * b / 100.0 else null), true)
            }

            ToolCard("What percent is X of Y?") {
                var x by remember { mutableStateOf("") }
                var y by remember { mutableStateOf("") }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    NumField("X", x, { x = it }, Modifier.weight(1f))
                    NumField("Y", y, { y = it }, Modifier.weight(1f))
                }
                val a = x.toDoubleOrNull()
                val b = y.toDoubleOrNull()
                val r = if (a != null && b != null && b != 0.0) a / b * 100.0 else null
                ResultLine("Result", if (r == null) "—" else fmt(r) + " %", true)
            }

            ToolCard("Percentage change") {
                var from by remember { mutableStateOf("") }
                var to by remember { mutableStateOf("") }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    NumField("From", from, { from = it }, Modifier.weight(1f))
                    NumField("To", to, { to = it }, Modifier.weight(1f))
                }
                val a = from.toDoubleOrNull()
                val b = to.toDoubleOrNull()
                val r = if (a != null && b != null && a != 0.0) (b - a) / a * 100.0 else null
                val label = when {
                    r == null -> "Change"
                    r > 0 -> "Increase"
                    r < 0 -> "Decrease"
                    else -> "Change"
                }
                ResultLine(label, if (r == null) "—" else fmt(r) + " %", true)
            }

            ToolCard("Discount and tax") {
                var price by remember { mutableStateOf("") }
                var disc by remember { mutableStateOf("") }
                var tax by remember { mutableStateOf("") }
                NumField("Price", price, { price = it }, Modifier.fillMaxWidth())
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    NumField("Discount (%)", disc, { disc = it }, Modifier.weight(1f))
                    NumField("Tax / VAT (%)", tax, { tax = it }, Modifier.weight(1f))
                }
                val p = price.toDoubleOrNull()
                val d = if (disc.isEmpty()) 0.0 else disc.toDoubleOrNull()
                val t = if (tax.isEmpty()) 0.0 else tax.toDoubleOrNull()
                val ok = p != null && d != null && t != null
                val saved = if (ok) p!! * d!! / 100.0 else null
                val after = if (ok) p!! - saved!! else null
                val taxAmt = if (ok) after!! * t!! / 100.0 else null
                val finalPrice = if (ok) after!! + taxAmt!! else null
                ResultLine("You save", fmt(saved))
                ResultLine("Tax", fmt(taxAmt))
                HorizontalDivider()
                ResultLine("Final price", fmt(finalPrice), true)
            }

            ToolCard("Split bill") {
                var bill by remember { mutableStateOf("") }
                var tip by remember { mutableStateOf("") }
                var people by remember { mutableStateOf("") }
                NumField("Bill amount", bill, { bill = it }, Modifier.fillMaxWidth())
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    NumField("Tip (%)", tip, { tip = it }, Modifier.weight(1f))
                    NumField("People", people, { people = it }, Modifier.weight(1f))
                }
                val b = bill.toDoubleOrNull()
                val tp = if (tip.isEmpty()) 0.0 else tip.toDoubleOrNull()
                val n = people.toIntOrNull()
                val ok = b != null && tp != null && n != null && n > 0
                val tipAmt = if (ok) b!! * tp!! / 100.0 else null
                val total = if (ok) b!! + tipAmt!! else null
                val each = if (ok) total!! / n!! else null
                ResultLine("Tip", fmt(tipAmt))
                ResultLine("Total", fmt(total))
                HorizontalDivider()
                ResultLine("Each person pays", fmt(each), true)
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ToolCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            content()
        }
    }
}

@Composable
private fun NumField(
    label: String,
    value: String,
    onChange: (String) -> Unit,
    modifier: Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = { v ->
            if (v.length <= 15 && v.all { it in '0'..'9' || it == '.' }) onChange(v)
        },
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = modifier
    )
}

@Composable
private fun ResultLine(label: String, value: String, strong: Boolean = false) {
    val cs = MaterialTheme.colorScheme
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = cs.onSurfaceVariant)
        Text(
            text = value,
            fontWeight = if (strong) FontWeight.Bold else FontWeight.Normal,
            color = if (strong) cs.primary else cs.onSurface
        )
    }
}
