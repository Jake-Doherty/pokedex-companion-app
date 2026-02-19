package com.example.pokedex

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

@Serializable
data class PokemonSpecies(
    val id: Int,
    val name: String,
    @SerialName("is_legendary") val isLegendary: Boolean,
    @SerialName("is_mythical") val isMythical: Boolean,
    @SerialName("capture_rate") val captureRate: Int?,
    @SerialName("base_happiness") val baseHappiness: Int?,
    @SerialName("gender_rate") val genderRate: Int?,
    @SerialName("hatch_counter") val hatchCounter: Int?,
    @SerialName("evolution_chain_id") val evolutionChainId: Int?,
    @SerialName("generation_id") val generationId: Int?
)

@Serializable
data class PokemonSprite(
    @SerialName("pokemon_id") val pokemonId: Int,
    val sprites: JsonElement
) {
    fun getFrontDefaultUrl(): String? {
        return try {
            sprites.jsonObject["front_default"]?.jsonPrimitive?.contentOrNull
        } catch (e: Exception) {
            null
        }
    }

    fun getFrontShinyUrl(): String? {
        return try {
            sprites.jsonObject["front_shiny"]?.jsonPrimitive?.contentOrNull
        } catch (e: Exception) {
            null
        }
    }
}

@Serializable
data class PokemonType(
    @SerialName("pokemon_id") val pokemonId: Int,
    @SerialName("type_id") val typeId: Int,
    val slot: Int
)

@Serializable
data class TypeName(
    val id: Int,
    val name: String
)

@Serializable
data class PokemonStat(
    @SerialName("pokemon_id") val pokemonId: Int,
    @SerialName("stat_id") val statId: Int,
    @SerialName("base_stat") val baseStat: Int,
    val effort: Int
)

@Serializable
data class StatName(
    val id: Int,
    val name: String
)

@Serializable
data class PokemonAbility(
    @SerialName("pokemon_id") val pokemonId: Int,
    @SerialName("ability_id") val abilityId: Int,
    @SerialName("is_hidden") val isHidden: Boolean,
    val slot: Int
)

@Serializable
data class AbilityName(
    val id: Int,
    val name: String
)

@Serializable
data class PokemonData(
    val id: Int,
    val height: Int?,
    val weight: Int?,
    @SerialName("base_experience") val baseExperience: Int?
)

@Serializable
data class PokemonEggGroup(
    @SerialName("pokemon_species_id") val speciesId: Int,
    @SerialName("egg_group_id") val eggGroupId: Int
)

@Serializable
data class EggGroupName(
    val id: Int,
    val name: String
)

@Serializable
data class FlavorText(
    @SerialName("pokemon_species_id") val speciesId: Int,
    @SerialName("flavor_text") val flavorText: String,
    @SerialName("language_id") val languageId: Int,
    @SerialName("version_id") val versionId: Int
)

@Serializable
data class EvolutionChain(
    val id: Int,
    @SerialName("evolved_species_id") val evolvedSpeciesId: Int?,
    @SerialName("min_level") val minLevel: Int?,
    @SerialName("item_id") val itemId: Int? = null,
    @SerialName("trigger_id") val triggerId: Int? = null
)

@Serializable
data class EvolutionTrigger(
    val id: Int,
    val name: String
)

@Serializable
data class PokemonMove(
    @SerialName("pokemon_id") val pokemonId: Int,
    @SerialName("move_id") val moveId: Int,
    @SerialName("move_learn_method_id") val learnMethodId: Int,
    @SerialName("level") val level: Int,
    @SerialName("version_group_id") val versionGroupId: Int
)

@Serializable
data class MoveName(
    val id: Int,
    val name: String
)

@Serializable
data class MoveLearnMethod(
    val id: Int,
    val name: String
)

@Serializable
data class Generation(
    val id: Int,
    val name: String
)