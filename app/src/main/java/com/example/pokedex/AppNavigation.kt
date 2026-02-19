package com.example.pokedex

enum class MenuItem {
    POKEDEX,
    POKEWALKER,
    EMULATOR,
    SETTINGS
}

data class AppState(
    val selectedMenuItem: MenuItem = MenuItem.POKEDEX,
    val enteredMenuItem: MenuItem? = null,
    val selectedPokemonId: Int? = null
)