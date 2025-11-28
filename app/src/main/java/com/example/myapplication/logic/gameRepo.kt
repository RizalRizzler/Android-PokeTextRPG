package com.example.myapplication.logic

import com.example.myapplication.models.Player
import com.example.myapplication.models.Pokeball
import com.example.myapplication.models.Potion
import com.example.myapplication.models.SuperPotion


object GameRepository {
    //nullable (?) because when the app starts, the player hasn't been created yet.
    var player: Player? = null

    //initialization code from main.kt
    fun initializeGame(playerName: String) {
        val newPlayer = Player(playerName)

        //starter items
        repeat(3) { newPlayer.inventory.addItem(Potion()) }
        repeat(3) { newPlayer.inventory.addItem(SuperPotion()) }
        repeat(3) { newPlayer.inventory.addItem(Pokeball()) }

        player = newPlayer
    }
}