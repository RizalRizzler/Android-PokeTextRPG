package com.example.myapplication.logic

import android.util.Log
import com.example.myapplication.models.Inventory
import com.example.myapplication.models.Move
import com.example.myapplication.models.Player
import com.example.myapplication.models.Pokemon
import com.example.myapplication.models.Potion
import com.example.myapplication.models.Pokeball
import kotlin.random.Random

enum class BattleStatus {
    ONGOING,
    WIN,
    LOSE,
    CAUGHT,
    RAN
}

data class TurnResult(
    val log: String,
    val status: BattleStatus,
    val enemyCurrentHp: Int,
    val playerCurrentHp: Int,
    val playerExp: Int,
    val playerMaxExp: Int,
    val playerLevel: Int
)

class BattleManager(
    private val player: Player,
    val playerPokemon: Pokemon,
    val wildPokemon: Pokemon
) {

    private val playerInventory: Inventory = player.inventory

    fun playerFight(moveIndex: Int): TurnResult {
        val move = playerPokemon.moves.getOrNull(moveIndex)
            ?: return createResult("Invalid move!", BattleStatus.ONGOING)

        val sb = StringBuilder()

        sb.append(performAttack(playerPokemon, wildPokemon, move))

        if (wildPokemon.isFainted()) {
            sb.append("\nThe wild ${wildPokemon.name} fainted. You win!")


            val expReward = wildPokemon.level * 5
            val levelLog = playerPokemon.expGain(expReward)
            sb.append("\n$levelLog")

            return createResult(sb.toString(), BattleStatus.WIN)
        }

        sb.append("\n").append(aiTurn())

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

        val useMessage = when (item) {
            is Potion -> item.use(playerPokemon, playerInventory, itemIndex)
            is Pokeball -> item.use(wildPokemon, playerInventory, itemIndex)
            else -> "You can't use that here!"
        }
        sb.append(useMessage)

        if (useMessage.contains("Gotcha!")) {
            player.addPokemon(wildPokemon)
            return createResult(sb.toString(), BattleStatus.CAUGHT)
        }

        if (!useMessage.contains("broke free") && !useMessage.contains("healed")) {
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

    private fun aiTurn(): String {
        val aiMove = wildPokemon.moves.random()
        return performAttack(wildPokemon, playerPokemon, aiMove)
    }

    private fun performAttack(attacker: Pokemon, target: Pokemon, move: Move): String {
        val levelFactor = (2.0 * attacker.level / 5.0) + 2.0
        val effectiveAtk = attacker.currentAtk.toDouble() / (10 + attacker.level)
        val baseDamage = (levelFactor * move.basePower * effectiveAtk) / 10.0

        val isCrit = Random.nextInt(100) < 10
        val critMod = if (isCrit) 1.5 else 1.0
        val randMod = 0.85 + (Random.nextDouble() * 0.15)

        val finalDamage = maxOf(1, (baseDamage * critMod * randMod).toInt())
        Log.d("BattleLogic", "${attacker.name} used ${move.name}. Base: $baseDamage, Final: $finalDamage")
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
            playerCurrentHp = playerPokemon.currentHP,
            playerExp = playerPokemon.exp,
            playerMaxExp = playerPokemon.expToLevelUp,
            playerLevel = playerPokemon.level
        )
    }
}