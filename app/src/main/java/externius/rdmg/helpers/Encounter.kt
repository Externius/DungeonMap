package externius.rdmg.helpers

import externius.rdmg.models.Monster
import kotlin.math.floor

internal object Encounter {
    private const val BASE = "Monsters: "
    private const val NONE = "None"
    private const val NOT_SUITABLE = "No suitable monsters with this settings"

    private var filteredMonsters: MutableList<Monster>? = null
    private var sumXP = 0

    private val challengeRatingXP = intArrayOf(
        10, 25, 50, 100, 200, 450, 700, 1100, 1800, 2300, 2900, 3900, 5000, 5900, 7200, 8400,
        10000, 11500, 13000, 15000, 18000, 20000, 22000, 25000, 33000, 41000, 50000, 62000,
        75000, 90000, 105000, 120000, 135000, 155000
    )

    private val multipliers = arrayOf(
        doubleArrayOf(1.0, 1.0),
        doubleArrayOf(2.0, 1.5),
        doubleArrayOf(3.0, 2.0),
        doubleArrayOf(7.0, 2.5),
        doubleArrayOf(11.0, 3.0),
        doubleArrayOf(15.0, 4.0)
    )

    private val challengeRating = listOf(
        "0", "1/8", "1/4", "1/2", "1", "2", "3", "4", "5",
        "6", "7", "8", "9", "10", "11", "12", "13", "14", "15",
        "16", "17", "18", "19", "20", "21", "22", "23", "24", "25",
        "26", "27", "28", "29", "30"
    )

    private val thresholds = arrayOf(
        intArrayOf(0, 0, 0, 0),
        intArrayOf(25, 50, 75, 100),
        intArrayOf(50, 100, 150, 200),
        intArrayOf(75, 150, 225, 400),
        intArrayOf(125, 250, 375, 500),
        intArrayOf(250, 500, 750, 1100),
        intArrayOf(300, 600, 900, 1400),
        intArrayOf(350, 750, 1100, 1700),
        intArrayOf(450, 900, 1400, 2100),
        intArrayOf(550, 1100, 1600, 2400),
        intArrayOf(600, 1200, 1900, 2800),
        intArrayOf(800, 1600, 2400, 3600),
        intArrayOf(1000, 2000, 3000, 4500),
        intArrayOf(1100, 2200, 3400, 5100),
        intArrayOf(1250, 2500, 3800, 5700),
        intArrayOf(1400, 2800, 4300, 6400),
        intArrayOf(1600, 3200, 4800, 7200),
        intArrayOf(2000, 3900, 5900, 8800),
        intArrayOf(2100, 4200, 6300, 9500),
        intArrayOf(2400, 4900, 7300, 10900),
        intArrayOf(2800, 5700, 8500, 12700)
    )

    private val difficulty = intArrayOf(0, 0, 0, 0)

    private fun getMonsterXP(monster: Monster): Int {
        val index = challengeRating.indexOf(monster.challengeRating)
        return if (index != -1) challengeRatingXP[index] else 0
    }

    private fun addMonster(currentXP: Int): String {
        val list = filteredMonsters ?: return NONE
        val monsterCount = list.size
        var monster = 0
        var count: Int
        var allXP: Double
        while (monster < monsterCount) {
            val currentMonster = list[Utils.getRandomInt(0, list.size)]
            list.remove(currentMonster)
            val monsterXP = getMonsterXP(currentMonster)
            for (i in multipliers.indices.reversed()) {
                count = multipliers[i][0].toInt()
                allXP = monsterXP * count * multipliers[i][1]
                if (allXP <= currentXP && count > 1) {
                    return "${count}x ${currentMonster.name} (CR: ${currentMonster.challengeRating}) ${monsterXP * count} XP"
                } else if (allXP <= currentXP) {
                    return "${currentMonster.name} (CR: ${currentMonster.challengeRating}) $monsterXP XP"
                }
            }
            monster++
        }
        return NONE
    }

    private fun checkPossible(): Boolean {
        val list = filteredMonsters ?: return false
        for (monster in list) {
            if (sumXP > getMonsterXP(monster)) {
                return true
            }
        }
        return false
    }

    private fun calcEncounter(): String {
        val result = StringBuilder()
        result.append(BASE)
        if (floor(Math.random() * 100) > 50) {
            result.append(addMonster(sumXP))
        } else {
            val x = Utils.getRandomInt(2, Utils.dungeonDifficulty + 3)
            repeat(x) {
                result.append(addMonster(sumXP / x))
                result.append(", ")
            }
            if (result.length >= 2) {
                result.setLength(result.length - 2)
            }
        }
        return result.toString().replace(", $NONE", "")
    }

    private fun init() {
        difficulty[0] = thresholds[Utils.partyLevel][0] * Utils.partySize
        difficulty[1] = thresholds[Utils.partyLevel][1] * Utils.partySize
        difficulty[2] = thresholds[Utils.partyLevel][2] * Utils.partySize
        difficulty[3] = thresholds[Utils.partyLevel][3] * Utils.partySize
        val list = Utils.monsterList
        filteredMonsters = if (list != null) ArrayList(list) else mutableListOf()
        sumXP = difficulty[Utils.dungeonDifficulty]
    }

    @JvmStatic
    fun getMonster(): String {
        if (Utils.monsterType.equals("none", ignoreCase = true)) {
            return BASE + NONE
        }
        init()
        val checkResult = checkPossible()
        return if (checkResult && floor(Math.random() * 100) <= Utils.monsterPercentage) {
            calcEncounter()
        } else if (!checkResult) {
            BASE + NOT_SUITABLE
        } else {
            BASE + NONE
        }
    }

    @JvmStatic
    fun getRoamingName(count: Int): String {
        return "ROAMING MONSTERS $count# "
    }

    @JvmStatic
    fun getRoamingMonster(): String {
        init()
        return if (checkPossible()) {
            calcEncounter().substring(10) // remove "Monsters: "
        } else {
            NOT_SUITABLE
        }
    }
}
