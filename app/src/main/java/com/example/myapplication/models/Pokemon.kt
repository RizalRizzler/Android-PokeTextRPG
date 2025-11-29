package com.example.myapplication.models

class Pokemon(val species: pokemonSpecies){
    var name: String = species.name
    var maxHP: Int = species.baseHP
    val moves: List<Move> = species.moves

    private  var _currentHp: Int = maxHP
    val currentHP: Int
        get() = _currentHp

    //starter lv set to 5
    var level: Int = 5
        private set

    var exp: Int = 0
        private set

    val expToLevelUp: Int
        get() = level * level * level

    var currentAtk: Int = species.baseATK +  (level*2)
//        get() = species.baseATK +  (level*2)

    init {
        _currentHp = maxHP
    }
    fun takeDamage(damage: Int){
        _currentHp -= damage
        if (_currentHp < 0){
            _currentHp = 0
        }
    }

    fun heal(amount: Int){
        _currentHp += amount
        if (_currentHp >maxHP){
            _currentHp = maxHP
        }
    }

    fun isFainted(): Boolean = _currentHp <= 0
    //println to string builder
    fun expGain(amount: Int): String {
        exp += amount
        val logBuilder = StringBuilder("$name gained $amount EXP.")

        while (exp >= expToLevelUp) {
            val levelUpMessage = levelUp()
            logBuilder.append("\n").append(levelUpMessage)
        }

        return logBuilder.toString()
    }

    fun levelUp(): String{
        exp -= expToLevelUp
        level++
    
        
        maxHP = species.baseHP + (level - 1) * 3
        currentAtk = species.baseATK + (level - 1) * 2
        //heal ke maxHP
        _currentHp = maxHP
        return "$name grew to Level $level! (HP: $maxHP, Atk: $currentAtk)"
    }

    fun setLevel(newLvl: Int){
        level = newLvl.coerceAtLeast(1)
        exp = 0

        maxHP = species.baseHP + (level - 1) * 3
        currentAtk = species.baseATK + (level - 1) * 2
        _currentHp = maxHP

    }
}


fun createPokemon(speciesName: String): Pokemon? {
    val species = Pokedex.database[speciesName.uppercase()]?: return null
    return Pokemon(species)
}

fun createRandomWildPokemon(): Pokemon {
    val randomSpeciesName = Pokedex.database.keys.random()
    return createPokemon(randomSpeciesName)!!
}

