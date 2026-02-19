package com.example.pokedex

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.jan.supabase.postgrest.from
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi

val typeChart: Map<String, Map<String, Float>> = mapOf(
    "normal"   to mapOf("rock" to 0.5f, "ghost" to 0f, "steel" to 0.5f),
    "fire"     to mapOf("fire" to 0.5f, "water" to 0.5f, "grass" to 2f, "ice" to 2f, "bug" to 2f, "rock" to 0.5f, "dragon" to 0.5f, "steel" to 2f),
    "water"    to mapOf("fire" to 2f, "water" to 0.5f, "grass" to 0.5f, "ground" to 2f, "rock" to 2f, "dragon" to 0.5f),
    "electric" to mapOf("water" to 2f, "electric" to 0.5f, "grass" to 0.5f, "ground" to 0f, "flying" to 2f, "dragon" to 0.5f),
    "grass"    to mapOf("fire" to 0.5f, "water" to 2f, "grass" to 0.5f, "poison" to 0.5f, "ground" to 2f, "flying" to 0.5f, "bug" to 0.5f, "rock" to 2f, "dragon" to 0.5f, "steel" to 0.5f),
    "ice"      to mapOf("fire" to 0.5f, "water" to 0.5f, "grass" to 2f, "ice" to 0.5f, "ground" to 2f, "flying" to 2f, "dragon" to 2f, "steel" to 0.5f),
    "fighting" to mapOf("normal" to 2f, "ice" to 2f, "poison" to 0.5f, "flying" to 0.5f, "psychic" to 0.5f, "bug" to 0.5f, "rock" to 2f, "ghost" to 0f, "dark" to 2f, "steel" to 2f, "fairy" to 0.5f),
    "poison"   to mapOf("grass" to 2f, "poison" to 0.5f, "ground" to 0.5f, "rock" to 0.5f, "ghost" to 0.5f, "steel" to 0f, "fairy" to 2f),
    "ground"   to mapOf("fire" to 2f, "electric" to 2f, "grass" to 0.5f, "poison" to 2f, "flying" to 0f, "bug" to 0.5f, "rock" to 2f, "steel" to 2f),
    "flying"   to mapOf("electric" to 0.5f, "grass" to 2f, "fighting" to 2f, "bug" to 2f, "rock" to 0.5f, "steel" to 0.5f),
    "psychic"  to mapOf("fighting" to 2f, "poison" to 2f, "psychic" to 0.5f, "dark" to 0f, "steel" to 0.5f),
    "bug"      to mapOf("fire" to 0.5f, "grass" to 2f, "fighting" to 0.5f, "poison" to 0.5f, "flying" to 0.5f, "psychic" to 2f, "ghost" to 0.5f, "dark" to 2f, "steel" to 0.5f, "fairy" to 0.5f),
    "rock"     to mapOf("fire" to 2f, "ice" to 2f, "fighting" to 0.5f, "ground" to 0.5f, "flying" to 2f, "bug" to 2f, "steel" to 0.5f),
    "ghost"    to mapOf("normal" to 0f, "psychic" to 2f, "ghost" to 2f, "dark" to 0.5f),
    "dragon"   to mapOf("dragon" to 2f, "steel" to 0.5f, "fairy" to 0f),
    "dark"     to mapOf("fighting" to 0.5f, "psychic" to 2f, "ghost" to 2f, "dark" to 0.5f, "fairy" to 0.5f),
    "steel"    to mapOf("fire" to 0.5f, "water" to 0.5f, "electric" to 0.5f, "ice" to 2f, "rock" to 2f, "steel" to 0.5f, "fairy" to 2f),
    "fairy"    to mapOf("fire" to 0.5f, "fighting" to 2f, "poison" to 0.5f, "dragon" to 2f, "dark" to 2f, "steel" to 0.5f)
)

fun calculateDefensiveEffectiveness(types: List<String>): Map<String, Float> {
    val allTypes = listOf("normal","fire","water","electric","grass","ice","fighting","poison",
        "ground","flying","psychic","bug","rock","ghost","dragon","dark","steel","fairy")
    return allTypes.associateWith { attackingType ->
        types.fold(1f) { acc, defendingType ->
            acc * (typeChart[attackingType]?.get(defendingType) ?: 1f)
        }
    }.filter { it.value != 1f }
}

