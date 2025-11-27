package com.example.myapplication.logic

import com.example.myapplication.models.Inventory
import com.example.myapplication.models.Move
import com.example.myapplication.models.Player
import com.example.myapplication.models.Pokemon
import com.example.myapplication.models.Potion
import com.example.myapplication.models.Pokeball
import kotlin.random.Random

// Enum to track the state of the battle
enum class BattleStatus {
    ONGOING,
    WIN,
    LOSE,
    CAUGHT,
    RAN
}

// A data packet sent back to the UI after every turn
data class TurnResult(
    val log: String,
    val status: BattleStatus,
    val enemyCurrentHp: Int,
    val playerCurrentHp: Int
)

class BattleManager(
    private val player: Player,
    val playerPokemon: Pokemon, // Public so UI can read maxHP/Name
    val wildPokemon: Pokemon
) {

    private val playerInventory: Inventory = player.inventory

    // --- PLAYER ACTIONS ---

    fun playerFight(moveIndex: Int): TurnResult {
        val move = playerPokemon.moves.getOrNull(moveIndex)
            ?: return createResult("Invalid move!", BattleStatus.ONGOING)

        val sb = StringBuilder()

        // 1. Player Attacks
        sb.append(performAttack(playerPokemon, wildPokemon, move))

        // 2. Check if Enemy Fainted
        if (wildPokemon.isFainted()) {
            sb.append("\nThe wild ${wildPokemon.name} fainted. You win!")
            return createResult(sb.toString(), BattleStatus.WIN)
        }

        // 3. Enemy Turn (if still alive)
        sb.append("\n").append(aiTurn())

        // 4. Check if Player Fainted
        if (playerPokemon.isFainted()) {
            sb.append("\n${playerPokemon.name} fainted... You blacked out.")
            return createResult(sb.toString(), BattleStatus.LOSE)
        }

        return createResult(sb.toString(), BattleStatus.ONGOING)
    }

    fun playerUseItem(itemIndex: Int): TurnResult {
        val item = playerInventory.getItem(itemIndex)
            ?: return createResult("Invalid item slot.", BattleStatus.ONGOING)

        val sb = StringBuilder()

        // 1. Use Item
        val useMessage = when (item) {
            is Potion -> item.use(playerPokemon, playerInventory, itemIndex)
            // Note: We pass 0 as a dummy index for Pokeball because it consumes itself in logic,
            // but your original logic passed itemIndex. Ensure Item.kt handles removal correctly.
            is Pokeball -> item.use(wildPokemon, playerInventory, itemIndex)
            else -> "You can't use that here!"
        }
        sb.append(useMessage)

        // 2. Check Catch Success
        if (useMessage.contains("Gotcha!")) {
            player.addPokemon(wildPokemon)
            return createResult(sb.toString(), BattleStatus.CAUGHT)
        }

        // 3. If it was a healing item, the turn continues -> Enemy Attacks
        // (Unless the item logic failed, e.g., "Already full HP")
        if (!useMessage.contains("broke free") && !useMessage.contains("healed")) {
            // If item failed (e.g. "Can't use"), don't trigger enemy turn?
            // Design choice: usually wasting a turn means getting hit.
            // For now, we only trigger enemy turn if item was actually used.
            return createResult(sb.toString(), BattleStatus.ONGOING)
        }

        sb.append("\n").append(aiTurn())

        if (playerPokemon.isFainted()) {
            sb.append("\n${playerPokemon.name} fainted...")
            return createResult(sb.toString(), BattleStatus.LOSE)
        }

        return createResult(sb.toString(), BattleStatus.ONGOING)
    }

    fun playerRun(): TurnResult {
        return createResult("You got away safely!", BattleStatus.RAN)
    }

    // --- INTERNAL LOGIC ---

    private fun aiTurn(): String {
        val aiMove = wildPokemon.moves.random()
        return performAttack(wildPokemon, playerPokemon, aiMove)
    }

    private fun performAttack(attacker: Pokemon, target: Pokemon, move: Move): String {
        // Math logic from your original Battle.kt
        val levelFactor = (2.0 * attacker.level / 5.0) + 2.0
        val effectiveAtk = attacker.currentAtk.toDouble() / (10 + attacker.level)
        val baseDamage = (levelFactor * move.basePower * effectiveAtk) / 10.0

        val isCrit = Random.nextInt(100) < 10
        val critMod = if (isCrit) 1.5 else 1.0
        val randMod = 0.85 + (Random.nextDouble() * 0.15)

        val finalDamage = maxOf(1, (baseDamage * critMod * randMod).toInt())
        target.takeDamage(finalDamage)

        val log = StringBuilder("${attacker.name} used ${move.name}!\n")
        if (isCrit) log.append("Critical hit! ")
        log.append("Dealt $finalDamage damage.")

        return log.toString()
    }

    private fun createResult(message: String, status: BattleStatus): TurnResult {
        return TurnResult(
            log = message,
            status = status,
            enemyCurrentHp = wildPokemon.currentHP,
            playerCurrentHp = playerPokemon.currentHP
        )
    }
}