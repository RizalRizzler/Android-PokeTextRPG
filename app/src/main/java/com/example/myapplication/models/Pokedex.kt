package com.example.myapplication.models
class Move(val name: String, val basePower: Int)

data class pokemonSpecies(
    val name: String,
    val baseHP: Int,
    val baseATK: Int,
    val moves: List<Move>
)

object Pokedex{
    private val tackle = Move("Tackle", basePower = 15)
    private val Obliteration = Move("Obliteration", 240)
    private val ember = Move("Ember", 30)
    private val absorb = Move("Absorb", 30)
    private val waterGun = Move("Water Gun",30)
    private val thundershock = Move("Thunder Shock", 30)
    private val wingAttack = Move("Wing Attack",30)
    private val headbutt = Move("Headbutt", 35)
    private val rollout = Move("Rollout",20)
    private val splash = Move("Splash",0)

    private val TREECKO = pokemonSpecies("Treecko",38,10,listOf(tackle,absorb))
    private val PIKACHU = pokemonSpecies("Pikachu",32,11,listOf(tackle,thundershock))
    private val TORCHIC = pokemonSpecies("Torchic",32,13,listOf(tackle,ember))
    private val MUDKIP = pokemonSpecies("Mudkip",44,10,listOf(tackle,waterGun))
    private val TAILOW = pokemonSpecies("Tailow",28,10,listOf(tackle,wingAttack))
    private val ZIGZAGOON = pokemonSpecies("Zigzagoon",24,9,listOf(tackle,headbutt))
    private val MAGIKARP = pokemonSpecies("Magikarp",18,8,listOf(tackle))
    private val MAGICKARP = pokemonSpecies("'Magic'Karp",18,12,listOf(tackle,Obliteration,splash,splash))
    private val MILTANK = pokemonSpecies("Miltank",46,9,listOf(rollout))    //make map buat list pokemon, key nya string value nya variable pokemon yang udah diisi

    val database: Map<String, pokemonSpecies> = mapOf(
        "TREECKO" to TREECKO,
        "PIKACHU" to PIKACHU,
        "TORCHIC" to TORCHIC,
        "MUDKIP" to MUDKIP,
        "TAILOW" to TAILOW,
        "ZIGZAGOON" to ZIGZAGOON,
        "MAGIKARP" to MAGIKARP,
        "MAGICKARP" to MAGICKARP,
        "MILTANK" to MILTANK
    )
}
