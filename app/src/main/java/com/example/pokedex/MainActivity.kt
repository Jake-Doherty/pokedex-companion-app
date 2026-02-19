package com.example.pokedex

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.window.layout.FoldingFeature
import androidx.window.layout.WindowInfoTracker
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val isUnfolded = produceState(initialValue = false) {
                WindowInfoTracker.getOrCreate(this@MainActivity)
                    .windowLayoutInfo(this@MainActivity)
                    .map { info ->
                        info.displayFeatures
                            .filterIsInstance<FoldingFeature>()
                            .any { it.state == FoldingFeature.State.FLAT }
                    }
                    .collect { value = it }
            }.value

            if (isUnfolded) {
                val navController = rememberNavController()
                var appState by remember { mutableStateOf(AppState()) }

                // T9 state
                val t9Map = mapOf(
                    1 to listOf("1", "-", "legendary", "mythical"),
                    2 to listOf("2", "a", "b", "c"),
                    3 to listOf("3", "d", "e", "f"),
                    4 to listOf("4", "g", "h", "i"),
                    5 to listOf("5", "j", "k", "l"),
                    6 to listOf("6", "m", "n", "o"),
                    7 to listOf("7", "p", "q", "r", "s"),
                    8 to listOf("8", "t", "u", "v"),
                    9 to listOf("9", "w", "x", "y", "z"),
                    10 to listOf("0", " ")
                )

                var button10PressTime by remember { mutableStateOf(0L) }
                var button10Job by remember { mutableStateOf<Job?>(null) }
                var lastPressedKey by remember { mutableStateOf<Int?>(null) }
                var cycleIndex by remember { mutableStateOf(0) }
                var pendingChar by remember { mutableStateOf<String?>(null) }
                var searchQuery by remember { mutableStateOf("") }
                val coroutineScope = rememberCoroutineScope()
                var commitJob by remember { mutableStateOf<Job?>(null) }

                val menuItems = MenuItem.entries
                val currentMenuIndex = menuItems.indexOf(appState.selectedMenuItem)

                fun commitPending() {
                    pendingChar?.let { searchQuery += it }
                    pendingChar = null
                    lastPressedKey = null
                    cycleIndex = 0
                }

                fun onT9Key(key: Int) {
                    val chars = t9Map[key] ?: return
                    commitJob?.cancel()
                    if (lastPressedKey == key) {
                        cycleIndex = (cycleIndex + 1) % chars.size
                    } else {
                        commitPending()
                        lastPressedKey = key
                        cycleIndex = 0
                    }
                    pendingChar = chars[cycleIndex]
                    commitJob = coroutineScope.launch {
                        delay(800)
                        commitPending()
                    }
                }

                fun onT9Backspace() {
                    commitJob?.cancel()
                    pendingChar = null
                    lastPressedKey = null
                    cycleIndex = 0
                    if (searchQuery.isNotEmpty()) {
                        searchQuery = searchQuery.dropLast(1)
                    }
                }

                val displayQuery = searchQuery + (pendingChar ?: "")

                PokedexLayout(
                    listContent = {
                        if (appState.enteredMenuItem == MenuItem.POKEDEX) {
                            PokemonListScreen(
                                navController = navController,
                                onPokemonSelected = { id ->
                                    appState = appState.copy(selectedPokemonId = id)
                                },
                                searchQuery = displayQuery,
                                onSearchQueryChange = { searchQuery = it; pendingChar = null }
                            )
                        } else {
                            MainMenuScreen(
                                selectedItem = appState.selectedMenuItem,
                                onItemSelected = { }
                            )
                        }
                    },
                    detailContent = {
                        when (appState.enteredMenuItem) {
                            null -> MenuPreviewScreen(item = appState.selectedMenuItem)
                            MenuItem.POKEDEX -> {
                                if (appState.selectedPokemonId != null) {
                                    PokemonDetailScreen(
                                        pokemonId = appState.selectedPokemonId!!,
                                        navController = navController,
                                        showBackButton = false
                                    )
                                } else {
                                    EmptyDetailScreen()
                                }
                            }
                            MenuItem.POKEWALKER -> PokewalkerScreen()
                            MenuItem.EMULATOR   -> EmulatorScreen()
                            MenuItem.SETTINGS   -> SettingsScreen()
                        }
                    },
                    smallLeftContent = {
                        when (appState.enteredMenuItem) {
                            MenuItem.POKEDEX -> CaptureRateScreen(pokemonId = appState.selectedPokemonId)
                            else -> StatusIndicatorScreen(label = "SYS", status = "IDLE")
                        }
                    },
                    smallMidContent = { page ->
                        when (appState.enteredMenuItem) {
                            MenuItem.POKEDEX -> TypeEffectivenessScreen(pokemonId = appState.selectedPokemonId, page = page)
                            else -> StatusIndicatorScreen(label = "COM", status = "OFF")
                        }
                    },
                    smallRightContent = {
                        when (appState.enteredMenuItem) {
                            MenuItem.POKEDEX -> PokemonIdScreen(pokemonId = appState.selectedPokemonId)
                            else -> StatusIndicatorScreen(label = "IR", status = "OFF")
                        }
                    },
                    onT9Key = { onT9Key(it) },
                    onT9Button10Press = {
                        button10PressTime = System.currentTimeMillis()
                        button10Job?.cancel()
                        button10Job = coroutineScope.launch {
                            delay(500)
                            while (true) {
                                onT9Backspace()
                                delay(150)
                            }
                        }
                    },
                    onT9Button10Release = {
                        val pressDuration = System.currentTimeMillis() - button10PressTime
                        button10Job?.cancel()
                        button10Job = null
                        if (pressDuration < 500) {
                            onT9Key(10)
                        }
                    },
                    onNavUp = {
                        if (appState.enteredMenuItem == null) {
                            val newIndex = (currentMenuIndex - 1 + menuItems.size) % menuItems.size
                            appState = appState.copy(selectedMenuItem = menuItems[newIndex])
                        }
                    },
                    onNavDown = {
                        if (appState.enteredMenuItem == null) {
                            val newIndex = (currentMenuIndex + 1) % menuItems.size
                            appState = appState.copy(selectedMenuItem = menuItems[newIndex])
                        }
                    },
                    onCircleRight = {
                        if (appState.enteredMenuItem == null) {
                            appState = appState.copy(enteredMenuItem = appState.selectedMenuItem)
                        }
                    },
                    onCircleLeft = {
                        if (appState.enteredMenuItem != null) {
                            appState = appState.copy(
                                enteredMenuItem = null,
                                selectedPokemonId = null
                            )
                        }
                    },
                    onDpadUp = { },
                    onDpadDown = { },
                    onDpadLeft = { },
                    onDpadRight = { }
                )
            } else {
                // Cover screen — closed Pokédex image as background
                Image(
                    painter = painterResource(id = R.drawable.closed_dex),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds
                )
            }
        }
    }
}