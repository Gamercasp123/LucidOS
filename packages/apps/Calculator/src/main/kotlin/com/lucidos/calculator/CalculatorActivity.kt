package com.lucidos.calculator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class CalculatorActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CalculatorApp()
        }
    }
}

@Composable
fun CalculatorApp() {
    var display by remember { mutableStateOf("0") }
    var operand1 by remember { mutableStateOf<Double?>(null) }
    var pendingOperation by remember { mutableStateOf<String?>(null) }
    var shouldResetDisplay by remember { mutableStateOf(false) }

    fun handleDigit(digit: String) {
        if (display == "0" || shouldResetDisplay) {
            display = digit
            shouldResetDisplay = false
        } else {
            display += digit
        }
    }

    fun handleOperation(op: String) {
        val currentValue = display.toDoubleOrNull() ?: 0.0
        if (operand1 == null) {
            operand1 = currentValue
        } else if (pendingOperation != null && !shouldResetDisplay) {
            val result = calculateResult(operand1!!, currentValue, pendingOperation!!)
            display = formatResult(result)
            operand1 = result
        }
        pendingOperation = op
        shouldResetDisplay = true
    }

    fun handleEqual() {
        val val2 = display.toDoubleOrNull() ?: 0.0
        if (operand1 != null && pendingOperation != null) {
            val result = calculateResult(operand1!!, val2, pendingOperation!!)
            display = formatResult(result)
            operand1 = null
            pendingOperation = null
            shouldResetDisplay = true
        }
    }

    fun handleClear() {
        display = "0"
        operand1 = null
        pendingOperation = null
        shouldResetDisplay = false
    }

    MaterialTheme(
        colorScheme = darkColorScheme(
            background = Color(0xFF17171C),
            surface = Color(0xFF2E2F3E),
            primary = Color(0xFFF1A33C)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF17171C))
                .padding(16.dp),
            verticalArrangement = Arrangement.Bottom
        ) {
            // Display Screen
            Text(
                text = display,
                color = Color.White,
                fontSize = 64.sp,
                fontWeight = FontWeight.Light,
                textAlign = TextAlign.End,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            )

            // Buttons Grid
            val buttons = listOf(
                listOf("C", "+/-", "%", "/"),
                listOf("7", "8", "9", "*"),
                listOf("4", "5", "6", "-"),
                listOf("1", "2", "3", "+"),
                listOf("0", ".", "=")
            )

            buttons.forEach { row ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    row.forEach { label ->
                        val isZero = label == "0"
                        val weight = if (isZero) 2f else 1f
                        val containerColor = when (label) {
                            "C", "+/-", "%" -> Color(0xFF4E505F)
                            "/", "*", "-", "+", "=" -> Color(0xFFF1A33C)
                            else -> Color(0xFF2E2F3E)
                        }
                        val contentColor = when (label) {
                            "C", "+/-", "%" -> Color.Black
                            else -> Color.White
                        }

                        Button(
                            onClick = {
                                when (label) {
                                    "C" -> handleClear()
                                    "=" -> handleEqual()
                                    "+", "-", "*", "/" -> handleOperation(label)
                                    else -> {
                                        if (label.toDoubleOrNull() != null || label == ".") {
                                            handleDigit(label)
                                        }
                                    }
                                }
                            },
                            modifier = Modifier
                                .weight(weight)
                                .aspectRatio(if (isZero) 2f else 1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = containerColor,
                                contentColor = contentColor
                            ),
                            shape = CircleShape
                        ) {
                            Text(
                                text = label,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun calculateResult(val1: Double, val2: Double, op: String): Double {
    return when (op) {
        "+" -> val1 + val2
        "-" -> val1 - val2
        "*" -> val1 * val2
        "/" -> if (val2 != 0.0) val1 / val2 else 0.0
        else -> val2
    }
}

private fun formatResult(result: Double): String {
    return if (result % 1.0 == 0.0) {
        result.toInt().toString()
    } else {
        result.toString()
    }
}
