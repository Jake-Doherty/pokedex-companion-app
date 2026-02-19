package com.example.pokedex

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import io.github.jan.supabase.postgrest.from
import androidx.compose.ui.text.style.TextAlign

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PokemonDetailScreen(
    pokemonId: Int,
    navController: NavController,
    showBackButton: Boolean = true
) {
    val supabase = SupabaseClientProvider.supabase

    var species by remember { mutableStateOf<PokemonSpecies?>(null) }
    var sprite by remember { mutableStateOf<PokemonSprite?>(null) }
    var types by remember { mutableStateOf<List<String>>(emptyList()) }
    var stats by remember { mutableStateOf<List<Pair<String, Int>>>(emptyList()) }
    var abilities by remember { mutableStateOf<List<Pair<String, Boolean>>>(emptyList()) }
    var pokemonData by remember { mutableStateOf<PokemonData?>(null) }
    var eggGroups by remember { mutableStateOf<List<String>>(emptyList()) }
    var flavorText by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var showShiny by remember { mutableStateOf(false) }
    var evolutionChain by remember { mutableStateOf<List<EvolutionChain>>(emptyList()) }
    var evolutionSpecies by remember { mutableStateOf<Map<Int, String>>(emptyMap()) }
    var evolutionSprites by remember { mutableStateOf<Map<Int, String?>>(emptyMap()) }
    var chainSpecies by remember { mutableStateOf<List<PokemonSpecies>>(emptyList()) }
    var moves by remember { mutableStateOf<List<Triple<String, Int, String>>>(emptyList()) }

    LaunchedEffect(pokemonId) {
        isLoading = true
        error = null
        try {
            species = supabase
                .from("pokemon_v2_pokemonspecies")
                .select { filter { eq("id", pokemonId) } }
                .decodeSingle<PokemonSpecies>()

            sprite = supabase
                .from("pokemon_v2_pokemonsprites")
                .select { filter { eq("pokemon_id", pokemonId) } }
                .decodeSingleOrNull<PokemonSprite>()

            pokemonData = supabase
                .from("pokemon_v2_pokemon")
                .select { filter { eq("id", pokemonId) } }
                .decodeSingleOrNull<PokemonData>()

            val typeNames = supabase
                .from("pokemon_v2_type")
                .select()
                .decodeList<TypeName>()
                .associateBy { it.id }

            types = supabase
                .from("pokemon_v2_pokemontype")
                .select { filter { eq("pokemon_id", pokemonId) } }
                .decodeList<PokemonType>()
                .sortedBy { it.slot }
                .mapNotNull { typeNames[it.typeId]?.name }

            val statNames = supabase
                .from("pokemon_v2_stat")
                .select()
                .decodeList<StatName>()
                .associateBy { it.id }

            stats = supabase
                .from("pokemon_v2_pokemonstat")
                .select { filter { eq("pokemon_id", pokemonId) } }
                .decodeList<PokemonStat>()
                .sortedBy { it.statId }
                .mapNotNull { stat ->
                    val name = statNames[stat.statId]?.name ?: return@mapNotNull null
                    Pair(name, stat.baseStat)
                }

            val abilityNames = supabase
                .from("pokemon_v2_ability")
                .select()
                .decodeList<AbilityName>()
                .associateBy { it.id }

            abilities = supabase
                .from("pokemon_v2_pokemonability")
                .select { filter { eq("pokemon_id", pokemonId) } }
                .decodeList<PokemonAbility>()
                .sortedBy { it.slot }
                .mapNotNull { ability ->
                    val name = abilityNames[ability.abilityId]?.name ?: return@mapNotNull null
                    Pair(name, ability.isHidden)
                }

            val eggGroupNames = supabase
                .from("pokemon_v2_egggroup")
                .select()
                .decodeList<EggGroupName>()
                .associateBy { it.id }

            eggGroups = supabase
                .from("pokemon_v2_pokemonegggroup")
                .select { filter { eq("pokemon_species_id", pokemonId) } }
                .decodeList<PokemonEggGroup>()
                .mapNotNull { eggGroupNames[it.eggGroupId]?.name }

            flavorText = supabase
                .from("pokemon_v2_pokemonspeciesflavortext")
                .select {
                    filter {
                        eq("pokemon_species_id", pokemonId)
                        eq("language_id", 9)
                    }
                    limit(1)
                }
                .decodeList<FlavorText>()
                .firstOrNull()?.flavorText?.replace("\n", " ")?.replace("\u000c", " ")

            val chainId = species?.evolutionChainId
            if (chainId != null) {
                chainSpecies = supabase
                    .from("pokemon_v2_pokemonspecies")
                    .select {
                        filter { eq("evolution_chain_id", chainId) }
                        order("id", io.github.jan.supabase.postgrest.query.Order.ASCENDING)
                    }
                    .decodeList<PokemonSpecies>()

                val speciesIds = chainSpecies.map { it.id }
                evolutionChain = supabase
                    .from("pokemon_v2_pokemonevolution")
                    .select()
                    .decodeList<EvolutionChain>()
                    .filter { it.evolvedSpeciesId in speciesIds }

                evolutionSpecies = chainSpecies.associate { it.id to it.name }

                val evoSpriteList = supabase
                    .from("pokemon_v2_pokemonsprites")
                    .select {
                        filter { isIn("pokemon_id", speciesIds) }
                    }
                    .decodeList<PokemonSprite>()

                evolutionSprites = evoSpriteList.associate { it.pokemonId to it.getFrontDefaultUrl() }
            }

            val moveNames = supabase
                .from("pokemon_v2_move")
                .select()
                .decodeList<MoveName>()
                .associateBy { it.id }

            val learnMethods = supabase
                .from("pokemon_v2_movelearnmethod")
                .select()
                .decodeList<MoveLearnMethod>()
                .associateBy { it.id }

            moves = supabase
                .from("pokemon_v2_pokemonmove")
                .select { filter { eq("pokemon_id", pokemonId) } }
                .decodeList<PokemonMove>()
                .groupBy { Pair(it.moveId, it.learnMethodId) }
                .map { (_, entries) -> entries.maxByOrNull { it.versionGroupId }!! }
                .sortedWith(compareBy({ it.learnMethodId }, { it.level }))
                .mapNotNull { move ->
                    val name = moveNames[move.moveId]?.name ?: return@mapNotNull null
                    val method = learnMethods[move.learnMethodId]?.name ?: "?"
                    Triple(name, move.level, method)
                }

        } catch (e: Exception) {
            error = e.message
        } finally {
            isLoading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
    ) {
        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Transparent)
                .padding(horizontal = 4.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (showBackButton) {
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TerminalDimGreen,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Text(
                text = species?.name?.replaceFirstChar { it.uppercase() } ?: "...",
                fontFamily = SixtyFourFont,
                fontSize = 13.sp,
                color = TerminalGreen,
                modifier = Modifier.padding(start = if (showBackButton) 2.dp else 8.dp)
            )
        }

        HorizontalDivider(color = Color.LightGray, thickness = 0.5.dp)

        when {
            isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    AnimatedLoadingText()
                }
            }
            error != null -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "ERR: $error",
                        fontFamily = SixtyFourFont,
                        fontSize = 9.sp,
                        color = Color.Red
                    )
                }
            }
            species != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Sprite + shiny toggle
                    AsyncImage(
                        model = if (showShiny) sprite?.getFrontShinyUrl() else sprite?.getFrontDefaultUrl(),
                        contentDescription = species!!.name,
                        modifier = Modifier.size(200.dp)
                    )
                    TextButton(
                        onClick = { showShiny = !showShiny },
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                            text = if (showShiny) "✨ SHINY" else "NORMAL",
                            fontFamily = SixtyFourFont,
                            fontSize = 9.sp,
                            color = if (showShiny) Color(0xFFFFD700) else TerminalDimGreen
                        )
                    }

                    // Type badges
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(bottom = 6.dp)
                    ) {
                        types.forEach { type -> TypeBadge(type) }
                    }

                    // Flavor text
                    if (flavorText != null) {
                        Text(
                            text = flavorText!!,
                            fontFamily = SixtyFourFont,
                            fontSize = 8.sp,
                            color = TerminalDimGreen,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 6.dp)
                        )
                    }

                    TerminalDividerLine("POKEDEX DATA")
                    TerminalDetailRow("CAPTURE RATE", "${species!!.captureRate ?: "?"}")
                    TerminalDetailRow("HAPPINESS", "${species!!.baseHappiness ?: "?"}")
                    TerminalDetailRow("HEIGHT", pokemonData?.height?.let { "${it / 10.0}m" } ?: "?")
                    TerminalDetailRow("WEIGHT", pokemonData?.weight?.let { "${it / 10.0}kg" } ?: "?")
                    TerminalDetailRow("BASE EXP", "${pokemonData?.baseExperience ?: "?"}")
                    TerminalDetailRow("LEGENDARY", if (species!!.isLegendary) "YES" else "NO")
                    TerminalDetailRow("MYTHICAL", if (species!!.isMythical) "YES" else "NO")
                    TerminalDetailRow(
                        "GENDER", when (species!!.genderRate) {
                            -1 -> "NONE"
                            0 -> "100% M"
                            1 -> "87.5M/12.5F"
                            2 -> "75M/25F"
                            4 -> "50/50"
                            6 -> "25M/75F"
                            7 -> "12.5M/87.5F"
                            8 -> "100% F"
                            else -> "?"
                        }
                    )
                    TerminalDetailRow(
                        "HATCH",
                        species!!.hatchCounter?.let { "${(it + 1) * 255} STEPS" } ?: "?"
                    )

                    if (abilities.isNotEmpty()) {
                        TerminalDividerLine("ABILITIES")
                        abilities.forEach { (name, isHidden) ->
                            TerminalDetailRow(
                                name.replace("-", " ").uppercase(),
                                if (isHidden) "HIDDEN" else "NORMAL"
                            )
                        }
                    }

                    if (eggGroups.isNotEmpty()) {
                        TerminalDividerLine("EGG GROUPS")
                        TerminalDetailRow(
                            "GROUPS",
                            eggGroups.joinToString(", ") { it.uppercase() }
                        )
                    }

                    if (stats.isNotEmpty()) {
                        TerminalDividerLine("BASE STATS")
                        val maxStat = 255f
                        stats.forEach { (name, value) ->
                            TerminalStatBar(
                                label = name.replace("-", " ").uppercase(),
                                value = value,
                                maxValue = maxStat
                            )
                        }
                        TerminalDetailRow("TOTAL", "${stats.sumOf { it.second }}")
                    }

                    if (evolutionChain.isNotEmpty()) {
                        TerminalDividerLine("EVOLUTION")
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AsyncImage(
                                model = evolutionSprites[chainSpecies.firstOrNull()?.id],
                                contentDescription = null,
                                modifier = Modifier.size(36.dp)
                            )
                            Text(
                                text = chainSpecies.firstOrNull()?.name?.uppercase() ?: "",
                                fontFamily = SixtyFourFont,
                                fontSize = 9.sp,
                                color = TerminalGreen,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }
                        evolutionChain.forEach { evo ->
                            val evoId = evo.evolvedSpeciesId
                            val evoName = evolutionSpecies[evoId]?.uppercase() ?: "#$evoId"
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "→ ",
                                        color = TerminalDimGreen,
                                        fontFamily = SixtyFourFont,
                                        fontSize = 9.sp
                                    )
                                    AsyncImage(
                                        model = evolutionSprites[evoId],
                                        contentDescription = evoName,
                                        modifier = Modifier.size(36.dp)
                                    )
                                    Text(
                                        text = evoName,
                                        fontFamily = SixtyFourFont,
                                        fontSize = 9.sp,
                                        color = TerminalGreen,
                                        modifier = Modifier.padding(start = 4.dp)
                                    )
                                }
                                Text(
                                    text = when {
                                        evo.minLevel != null -> "LV.${evo.minLevel}"
                                        else -> "SPECIAL"
                                    },
                                    fontFamily = SixtyFourFont,
                                    fontSize = 8.sp,
                                    color = TerminalDimGreen
                                )
                            }
                            HorizontalDivider(color = TerminalDivider, thickness = 0.5.dp)
                        }
                    }

                    if (moves.isNotEmpty()) {
                        TerminalDividerLine("MOVES")
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 2.dp)
                        ) {
                            Text("MOVE", fontFamily = SixtyFourFont, fontSize = 8.sp, color = TerminalDimGreen, modifier = Modifier.weight(1f))
                            Text("LV", fontFamily = SixtyFourFont, fontSize = 8.sp, color = TerminalDimGreen, modifier = Modifier.width(24.dp))
                            Text("METHOD", fontFamily = SixtyFourFont, fontSize = 8.sp, color = TerminalDimGreen, modifier = Modifier.width(60.dp))
                        }
                        HorizontalDivider(color = TerminalDivider)
                        moves.forEach { (name, level, method) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = name.replace("-", " ").uppercase(),
                                    fontFamily = SixtyFourFont,
                                    fontSize = 8.sp,
                                    color = TerminalGreen,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = if (level > 0) "$level" else "-",
                                    fontFamily = SixtyFourFont,
                                    fontSize = 8.sp,
                                    color = TerminalGreen,
                                    modifier = Modifier.width(24.dp)
                                )
                                Text(
                                    text = method.replace("-", " ").uppercase(),
                                    fontFamily = SixtyFourFont,
                                    fontSize = 8.sp,
                                    color = TerminalDimGreen,
                                    modifier = Modifier.width(60.dp)
                                )
                            }
                            HorizontalDivider(color = TerminalDivider, thickness = 0.5.dp)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
