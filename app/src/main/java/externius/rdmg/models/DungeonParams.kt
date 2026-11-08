package externius.rdmg.models

import externius.rdmg.helpers.Utils

data class DungeonParams(
    val partyLevel: Int = 0,
    val partySize: Int = 0,
    val dungeonDifficulty: Int = 0,
    val treasureValue: Double = 0.0,
    val itemsRarity: Int = 0,
    val monsterType: String = "",
    val monsterList: List<Monster> = emptyList(),
    val treasureList: List<Treasures> = emptyList()
) {
    val treasurePercentage: Int
        get() = when (dungeonDifficulty) {
            0 -> Utils.getRandomInt(20, 71)
            1 -> Utils.getRandomInt(30, 81)
            2 -> Utils.getRandomInt(40, 91)
            3 -> Utils.getRandomInt(50, 101)
            else -> 0
        }

    val monsterPercentage: Int
        get() = when (dungeonDifficulty) {
            0 -> Utils.getRandomInt(40, 81)
            1 -> Utils.getRandomInt(50, 91)
            2 -> Utils.getRandomInt(60, 101)
            3 -> Utils.getRandomInt(70, 101)
            else -> 0
        }
}
