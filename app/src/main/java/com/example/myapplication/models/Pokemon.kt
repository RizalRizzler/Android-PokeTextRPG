package com.example.myapplication.models

import android.os.Parcelable
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Parcelize
class Pokemon(
    val speciesName: String,
    var level: Int = 5,
    var exp: Int = 0,
    private var _currentHp: Int
) : Parcelable {

    @IgnoredOnParcel
    val species: pokemonSpecies
        get() = Pokedex.database[speciesName]?: throw IllegalStateException("Species '$speciesName' not found in Pokedex!")

    @IgnoredOnParcel
    val maxHP: Int
        get() = species.baseHP + (level - 1) * 3
    @IgnoredOnParcel
    val currentAtk: Int
        get() = species.baseATK + (level - 1) * 2

    val currentHP: Int
        get() = _currentHp

    val moves: List<Move>
        get() = species.moves

    val name: String
        get() = species.name

    // 4. Logic Methods
    fun takeDamage(damage: Int){
        _currentHp -= damage
        if (_currentHp < 0) _currentHp = 0
    }

    fun heal(amount: Int){
        _currentHp += amount
        if (_currentHp > maxHP) _currentHp = maxHP
    }

    fun isFainted(): Boolean = _currentHp <= 0

    fun expGain(amount: Int): String {
        exp += amount
        val logBuilder = StringBuilder("$name gained $amount EXP.")
        while (exp >= expToLevelUp) {
            logBuilder.append("\n").append(levelUp())
        }
        return logBuilder.toString()
    }

    val expToLevelUp: Int
        get() = level * level * level

    fun levelUp(): String{
        exp -= expToLevelUp
        level++

        _currentHp = maxHP

        return "$name grew to Level $level! (HP: $maxHP, Atk: $currentAtk)"
    }

    fun setLevel(newLvl: Int){
        level = newLvl.coerceAtLeast(1)
        exp = 0
        _currentHp = maxHP
    }
}

//ganti construct

fun createPokemon(speciesName: String): Pokemon? {
    val species = Pokedex.database[speciesName.uppercase()] ?: return null

    val startingLevel = 5
    val initialMaxHP = species.baseHP + (startingLevel - 1) * 3
    return Pokemon(
        speciesName = speciesName.uppercase(),
        level = startingLevel,
        exp = 0,
        _currentHp = initialMaxHP
    )
}

fun createRandomWildPokemon(): Pokemon {
    val randomSpeciesName = Pokedex.database.keys.random()
    return createPokemon(randomSpeciesName)!!
}