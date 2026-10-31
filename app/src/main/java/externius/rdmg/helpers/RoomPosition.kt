package externius.rdmg.helpers

import externius.rdmg.models.DungeonTile
import externius.rdmg.models.Textures

object RoomPosition {
    @JvmStatic var isUp: Boolean = false
        private set
    @JvmStatic var isDown: Boolean = false
        private set
    @JvmStatic var isLeft: Boolean = false
        private set
    @JvmStatic var isRight: Boolean = false
        private set

    @JvmStatic
    fun checkRoomPosition(dungeon: Array<Array<DungeonTile>>, x: Int, y: Int) {
        isUp = false
        isDown = false
        isLeft = false
        isRight = false
        if (dungeon[x][y - 1].texture == Textures.ROOM) { // left
            isLeft = true
        }
        if (dungeon[x][y + 1].texture == Textures.ROOM) { // right
            isRight = true
        }
        if (dungeon[x + 1][y].texture == Textures.ROOM) { // bottom
            isDown = true
        }
        if (dungeon[x - 1][y].texture == Textures.ROOM) { // top
            isUp = true
        }
    }
}
