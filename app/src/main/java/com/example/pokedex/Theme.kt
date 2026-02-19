package com.example.pokedex

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.animation.core.*
import androidx.compose.runtime.*
import androidx.compose.ui.unit.sp

val SixtyFourFont = FontFamily(Font(R.font.sixtyfour_reg))

val TerminalGreen = Color(0xFF00FF41)
val TerminalBackground = Color(0xFF0A0A0A)
val TerminalDivider = Color(0xFF1A3A1A)
val TerminalDimGreen = Color(0xFF00A328)

fun typeColor(type: String): Color = when (type.lowercase()) {
    "fire"     -> Color(0xFFF08030)
    "water"    -> Color(0xFF6890F0)
    "grass"    -> Color(0xFF78C850)
    "electric" -> Color(0xFFF8D030)
    "ice"      -> Color(0xFF98D8D8)
    "fighting" -> Color(0xFFC03028)
    "poison"   -> Color(0xFFA040A0)
    "ground"   -> Color(0xFFE0C068)
    "flying"   -> Color(0xFFA890F0)
    "psychic"  -> Color(0xFFF85888)
    "bug"      -> Color(0xFFA8B820)
    "rock"     -> Color(0xFFB8A038)
    "ghost"    -> Color(0xFF705898)
    "dragon"   -> Color(0xFF7038F8)
    "dark"     -> Color(0xFF705848)
    "steel"    -> Color(0xFFB8B8D0)
    "fairy"    -> Color(0xFFEE99AC)
    "normal"   -> Color(0xFFA8A878)
    else       -> Color(0xFFAAAAAA)
}

@Composable
fun TypeBadge(type: String) {
    Box(
        modifier = Modifier
            .background(typeColor(type), RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 3.dp)
    ) {
        Text(
            text = type.replaceFirstChar { it.uppercase() },
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun AnimatedLoadingText(fontSize: androidx.compose.ui.unit.TextUnit = 11.sp) {
    var dotCount by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(500)
            dotCount = (dotCount + 1) % 4
        }
    }

    val dots = ".".repeat(dotCount)
    val padding = " ".repeat(3 - dotCount)

    Text(
        text = "LOADING$dots$padding",
        color = TerminalGreen,
        fontFamily = SixtyFourFont,
        fontSize = fontSize
    )
}