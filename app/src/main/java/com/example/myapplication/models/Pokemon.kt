package com.example.myapplication.models

class Pokemon(val species: pokemonSpecies){
    var name: String = species.name
    var maxHP: Int = species.baseHP
    val moves: List<Move> = species.moves

    private  var _currentHp: Int = maxHP
    val currentHP: Int
        get() = _currentHp

    //level awal pokemon (5 karena biasanya starter lv 5)
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
        //supaya current hp tidak negatif
        if (_currentHp < 0){
            _currentHp = 0
        }
    }

    fun heal(amount: Int){
        _currentHp += amount
        //supaya current hp tidak melebihi maxHP
        if (_currentHp >maxHP){
            _currentHp = maxHP
        }
    }

    fun isFainted(): Boolean = _currentHp <= 0

    /**
     * Adds Exp and handles leveling up.
     * Returns a String log of what happened for the UI to display.
     */
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
        exp -= expToLevelUp //current exp dikurangi cap exp lalu level up
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

