package externius.rdmg

import externius.rdmg.models.DungeonTile
import externius.rdmg.models.Textures

internal object DrawTestDungeon {

    @JvmStatic
    fun draw(dungeonTiles: Array<Array<DungeonTile>>) {
        for (row in dungeonTiles) {
            printRow(row)
        }
        println()
    }

    private fun printRow(row: Array<DungeonTile>) {
        for (i in row) {
            when (i.texture) {
                Textures.EDGE -> print("X")
                Textures.ROOM_EDGE -> print("#")
                Textures.MARBLE -> print(" ")
                Textures.ROOM -> print(".")
                Textures.NO_CORRIDOR_DOOR, Textures.NO_CORRIDOR_DOOR_LOCKED,
                Textures.NO_CORRIDOR_DOOR_TRAPPED, Textures.DOOR, Textures.DOOR_LOCKED,
                Textures.DOOR_TRAPPED -> print("D")
                Textures.CORRIDOR -> print("-")
                Textures.ENTRY -> print("E")
                Textures.TRAP -> print("T")
                Textures.ROAMING_MONSTER -> print("M")
            }
        }
        println()
    }
}
