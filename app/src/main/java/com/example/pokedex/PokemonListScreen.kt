package com.example.pokedex

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController

val regionToGeneration = mapOf(
    "kanto" to 1, "johto" to 2, "hoenn" to 3, "sinnoh" to 4,
    "unova" to 5, "kalos" to 6, "alola" to 7, "galar" to 8, "paldea" to 9
)

val allTypeNames = setOf(
    "normal", "fire", "water", "electric", "grass", "ice", "fighting", "poison",
    "ground", "flying", "psychic", "bug", "rock", "ghost", "dragon", "dark", "steel", "fairy"
)

data class SearchFilters(
    val nameQuery: String = "",
    val dexNumber: Int? = null,
    val types: List<String> = emptyList(),
    val generationId: Int? = null,
    val legendary: Boolean? = null,
    val mythical: Boolean? = null
)

fun parseSearchQuery(query: String): SearchFilters {
    val tokens = query.trim().lowercase().split("\\s+".toRegex())
    var nameQuery = ""
    var dexNumber: Int? = null
    val types = mutableListOf<String>()
    var generationId: Int? = null
    var legendary: Boolean? = null
    var mythical: Boolean? = null

    var i = 0
    while (i < tokens.size) {
        val token = tokens[i]

        // Dex number: #001 or 001
        val cleanedNumber = token.trimStart('#')
        if (cleanedNumber.all { it.isDigit() } && cleanedNumber.isNotEmpty()) {
            dexNumber = cleanedNumber.toIntOrNull()
            i++; continue
        }

        // Generation: gen1-gen9
        if (token.matches(Regex("gen\\d"))) {
            generationId = token.removePrefix("gen").toIntOrNull()
            i++; continue
        }

        // Generation: "gen 1" (two tokens)
        if (token == "gen" && i + 1 < tokens.size) {
            val next = tokens[i + 1].toIntOrNull()
            if (next != null) {
                generationId = next
                i += 2; continue
            }
        }

        // Region names
        if (regionToGeneration.containsKey(token)) {
            generationId = regionToGeneration[token]
            i++; continue
        }

        // Type names
        if (allTypeNames.contains(token)) {
            types.add(token)
            i++; continue
        }

        // Status filters
        if (token == "legendary") { legendary = true; i++; continue }
        if (token == "mythical") { mythical = true; i++; continue }

        // Everything else is a name search
        nameQuery += "$token "
        i++
    }

    return SearchFilters(
        nameQuery = nameQuery.trim(),
        dexNumber = dexNumber,
        types = types,
        generationId = generationId,
        legendary = legendary,
        mythical = mythical
    )
}

fun applyFilters(
    pokemon: List<PokemonSpecies>,
    typesByPokemonId: Map<Int, List<String>>,
    filters: SearchFilters
): List<PokemonSpecies> {
    return pokemon.filter { species ->
        // Dex number
        if (filters.dexNumber != null && species.id != filters.dexNumber) return@filter false

        // Name
        if (filters.nameQuery.isNotBlank() &&
            !species.name.contains(filters.nameQuery, ignoreCase = true)) return@filter false

        // Types — all specified types must be present
        if (filters.types.isNotEmpty()) {
            val speciesTypes = typesByPokemonId[species.id] ?: emptyList()
            if (!filters.types.all { it in speciesTypes }) return@filter false
        }

        // Generation
        if (filters.generationId != null && species.generationId != filters.generationId)
            return@filter false

        // Legendary
        if (filters.legendary == true && !species.isLegendary) return@filter false

        // Mythical
        if (filters.mythical == true && !species.isMythical) return@filter false

        true
    }
}

@Composable
fun PokemonListScreen(
    viewModel: PokemonViewModel = viewModel(),
    navController: NavController,
    onPokemonSelected: ((Int) -> Unit)? = null,
    searchQuery: String = "",
    onSearchQueryChange: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
    ) {
        // Terminal search bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Transparent)
                .padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            if (searchQuery.isEmpty()) {
                Text(
                    text = "SEARCH...",
                    color = TerminalDimGreen,
                    fontFamily = SixtyFourFont,
                    fontSize = 12.sp
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BasicTextField(
                    value = searchQuery,
                    onValueChange = { onSearchQueryChange(it) },
                    singleLine = true,
                    textStyle = TextStyle(
                        color = TerminalGreen,
                        fontFamily = SixtyFourFont,
                        fontSize = 12.sp
                    ),
                    cursorBrush = SolidColor(TerminalGreen),
                    modifier = Modifier.weight(1f)
                )
                if (searchQuery.isNotEmpty()) {
                    Text(
                        text = "✕",
                        color = Color(0xFFE74C3C),
                        fontSize = 12.sp,
                        modifier = Modifier
                            .clickable { onSearchQueryChange("") }
                            .padding(start = 4.dp)
                    )
                }
            }
        }

        HorizontalDivider(color = TerminalDimGreen, thickness = 1.dp)

        when (val state = uiState) {
            is PokemonUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    AnimatedLoadingText()
                }
            }
            is PokemonUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "ERR: ${state.message}",
                        color = Color.Red,
                        fontFamily = SixtyFourFont,
                        fontSize = 10.sp
                    )
                }
            }
            is PokemonUiState.Success -> {
                val filters = remember(searchQuery) { parseSearchQuery(searchQuery) }
                val filtered = remember(filters, state.pokemon, state.typesByPokemonId) {
                    if (searchQuery.isBlank()) state.pokemon
                    else applyFilters(state.pokemon, state.typesByPokemonId, filters)
                }

                if (filtered.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "NO MATCH",
                            color = TerminalDimGreen,
                            fontFamily = SixtyFourFont,
                            fontSize = 11.sp
                        )
                    }
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(filtered, key = { it.id }) { species ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (onPokemonSelected != null) {
                                            onPokemonSelected(species.id)
                                        } else {
                                            navController.navigate("detail/${species.id}")
                                        }
                                    }
                                    .padding(horizontal = 8.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "#${species.id.toString().padStart(4, '0')} ${species.name.lowercase()}",
                                    color = TerminalGreen,
                                    fontFamily = SixtyFourFont,
                                    fontSize = 11.sp,
                                    modifier = Modifier.weight(1f)
                                )
                                if (species.isLegendary || species.isMythical) {
                                    Text(
                                        text = if (species.isLegendary) "★" else "✦",
                                        color = Color(0xFFFFD700),
                                        fontSize = 10.sp
                                    )
                                }
                            }
                            HorizontalDivider(color = TerminalDivider, thickness = 0.5.dp)
                        }
                    }
                }
            }
        }
    }
}