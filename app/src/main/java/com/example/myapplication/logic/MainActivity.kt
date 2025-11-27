package com.example.myapplication.logic

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.R
import com.example.myapplication.models.Potion
import com.example.myapplication.models.Revive
import com.example.myapplication.models.SuperPotion
import com.example.myapplication.models.createPokemon

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

        txtGameLog = findViewById(R.id.txtGameLog)
        txtPlayerStatus = findViewById(R.id.txtPlayerStatus)
        btnAttack = findViewById(R.id.btnAttack)
        btnHeal = findViewById(R.id.btnHeal)
        btnRun = findViewById(R.id.btnRun)

        if (GameRepository.player == null) {
            GameRepository.initializeGame("Ash Ketchum")
            val starter = createPokemon("TREECKO")
            if (starter != null) {
                GameRepository.player?.addPokemon(starter)
                appendLog("You chose Treecko!")
            }
        }

        startWildEncounter()

        btnAttack.setOnClickListener {
            val battle = currentBattle ?: return@setOnClickListener
            val result = battle.playerFight(0) // Uses first move (Tackle)
            updateUI(result.log, result.playerCurrentHp, result.enemyCurrentHp)
            checkBattleEnd(result.status)
        }

        btnHeal.setOnClickListener {
            val battle = currentBattle ?: return@setOnClickListener
            val player = GameRepository.player ?: return@setOnClickListener

            // FIX: Find the first index that contains a Potion, SuperPotion, or Revive
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
        // Auto-switch to first healthy pokemon
        val activePokemon = player.getActivePokemon()

        if (activePokemon == null) {
            appendLog("All your Pokemon have fainted! You blacked out.")
            // In a real game, you'd heal them here or reset the game
            player.party.forEach { it.heal(it.maxHP) }
            appendLog("Nurse Joy healed your party.")
            startWildEncounter() // Try again
            return
        }

        val wildPokemon = createPokemon("TORCHIC") ?: return
        wildPokemon.setLevel(5)

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
        // Optional: Scroll to bottom logic would go here
    }

    private fun checkBattleEnd(status: BattleStatus) {
        when(status) {
            BattleStatus.WIN -> {
                appendLog("Victory!")
                Toast.makeText(this, "You Won!", Toast.LENGTH_SHORT).show()
                // FIX: Start a new battle after a short delay or immediately
                startWildEncounter()
            }
            BattleStatus.LOSE -> {
                appendLog("Defeat...")
                Toast.makeText(this, "You Lost...", Toast.LENGTH_SHORT).show()
                // Logic to heal or restart
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