@Composable
fun TypeBadgeSmall(type: String, suffix: String = "") {
    Box(
        modifier = Modifier
            .background(typeColor(type), RoundedCornerShape(8.dp))
            .padding(horizontal = 4.dp, vertical = 2.dp)
    ) {
        Text(
            text = type.take(3).uppercase() + suffix,
            color = Color.White,
            fontFamily = SixtyFourFont,
            fontSize = 7.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun CaptureRateScreen(pokemonId: Int?) {
    var captureRate by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(pokemonId) {
        if (pokemonId == null) { captureRate = null; return@LaunchedEffect }
        try {
            val result = SupabaseClientProvider.supabase
                .from("pokemon_v2_pokemonspecies")
                .select { filter { eq("id", pokemonId) } }
                .decodeSingle<PokemonSpecies>()
            captureRate = result.captureRate
        } catch (e: Exception) { captureRate = null }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent),
        contentAlignment = Alignment.Center
    ) {
        if (pokemonId == null || captureRate == null) {
            Text("---", fontFamily = SixtyFourFont, fontSize = 8.sp, color = TerminalDimGreen)
        } else {
            Column(
                modifier = Modifier.fillMaxSize().padding(4.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "CATCH RATE",
                    fontFamily = SixtyFourFont,
                    fontSize = 6.sp,
                    color = TerminalDimGreen
                )
                Spacer(modifier = Modifier.height(2.dp))
                LinearProgressIndicator(
                    progress = { captureRate!! / 255f },
                    modifier = Modifier.fillMaxWidth().height(6.dp),
                    color = when {
                        captureRate!! >= 200 -> Color(0xFF27AE60)
                        captureRate!! >= 100 -> Color(0xFFF39C12)
                        else -> Color(0xFFE74C3C)
                    },
                    trackColor = TerminalDivider
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${captureRate!!}/255",
                    fontFamily = SixtyFourFont,
                    fontSize = 7.sp,
                    color = TerminalGreen
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TypeEffectivenessScreen(pokemonId: Int?, page: TypeEffectivenessPage = TypeEffectivenessPage.WEAK) {
    var types by remember { mutableStateOf<List<String>>(emptyList()) }

    LaunchedEffect(pokemonId) {
        if (pokemonId == null) { types = emptyList(); return@LaunchedEffect }
        try {
            val typeNames = SupabaseClientProvider.supabase
                .from("pokemon_v2_type")
                .select()
                .decodeList<TypeName>()
                .associateBy { it.id }

            types = SupabaseClientProvider.supabase
                .from("pokemon_v2_pokemontype")
                .select { filter { eq("pokemon_id", pokemonId) } }
                .decodeList<PokemonType>()
                .sortedBy { it.slot }
                .mapNotNull { typeNames[it.typeId]?.name }
        } catch (e: Exception) { types = emptyList() }
    }

    val effectiveness = remember(types) { calculateDefensiveEffectiveness(types) }
    val weaknesses = effectiveness.filter { it.value > 1f }.entries.sortedByDescending { it.value }
    val resistances = effectiveness.filter { it.value < 1f && it.value > 0f }.entries.sortedBy { it.value }
    val immunities = effectiveness.filter { it.value == 0f }.entries

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .padding(3.dp)
    ) {
        if (pokemonId == null || types.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("---", fontFamily = SixtyFourFont, fontSize = 8.sp, color = TerminalDimGreen)
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                // Page label
                val (label, labelColor, entries) = when (page) {
                    TypeEffectivenessPage.WEAK   -> Triple("WEAK", Color(0xFFE74C3C), weaknesses)
                    TypeEffectivenessPage.RESIST -> Triple("RESIST", Color(0xFF27AE60), resistances)
                    TypeEffectivenessPage.IMMUNE -> Triple("IMMUNE", TerminalDimGreen, immunities.toList())
                }

                Text(
                    text = label,
                    fontFamily = SixtyFourFont,
                    fontSize = 7.sp,
                    color = labelColor
                )

                if (entries.isEmpty()) {
                    Text(
                        text = "NONE",
                        fontFamily = SixtyFourFont,
                        fontSize = 7.sp,
                        color = TerminalDimGreen
                    )
                } else {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        entries.forEach { (type, mult) ->
                            TypeBadgeSmall(
                                type,
                                if (page == TypeEffectivenessPage.WEAK && mult == 4f) "x4" else ""
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PokemonIdScreen(pokemonId: Int?) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .padding(4.dp)
    ) {
        // "NO." anchored to top-left
        Text(
            text = "NO.",
            fontFamily = SixtyFourFont,
            fontSize = 9.sp,
            color = TerminalDimGreen,
            modifier = Modifier.align(Alignment.TopStart)
        )
        // ID number centered in the remaining space
        Text(
            text = pokemonId?.toString()?.padStart(4, '0') ?: "----",
            fontFamily = SixtyFourFont,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = TerminalGreen,
            textAlign = TextAlign.Center,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

@Composable
fun StatusIndicatorScreen(label: String, status: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .padding(4.dp)
    ) {
        Text(
            text = label,
            fontFamily = SixtyFourFont,
            fontSize = 7.sp,
            color = TerminalDimGreen,
            modifier = Modifier.align(Alignment.TopStart)
        )
        Text(
            text = status,
            fontFamily = SixtyFourFont,
            fontSize = 9.sp,
            color = if (status == "OFF" || status == "IDLE") TerminalDimGreen else TerminalGreen,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}