fun TerminalDividerLine(label: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = TerminalDimGreen,
            thickness = 0.5.dp
        )
        Text(
            text = " $label ",
            fontFamily = SixtyFourFont,
            fontSize = 8.sp,
            color = TerminalDimGreen
        )
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = TerminalDimGreen,
            thickness = 0.5.dp
        )
    }
}

@Composable
fun TerminalDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontFamily = SixtyFourFont,
            fontSize = 8.sp,
            color = TerminalDimGreen
        )
        Text(
            text = value,
            fontFamily = SixtyFourFont,
            fontSize = 8.sp,
            color = TerminalGreen
        )
    }
    HorizontalDivider(color = TerminalDivider, thickness = 0.5.dp)
}

@Composable
fun TerminalStatBar(label: String, value: Int, maxValue: Float) {
    val color = when {
        value < 50  -> Color(0xFFE74C3C)
        value < 80  -> Color(0xFFF39C12)
        value < 100 -> Color(0xFF27AE60)
        else        -> Color(0xFF2980B9)
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontFamily = SixtyFourFont,
            fontSize = 7.sp,
            color = TerminalDimGreen,
            modifier = Modifier.width(72.dp)
        )
        Text(
            text = value.toString(),
            fontFamily = SixtyFourFont,
            fontSize = 7.sp,
            color = TerminalGreen,
            modifier = Modifier.width(24.dp)
        )
        LinearProgressIndicator(
            progress = { value / maxValue },
            modifier = Modifier
                .weight(1f)
                .height(4.dp),
            color = color,
            trackColor = TerminalDivider
        )
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    TerminalDetailRow(label, value)
}

@Composable
fun EmptyDetailScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "SELECT A\nPOKEMON",
            textAlign = TextAlign.Center,
            fontFamily = SixtyFourFont,
            fontSize = 11.sp,
            color = TerminalGreen
        )
    }
}