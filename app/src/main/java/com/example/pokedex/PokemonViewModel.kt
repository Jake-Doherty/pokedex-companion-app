package com.example.pokedex

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class PokemonUiState {
    object Loading : PokemonUiState()
    data class Success(
        val pokemon: List<PokemonSpecies>,
        val sprites: Map<Int, PokemonSprite>,
        val typesByPokemonId: Map<Int, List<String>>
    ) : PokemonUiState()
    data class Error(val message: String) : PokemonUiState()
}

class PokemonViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<PokemonUiState>(PokemonUiState.Loading)
    val uiState: StateFlow<PokemonUiState> = _uiState

    init {
        fetchPokemon()
    }

    private suspend fun <T> fetchAllRows(
        table: String,
        decoder: suspend (io.github.jan.supabase.postgrest.result.PostgrestResult) -> List<T>
    ): List<T> {
        val pageSize = 1000
        val results = mutableListOf<T>()
        var offset = 0
        while (true) {
            val page = decoder(
                SupabaseClientProvider.supabase
                    .from(table)
                    .select {
                        range(offset.toLong(), (offset + pageSize - 1).toLong())
                    }
            )
            results.addAll(page)
            if (page.size < pageSize) break
            offset += pageSize
        }
        return results
    }

    private fun fetchPokemon() {
        viewModelScope.launch {
            _uiState.value = PokemonUiState.Loading
            try {
                val pokemon = fetchAllRows("pokemon_v2_pokemonspecies") {
                    it.decodeList<PokemonSpecies>()
                }.sortedBy { it.id }

                val sprites = fetchAllRows("pokemon_v2_pokemonsprites") {
                    it.decodeList<PokemonSprite>()
                }.associateBy { it.pokemonId }

                val typeNames = SupabaseClientProvider.supabase
                    .from("pokemon_v2_type")
                    .select()
                    .decodeList<TypeName>()
                    .associateBy { it.id }

                // Fetch all pokemon types in two pages to get past 1000 row limit
                val pokemonTypesPage1 = SupabaseClientProvider.supabase
                    .from("pokemon_v2_pokemontype")
                    .select { range(0L, 999L) }
                    .decodeList<PokemonType>()

                val pokemonTypesPage2 = SupabaseClientProvider.supabase
                    .from("pokemon_v2_pokemontype")
                    .select { range(1000L, 1999L) }
                    .decodeList<PokemonType>()

                val pokemonTypesPage3 = SupabaseClientProvider.supabase
                    .from("pokemon_v2_pokemontype")
                    .select { range(2000L, 2999L) }
                    .decodeList<PokemonType>()

                val allPokemonTypes = pokemonTypesPage1 + pokemonTypesPage2 + pokemonTypesPage3

                val typesByPokemonId = allPokemonTypes
                    .groupBy { it.pokemonId }
                    .mapValues { (_, types) ->
                        types.sortedBy { it.slot }
                            .mapNotNull { typeNames[it.typeId]?.name }
                    }

                _uiState.value = PokemonUiState.Success(pokemon, sprites, typesByPokemonId)
            } catch (e: Exception) {
                _uiState.value = PokemonUiState.Error(e.message ?: "Unknown error")
            }
        }
    }
}