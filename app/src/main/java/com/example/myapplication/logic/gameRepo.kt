package com.example.myapplication.logic

import com.example.myapplication.models.Player
import com.example.myapplication.models.Pokeball
import com.example.myapplication.models.Potion
import com.example.myapplication.models.SuperPotion

// 'object' means this is a Singleton. There is only ONE instance of this
// in the entire app, and it can be accessed from anywhere.
object GameRepository {

    // This holds the player data globally.
    // It is nullable (?) because when the app starts, the player hasn't been created yet.
    var player: Player? = null

    // Replaces the initialization code from main.kt
    fun initializeGame(playerName: String) {
        val newPlayer = Player(playerName)

        // Add Starter Items (Logic copied from your main.kt)
        // Note: We use 3 of each for brevity, you can add more
        repeat(3) { newPlayer.inventory.addItem(Potion()) }
        repeat(3) { newPlayer.inventory.addItem(SuperPotion()) } // Ensure class is renamed to SuperPotion
        repeat(3) { newPlayer.inventory.addItem(Pokeball()) }

        player = newPlayer
    }
}