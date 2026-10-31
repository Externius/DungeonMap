package externius.rdmg.helpers

import externius.rdmg.models.DungeonTile
import externius.rdmg.models.Textures

object Door {
    private var wCount = 0
    private var sCount = 0
    private var eCount = 0
    private var nCount = 0

    private val doorTypes = arrayOf(
        "Crystal", "Wooden", "Stone", "Iron", "Steel", "Mithral", "Adamantine"
    )

    private val doorAC = intArrayOf(13, 15, 17, 19, 19, 21, 23)
    private val doorHP = intArrayOf(10, 10, 15, 15, 18, 18, 27)
    private val lockDifficulty = intArrayOf(5, 10, 15, 20, 25, 25, 30)

    private fun getDoorDC(): Int {
        return when (Utils.dungeonDifficulty) {
            0 -> Utils.getRandomInt(0, 2)
            1 -> Utils.getRandomInt(1, 4)
            2 -> Utils.getRandomInt(1, 6)
            3 -> Utils.getRandomInt(2, 7)
            else -> 0
        }
    }

    private fun getState(texture: Textures, x: Int): String {
        return when (texture) {
            Textures.NO_CORRIDOR_DOOR_LOCKED, Textures.DOOR_LOCKED ->
                " Locked Door (AC ${doorAC[x]}, HP ${doorHP[x]}, DC ${lockDifficulty[x]} to unlock)"
            Textures.NO_CORRIDOR_DOOR_TRAPPED, Textures.DOOR_TRAPPED ->
                " Trapped Door (AC ${doorAC[x]}, HP ${doorHP[x]}) ${Trap.getCurrentTrap(true)}"
            else -> " Open Door (AC ${doorAC[x]}, HP ${doorHP[x]})"
        }
    }

    private fun getDoorText(texture: Textures, x: Int): String {
        return if (RoomPosition.isUp) {
            "South Entry #${sCount++}: ${doorTypes[x]}${getState(texture, x)}\n"
        } else if (RoomPosition.isDown) {
            "North Entry #${nCount++}: ${doorTypes[x]}${getState(texture, x)}\n"
        } else if (RoomPosition.isRight) {
            "West Entry #${wCount++}: ${doorTypes[x]}${getState(texture, x)}\n"
        } else {
            "East Entry #${eCount++}: ${doorTypes[x]}${getState(texture, x)}\n"
        }
    }

    @JvmStatic
    fun getDoorDescription(dungeon: Array<Array<DungeonTile>>, doorList: List<DungeonTile>): String {
        val sb = StringBuilder()
        var start: Int
        var end: Int
        wCount = 1
        sCount = 1
        eCount = 1
        nCount = 1
        for (door in doorList) {
            start = sb.length
            RoomPosition.checkRoomPosition(dungeon, door.i, door.j)
            sb.append(getDoorText(door.texture, getDoorDC()))
            end = sb.length
            dungeon[door.i][door.j].description = sb.substring(start, end)
        }
        if (sb.isNotEmpty()) {
            sb.setLength(sb.length - 1)
        }
        return sb.toString()
    }

    @JvmStatic
    fun getNCDoor(door: DungeonTile): String {
        val x = getDoorDC()
        return ": ${doorTypes[x]}${getState(door.texture, x)}\n"
    }

    @JvmStatic
    fun getNCDoorDescription(dungeon: Array<Array<DungeonTile>>, closedList: List<DungeonTile>): String {
        wCount = 1
        sCount = 1
        eCount = 1
        nCount = 1
        val sb = StringBuilder()
        for (tile in closedList) {
            if (checkNCDoor(dungeon, tile.i, tile.j - 1)) {
                sb.append("West Entry #").append(wCount++)
                    .append(dungeon[tile.i][tile.j - 1].description)
            } else if (checkNCDoor(dungeon, tile.i, tile.j + 1)) {
                sb.append("East Entry #").append(eCount++)
                    .append(dungeon[tile.i][tile.j + 1].description)
            } else if (checkNCDoor(dungeon, tile.i + 1, tile.j)) {
                sb.append("South Entry #").append(sCount++)
                    .append(dungeon[tile.i + 1][tile.j].description)
            } else if (checkNCDoor(dungeon, tile.i - 1, tile.j)) {
                sb.append("North Entry #").append(nCount++)
                    .append(dungeon[tile.i - 1][tile.j].description)
            }
        }
        if (sb.isNotEmpty()) {
            sb.setLength(sb.length - 1)
        }
        return sb.toString()
    }

    @JvmStatic
    fun checkNCDoor(dungeon: Array<Array<DungeonTile>>, x: Int, y: Int): Boolean {
        val tex = dungeon[x][y].texture
        return tex == Textures.NO_CORRIDOR_DOOR || tex == Textures.NO_CORRIDOR_DOOR_LOCKED || tex == Textures.NO_CORRIDOR_DOOR_TRAPPED
    }
}
