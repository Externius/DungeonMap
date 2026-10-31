package externius.rdmg.helpers

import externius.rdmg.models.Treasures
import kotlin.math.floor

internal object Treasure {
    private const val BASE = "Treasures: "
    private var sumValue = 0

    private val treasureGP = intArrayOf(
        0, 300, 600, 900, 1200, 1600, 2000, 2600, 3400, 4500, 5800,
        7500, 9800, 13000, 17000, 22000, 28000, 36000, 47000, 61000, 80000
    )

    private val itemCount = intArrayOf(
        0, 4, 4, 5, 5, 7, 7, 8, 8, 8, 9,
        9, 9, 9, 9, 12, 12, 12, 15, 15, 15
    )

    private fun getFiltered(treasures: List<Treasures>?): List<Treasures> {
        val result = mutableListOf<Treasures>()
        if (treasures == null) return result
        if (Utils.monsterType.equals("any", ignoreCase = true)) {
            for (treasure in treasures) {
                val rarity = treasure.rarity ?: 0
                val cost = treasure.cost ?: 0
                if (rarity <= Utils.itemsRarity && cost < sumValue) {
                    result.add(treasure)
                }
            }
        } else {
            for (treasure in treasures) {
                val rarity = treasure.rarity ?: 0
                val cost = treasure.cost ?: 0
                val types = treasure.types
                if (rarity <= Utils.itemsRarity && cost < sumValue && types != null && types.contains(Utils.monsterType)) {
                    result.add(treasure)
                }
            }
        }
        return result
    }

    @JvmStatic
    fun getTreasure(): String {
        if (floor(Math.random() * 100) > Utils.treasurePercentage) {
            return "${BASE}Empty"
        }
        getAllCost()
        val filteredTreasures = getFiltered(Utils.treasureList)
        return BASE + calcTreasure(filteredTreasures)
    }

    private fun calcTreasure(filteredTreasures: List<Treasures>): String {
        val sb = StringBuilder()
        var currentValue = 0
        val targetItemCount = getItemsCount()
        var currentCount = 0
        var maxAttempt = filteredTreasures.size * 2
        val finalList = mutableListOf<Treasures>()
        while (currentCount < targetItemCount && maxAttempt > 0 && filteredTreasures.isNotEmpty()) {
            val currentTreasure = filteredTreasures[Utils.getRandomInt(0, filteredTreasures.size)]
            val cost = currentTreasure.cost ?: 0
            if (currentValue + cost < sumValue) {
                currentValue += cost
                finalList.add(currentTreasure)
                currentCount++
            }
            maxAttempt--
        }
        val map = mutableMapOf<Treasures, Int>()
        for (temp in finalList) {
            map[temp] = (map[temp] ?: 0) + 1
        }
        for ((key, value) in map) {
            if (value > 1) {
                sb.append(value).append("x ")
            }
            sb.append(key.name).append(", ")
        }
        sb.append(Utils.getRandomInt(1, sumValue - currentValue))
        sb.append(" gp")
        return sb.toString()
    }

    private fun getItemsCount(): Int {
        return when (Utils.dungeonDifficulty) {
            0 -> Utils.getRandomInt(0, itemCount[Utils.partyLevel])
            1 -> Utils.getRandomInt(2, itemCount[Utils.partyLevel])
            2 -> Utils.getRandomInt(3, itemCount[Utils.partyLevel])
            3 -> Utils.getRandomInt(4, itemCount[Utils.partyLevel])
            else -> 0
        }
    }

    private fun getAllCost() {
        sumValue = (treasureGP[Utils.partyLevel] * Utils.treasureValue).toInt()
    }
}
