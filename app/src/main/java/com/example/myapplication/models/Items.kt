package com.example.myapplication.models

import kotlin.random.Random

interface Item {
    val name: String
    val canBeUsedPostBattle: Boolean
    // Returns a String message to be displayed by the Android UI (Toast or Text)
    fun use(target: Pokemon, inventory: Inventory, index: Int): String
}

class Inventory(private val size: Int = 10) {
    private val items: Array<Item?> = arrayOfNulls(size)

    /**
     * Tries to add an item.
     * Returns a String message describing what happened (Success or Failure).
     */
    fun addItem(item: Item): String {
        for (i in items.indices) {
            if (items[i] == null) {
                items[i] = item
                return "Added ${item.name} to inventory."
            }
        }
        return "Inventory is full!"
    }

    fun removeItem(index: Int) {
        if (index in items.indices) {
            items[index] = null
        }
    }

    fun getItem(index: Int): Item? {
        return items.getOrNull(index)
    }

    /**
     * Returns the raw array of items.
     * The Android UI will use this to generate the list of buttons.
     */
    fun getContents(): Array<Item?> {
        return items
    }

    // NOTE: listItems() was removed because the Console cannot print in Android app.
}

class Potion(val healAmount: Int = 20) : Item {
    override val name: String = "Potion (Heal $healAmount)"
    override val canBeUsedPostBattle: Boolean = true

    override fun use(target: Pokemon, inventory: Inventory, index: Int): String {
        if (target.isFainted()) {
            return "${target.name} is fainted!"
        } else if (target.currentHP == target.maxHP) {
            return "${target.name} already has full HP!"
        }
        target.heal(healAmount)
        inventory.removeItem(index)
        return "${target.name} was healed for $healAmount HP."
    }
}

class SuperPotion(val healAmount: Int = 60) : Item { // Renamed to Capital S for standard convention
    override val name: String = "Super Potion (Heal $healAmount)"
    override val canBeUsedPostBattle: Boolean = true

    override fun use(target: Pokemon, inventory: Inventory, index: Int): String {
        if (target.isFainted()) {
            return "${target.name} is fainted!"
        } else if (target.currentHP == target.maxHP) {
            return "${target.name} already has full HP!"
        }
        target.heal(healAmount)
        inventory.removeItem(index)
        return "${target.name} was healed for $healAmount HP."
    }
}

class Pokeball : Item {
    override val name: String = "Pokeball"
    override val canBeUsedPostBattle: Boolean = false

    override fun use(target: Pokemon, inventory: Inventory, index: Int): String {
        inventory.removeItem(index)

        // Logic remains exactly the same as your original code
        val catchRate = (1.0 - (target.currentHP.toDouble() / target.maxHP)) * 0.7 + 0.3

        return if (Random.nextDouble() < catchRate) {
            // Note: Actual logic to 'add to party' must be handled by the Battle Manager checking this string
            "Gotcha! ${target.name} was caught!"
        } else {
            "Oh no! ${target.name} broke free!"
        }
    }
}

class Revive : Item {
    override val name: String = "Revive"
    override val canBeUsedPostBattle: Boolean = true

    override fun use(target: Pokemon, inventory: Inventory, index: Int): String {
        if (target.currentHP > 0) {
            return "Can't use revive on a healthy pokemon!"
        }
        target.heal(target.maxHP / 2)
        inventory.removeItem(index)
        return "${target.name} was revived!"
    }
}