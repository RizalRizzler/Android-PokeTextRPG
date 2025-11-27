package com.example.myapplication.logic

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.R
import com.example.myapplication.models.Potion
import com.example.myapplication.models.SuperPotion
import com.example.myapplication.models.createPokemon
import com.example.myapplication.models.createRandomWildPokemon

class MainActivity : AppCompatActivity() {

    // Game UI Elements
    private lateinit var layoutSetup: LinearLayout
    private lateinit var layoutGame: LinearLayout

    // Setup UI Elements
    private lateinit var etPlayerName: EditText
    private lateinit var rgStarter: RadioGroup
    private lateinit var btnStartGame: Button

    // Battle UI Elements
    private lateinit var txtGameLog: TextView
    private lateinit var txtPlayerStatus: TextView
    private lateinit var btnAttack: Button
    private lateinit var btnHeal: Button
    private lateinit var btnRun: Button

    private var currentBattle: BattleManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize Views
        layoutSetup = findViewById(R.id.layoutSetup)
        layoutGame = findViewById(R.id.layoutGame)

        etPlayerName = findViewById(R.id.etPlayerName)
        rgStarter = findViewById(R.id.rgStarter)
        btnStartGame = findViewById(R.id.btnStartGame)

        txtGameLog = findViewById(R.id.txtGameLog)
        txtPlayerStatus = findViewById(R.id.txtPlayerStatus)
        btnAttack = findViewById(R.id.btnAttack)
        btnHeal = findViewById(R.id.btnHeal)
        btnRun = findViewById(R.id.btnRun)

        // Check if game is already running (e.g. after rotation), otherwise show setup
        if (GameRepository.player == null) {
            showSetupScreen()
        } else {
            showGameScreen()
        }

        // --- Setup Logic ---
        btnStartGame.setOnClickListener {
            val nameInput = etPlayerName.text.toString()
            if (nameInput.isBlank()) {
                etPlayerName.error = "Please enter a name!"
                return@setOnClickListener
            }

            // Determine Starter based on Radio Button
            val selectedStarterName = when (rgStarter.checkedRadioButtonId) {
                R.id.rbTreecko -> "TREECKO"
                R.id.rbTorchic -> "TORCHIC"
                R.id.rbMudkip -> "MUDKIP"
                else -> "TREECKO" // Default
            }

            // Initialize Game
            GameRepository.initializeGame(nameInput)
            val starterPokemon = createPokemon(selectedStarterName)

            if (starterPokemon != null) {
                GameRepository.player?.addPokemon(starterPokemon)
                showGameScreen()
                appendLog("Welcome, $nameInput! You chose ${starterPokemon.name}!")
                startWildEncounter()
            } else {
                Toast.makeText(this, "Error creating starter.", Toast.LENGTH_SHORT).show()
            }
        }

        // --- Battle Logic ---
        btnAttack.setOnClickListener {
            val battle = currentBattle ?: return@setOnClickListener
            val result = battle.playerFight(0) // Uses first move
            updateUI(result.log, result.playerCurrentHp, result.enemyCurrentHp)
            checkBattleEnd(result.status)
        }

        btnHeal.setOnClickListener {
            val battle = currentBattle ?: return@setOnClickListener
            val player = GameRepository.player ?: return@setOnClickListener

            // Find healing item
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

    private fun showSetupScreen() {
        layoutSetup.visibility = View.VISIBLE
        layoutGame.visibility = View.GONE
    }

    private fun showGameScreen() {
        layoutSetup.visibility = View.GONE
        layoutGame.visibility = View.VISIBLE
    }

    private fun startWildEncounter() {
        val player = GameRepository.player ?: return
        val activePokemon = player.getActivePokemon()

        if (activePokemon == null) {
            appendLog("\nAll your Pokemon have fainted! You blacked out.")
            // Logic to heal party (Simulating Center)
            player.party.forEach { it.heal(it.maxHP) }
            appendLog("Nurse Joy healed your party. Be careful next time!")
            // Try again
            startWildEncounter()
            return
        }

        // --- RANDOMIZED ENCOUNTER CHANGE ---
        // Instead of hardcoding "TORCHIC", we use the helper from Pokemon.kt
        val wildPokemon = createRandomWildPokemon()

        // Scale enemy level to player level roughly (optional, but good for balance)
        wildPokemon.setLevel(activePokemon.level.coerceAtLeast(1))

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

        // Auto-scroll logic could be added here
    }

    private fun checkBattleEnd(status: BattleStatus) {
        when(status) {
            BattleStatus.WIN -> {
                appendLog("Victory!")
                Toast.makeText(this, "You Won!", Toast.LENGTH_SHORT).show()
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