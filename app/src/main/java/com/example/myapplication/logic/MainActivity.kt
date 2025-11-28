package com.example.myapplication.logic

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.R
import com.example.myapplication.models.Potion
import com.example.myapplication.models.SuperPotion
import com.example.myapplication.models.createRandomWildPokemon

class MainActivity : AppCompatActivity() {

    private lateinit var txtGameLog: TextView
    private lateinit var txtPlayerStatus: TextView
    private lateinit var btnAttack: Button
    private lateinit var btnHeal: Button
    private lateinit var btnRun: Button

    private var currentBattle: BattleManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Safety Check: If player is null (app crashed/restarted in background), go back to Setup
        if (GameRepository.player == null) {
            startActivity(Intent(this, SetupActivity::class.java))
            finish()
            return
        }

        txtGameLog = findViewById(R.id.txtGameLog)
        txtPlayerStatus = findViewById(R.id.txtPlayerStatus)
        btnAttack = findViewById(R.id.btnAttack)
        btnHeal = findViewById(R.id.btnHeal)
        btnRun = findViewById(R.id.btnRun)

        // Log welcome message
        val playerName = GameRepository.player?.name ?: "Trainer"
        val starterName = GameRepository.player?.party?.firstOrNull()?.name ?: "Pokemon"
        appendLog("Welcome, $playerName! Go get 'em, $starterName!")

        // Start the first random battle
        startWildEncounter()

        // --- BUTTON LISTENERS ---
        btnAttack.setOnClickListener {
            val battle = currentBattle ?: return@setOnClickListener
            // Use move 0 (Tackle/Scratch usually)
            val result = battle.playerFight(0)
            updateUI(result.log, result.playerCurrentHp, result.enemyCurrentHp)
            checkBattleEnd(result.status)
        }

        btnHeal.setOnClickListener {
            val battle = currentBattle ?: return@setOnClickListener
            val player = GameRepository.player ?: return@setOnClickListener

            // Find first available healing item
            var itemIndex = -1
            val inventory = player.inventory.getContents()

            for (i in inventory.indices) {
                val item = inventory[i]
                if (item is Potion || item is SuperPotion) {
                    itemIndex = i
                    break
                }
            }

            if (itemIndex != -1) {
                val result = battle.playerUseItem(itemIndex)
                updateUI(result.log, result.playerCurrentHp, result.enemyCurrentHp)
                checkBattleEnd(result.status)
            } else {
                Toast.makeText(this, "No healing items left!", Toast.LENGTH_SHORT).show()
            }
        }

        btnRun.setOnClickListener {
            val battle = currentBattle ?: return@setOnClickListener
            val result = battle.playerRun()
            updateUI(result.log, result.playerCurrentHp, result.enemyCurrentHp)
            checkBattleEnd(result.status)
        }
    }

    private fun startWildEncounter() {
        val player = GameRepository.player ?: return
        val activePokemon = player.getActivePokemon()

        if (activePokemon == null) {
            appendLog("\nAll your Pokemon have fainted! You blacked out.")
            // Heal party and restart battle
            player.party.forEach { it.heal(it.maxHP) }
            appendLog("Nurse Joy healed your party.")
            startWildEncounter()
            return
        }

        // --- RANDOMIZED ENCOUNTER ---
        // Using the helper function from Pokemon.kt
        val wildPokemon = createRandomWildPokemon()

        // Optional: Scale wild pokemon to be near player level
        val scaledLevel = (activePokemon.level - 1).coerceAtLeast(1)
        wildPokemon.setLevel(scaledLevel)

        currentBattle = BattleManager(player, activePokemon, wildPokemon)

        appendLog("\n--- NEW BATTLE ---")
        appendLog("A wild ${wildPokemon.name} appeared!")
        updateStatus(activePokemon.currentHP, wildPokemon.currentHP)
    }

    private fun updateUI(log: String, playerHp: Int, enemyHp: Int) {
        appendLog(log)
        updateStatus(playerHp, enemyHp)
    }

    private fun updateStatus(playerHp: Int, enemyHp: Int) {
        txtPlayerStatus.text = "Player HP: $playerHp | Enemy HP: $enemyHp"
    }

    private fun appendLog(text: String) {
        val currentText = txtGameLog.text.toString()
        txtGameLog.text = "$currentText\n$text"
    }

    private fun checkBattleEnd(status: BattleStatus) {
        when(status) {
            BattleStatus.WIN -> {
                appendLog("Victory!")
                Toast.makeText(this, "You Won!", Toast.LENGTH_SHORT).show()
                // Start next battle automatically
                startWildEncounter()
            }
            BattleStatus.LOSE -> {
                appendLog("Defeat...")
                Toast.makeText(this, "You Lost...", Toast.LENGTH_SHORT).show()
                startWildEncounter()
            }
            BattleStatus.CAUGHT -> {
                appendLog("You caught the Pokemon!")
                Toast.makeText(this, "Caught!", Toast.LENGTH_SHORT).show()
                startWildEncounter()
            }
            BattleStatus.RAN -> {
                appendLog("Got away safely.")
                startWildEncounter()
            }
            BattleStatus.ONGOING -> {}
        }
    }
}