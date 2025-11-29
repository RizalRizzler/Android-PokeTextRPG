package com.example.myapplication.models
class Player(val name: String){
    val inventory: Inventory = Inventory()
    val party: MutableList<Pokemon> = mutableListOf()

    val pcBox: MutableList<Pokemon> = mutableListOf()//dummy

    companion object{
        const val MAX_PARTY_SIZE = 6
    }

    fun addPokemon(pokemon: Pokemon): String{
        if (party.size < MAX_PARTY_SIZE){
            party.add(pokemon)
            return "${pokemon.name} was added to you party!"
        } else {
            pcBox.add(pokemon)
            return "${pokemon.name} was sent to the PC Box"
        }
    }

    fun getActivePokemon(): Pokemon?{
        return party.firstOrNull { !it.isFainted() }
    }

}