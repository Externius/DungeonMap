package externius.rdmg.core

import externius.rdmg.helpers.Door
import externius.rdmg.helpers.RoomPosition
import externius.rdmg.helpers.Utils
import externius.rdmg.models.DungeonTile
import externius.rdmg.models.Textures
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.roundToInt

class DungeonNoCorridor : Dungeon {
    var openDoorList: MutableList<DungeonTile> = ArrayList()
        private set
    private var edgeTileList: MutableList<DungeonTile> = ArrayList()
    private var roomStart: MutableList<DungeonTile> = ArrayList()

    constructor(
        dungeonWidth: Int,
        dungeonHeight: Int,
        dungeonSize: Int,
        roomSizePercent: Int
    ) : super() {
        this.dungeonWidth = dungeonWidth
        this.dungeonHeight = dungeonHeight
        this.dungeonSize = dungeonSize
        this.roomSizePercent = roomSizePercent
    }

    override fun init() {
        val imgSizeX = dungeonWidth / dungeonSize
        val imgSizeY = dungeonHeight / dungeonSize
        roomSize = (dungeonSize.toFloat() / 100 * roomSizePercent).roundToInt()
        roomDescription = ArrayList()
        dungeonSize += 2 // because of boundaries
        dungeonTiles = Array(dungeonSize) { i ->
            Array(dungeonSize) { j ->
                DungeonTile(i, j)
            }
        }
        for (i in 1 until dungeonSize - 1) { // set drawing area
            for (j in 1 until dungeonSize - 1) {
                dungeonTiles[i][j] =
                    DungeonTile(i, j, (j - 1) * imgSizeX, (i - 1) * imgSizeY, imgSizeX, imgSizeY)
            }
        }
    }

    override fun generate() {
        init()
        addFirstRoom()
        fillRoomToDoor()
        addEntryPoint()
        addDescription()
    }

    fun addFirstRoom() {
        roomStart = ArrayList()
        openDoorList = ArrayList()
        val x = Utils.getRandomInt(5, dungeonTiles.size - (roomSize + 4))
        val y = Utils.getRandomInt(5, dungeonTiles.size - (roomSize + 4))
        val right = Utils.getRandomInt(2, roomSize + 1)
        val down = Utils.getRandomInt(2, roomSize + 1)
        fillRoom(x, y, down, right)
        roomStart.add(dungeonTiles[x][y])
    }

    private fun setRoomTiles(x: Int, y: Int) {
        dungeonTiles[x][y].texture = Textures.ROOM
        dungeonTiles[x][y].description = " "
        dungeonTiles[x][y].index = roomStart.size
    }

    override fun fillRoom(x: Int, y: Int, right: Int, down: Int) {
        val doorCount = getDoorCount(down, right)
        for (i in 0 until down + 2) { // fill with room_edge texture the bigger boundaries
            for (j in 0 until right + 2) {
                dungeonTiles[x + i - 1][y + j - 1].texture = Textures.ROOM_EDGE
            }
        }
        for (i in 0 until down) { // fill room texture
            for (j in 0 until right) {
                setRoomTiles(x + i, y + j)
                rooms.add(dungeonTiles[x + i][y + j])
            }
        }
        repeat(doorCount) {
            addDoor(x, y, down, right)
        }
    }

    override fun addEntryPoint() {
        var entryIsOk: Boolean
        var x: Int
        var y: Int
        do {
            x = Utils.getRandomInt(1, dungeonTiles.size - 1)
            y = Utils.getRandomInt(1, dungeonTiles.size - 1)
            RoomPosition.checkRoomPosition(dungeonTiles, x, y)
            entryIsOk =
                dungeonTiles[x][y].texture == Textures.ROOM_EDGE && checkPos() && checkNearbyDoor(
                    dungeonTiles[x][y]
                ) && checkEdges(dungeonTiles, x, y)
        } while (!entryIsOk)
        dungeonTiles[x][y].texture = Textures.ENTRY
    }

    private fun checkEdges(dungeonTiles: Array<Array<DungeonTile>>, x: Int, y: Int): Boolean {
        return dungeonTiles[x][y - 1].texture == Textures.ROOM || dungeonTiles[x][y + 1].texture == Textures.ROOM || dungeonTiles[x + 1][y].texture == Textures.ROOM || dungeonTiles[x - 1][y].texture == Textures.ROOM
    }

    fun fillRoomToDoor() {
        while (openDoorList.isNotEmpty()) {
            val i = openDoorList.size - 1
            RoomPosition.checkRoomPosition(dungeonTiles, openDoorList[i].i, openDoorList[i].j)
            if (checkPos()) {
                if (RoomPosition.isUp) {
                    randomFillUpDown(openDoorList[i].i + 1, openDoorList[i].j, openDoorList[i])
                } else if (RoomPosition.isDown) {
                    randomFillUpDown(openDoorList[i].i - 1, openDoorList[i].j, openDoorList[i])
                } else if (RoomPosition.isRight) {
                    randomFillLeftRight(openDoorList[i].i, openDoorList[i].j - 1, openDoorList[i])
                } else if (RoomPosition.isLeft) {
                    randomFillLeftRight(openDoorList[i].i, openDoorList[i].j + 1, openDoorList[i])
                }
            }
            openDoorList.removeAt(i)
        }
    }

    private fun randomFillLeftRight(x: Int, y: Int, door: DungeonTile) {
        val result = checkArea(x, y, door)
        if (result[0] != 0 && result[1] != 0) {
            fillLeftRight(x, y, result[0], result[1])
        }
    }

    private fun fillLeftRight(x: Int, y: Int, down: Int, right: Int) {
        edgeTileList = ArrayList()
        if (right < 0) {
            for (i in right + 1 until 1) {
                fillVertical(x, y + i, down)
            }
        } else {
            for (i in 0 until right) {
                fillVertical(x, y + i, down)
            }
        }
        setVerticalEdge(x, y, right, down)
        setVerticalEdge(x, y, if (right < 0) 1 else -1, down)
        fillDoor(down, right)
        roomStart.add(dungeonTiles[x][y])
    }

    fun addDescription() {
        cleanDoorList()
        setDoorsDescriptions()
        for (room in roomStart) {
            val openList = ArrayList<DungeonTile>()
            val closedList = ArrayList<DungeonTile>()
            var start = room
            addToClosedList(closedList, start)
            addToOpen(start, openList, closedList)
            while (openList.isNotEmpty()) {
                start = openList[0]
                addToClosedList(closedList, start)
                removeFromOpen(openList, start)
                addToOpen(start, openList, closedList)
            }
            Utils.addNCRoomDescription(
                dungeonTiles,
                room.i,
                room.j,
                roomDescription,
                Door.getNCDoorDescription(dungeonTiles, closedList)
            )
        }
    }

    private fun setDoorsDescriptions() {
        for (door in doors) {
            door.description = Door.getNCDoor(door)
        }
    }

    override fun checkTileForOpenList(x: Int, y: Int): Boolean {
        return dungeonTiles[x][y].texture == Textures.ROOM
    }

    private fun addToOpenList(
        x: Int,
        y: Int,
        openList: MutableList<DungeonTile>,
        closedList: List<DungeonTile>
    ) {
        if (checkTileForOpenList(
                x,
                y
            ) && !closedList.contains(dungeonTiles[x][y]) && !openList.contains(dungeonTiles[x][y])
        ) {
            openList.add(dungeonTiles[x][y])
        }
    }

    private fun addToOpen(
        node: DungeonTile,
        openList: MutableList<DungeonTile>,
        closedList: List<DungeonTile>
    ) {
        addToOpenList(node.i, node.j - 1, openList, closedList)
        addToOpenList(node.i - 1, node.j, openList, closedList)
        addToOpenList(node.i + 1, node.j, openList, closedList)
        addToOpenList(node.i, node.j + 1, openList, closedList)
    }

    private fun cleanDoorList() {
        val toDelete = ArrayList<DungeonTile>()
        for (door in doors) {
            if (door.texture == Textures.ROOM_EDGE) {
                toDelete.add(door)
            }
        }
        doors.removeAll(toDelete)
    }

    private fun setVerticalEdge(x: Int, y: Int, right: Int, down: Int) {
        val addToEdgeList = !(right == 1 || right == -1)
        if (down < 0) {
            for (i in down until 2) {
                setRoomEdge(x + i, y + right, addToEdgeList)
            }
        } else {
            for (i in -1 until down + 1) {
                setRoomEdge(x + i, y + right, addToEdgeList)
            }
        }
    }

    private fun fillVertical(x: Int, y: Int, down: Int) {
        if (down < 0) {
            for (i in down + 1 until 1) {
                setRoomTiles(x + i, y)
            }
            setTextureToRoomEdge(x + 1, y)
            addEdgeTileList(x + 1, y)
        } else {
            for (i in 0 until down) {
                setRoomTiles(x + i, y)
            }
            setTextureToRoomEdge(x - 1, y)
            addEdgeTileList(x - 1, y)
        }
        setTextureToRoomEdge(x + down, y)
        addEdgeTileList(x + down, y)
    }

    private fun checkArea(x: Int, y: Int, door: DungeonTile): IntArray {
        var vertical = checkVertical(x, y)
        var horizontal = checkHorizontal(x, y)
        if (checkPossible(vertical, horizontal, door)) {
            val result = getDownRight(vertical, horizontal)
            val down = result[0]
            val right = result[1]
            vertical = checkVerticalOneWay(x, y + right, down)
            horizontal = checkHorizontalOneWay(x + down, y, right)
            if (checkPossibleEnd(vertical, horizontal, door, down, right)) {
                return intArrayOf(down, right)
            }
        }
        return intArrayOf(0, 0)
    }

    private fun checkPossibleEnd(
        vertical: Int,
        horizontal: Int,
        door: DungeonTile,
        down: Int,
        right: Int
    ): Boolean {
        if ((vertical < 0) != (down < 0) || (horizontal < 0) != (right < 0) ||
            abs(vertical) < abs(down) || abs(horizontal) < abs(right)
        ) {
            dungeonTiles[door.i][door.j].texture = Textures.ROOM_EDGE
            return false
        }
        return true
    }

    private fun randomFillUpDown(x: Int, y: Int, door: DungeonTile) {
        val result = checkArea(x, y, door)
        if (result[0] != 0 && result[1] != 0) {
            fillUpDown(x, y, result[0], result[1])
        }
    }

    private fun fillUpDown(x: Int, y: Int, down: Int, right: Int) {
        edgeTileList = ArrayList()
        if (down < 0) {
            for (i in down + 1 until 1) {
                fillHorizontal(x + i, y, right)
            }
        } else {
            for (i in 0 until down) {
                fillHorizontal(x + i, y, right)
            }
        }
        setHorizontalEdge(x, y, right, down)
        setHorizontalEdge(x, y, right, if (down < 0) 1 else -1)
        fillDoor(down, right)
        roomStart.add(dungeonTiles[x][y])
    }

    override fun getDoorCount(down: Int, right: Int): Int {
        return if (abs(down) < 4 || abs(right) < 4) {
            2
        } else {
            Utils.getRandomInt(3, 6)
        }
    }

    private fun fillDoor(down: Int, right: Int) {
        var doorCount = getDoorCount(down, right)
        cleanEdgeTileList()
        var maxTryNumber = edgeTileList.size
        do {
            if (edgeTileList.isEmpty()) break
            val random = Utils.getRandomInt(0, edgeTileList.size)
            if (checkNearbyDoor(edgeTileList[random])) {
                setDoor(edgeTileList[random].i, edgeTileList[random].j)
                doorCount--
            }
            maxTryNumber--
        } while (doorCount > 0 && maxTryNumber > 0)
    }

    private fun checkNearbyDoor(node: DungeonTile): Boolean {
        for (i in node.i - 1..node.i + 1) {
            for (j in node.j - 1..node.j + 1) {
                if (Door.checkNCDoor(dungeonTiles, i, j)) {
                    return false
                }
            }
        }
        return true
    }

    private fun cleanEdgeTileList() {
        val toDelete = ArrayList<DungeonTile>()
        for (tile in edgeTileList) {
            if (checkRooms(tile.i, tile.j)) {
                toDelete.add(tile)
            }
        }
        edgeTileList.removeAll(toDelete)
    }

    private fun checkRooms(x: Int, y: Int): Boolean {
        return dungeonTiles[x][y - 1].texture != Textures.ROOM && dungeonTiles[x][y + 1].texture != Textures.ROOM && dungeonTiles[x + 1][y].texture != Textures.ROOM && dungeonTiles[x - 1][y].texture != Textures.ROOM
    }

    private fun setHorizontalEdge(x: Int, y: Int, right: Int, down: Int) {
        val addToEdgeList = !(down == 1 || down == -1)
        if (right < 0) {
            for (i in right until 2) {
                setRoomEdge(x + down, y + i, addToEdgeList)
            }
        } else {
            for (i in -1 until right + 1) {
                setRoomEdge(x + down, y + i, addToEdgeList)
            }
        }
    }

    private fun setRoomEdge(x: Int, y: Int, addToEdgeList: Boolean) {
        if (!Door.checkNCDoor(dungeonTiles, x, y) && addToEdgeList) {
            setTextureToRoomEdge(x, y)
            addEdgeTileList(x, y)
        } else if (!Door.checkNCDoor(dungeonTiles, x, y)) {
            setTextureToRoomEdge(x, y)
        }
    }

    private fun setTextureToRoomEdge(x: Int, y: Int) {
        dungeonTiles[x][y].texture = Textures.ROOM_EDGE
    }

    private fun addEdgeTileList(x: Int, y: Int) {
        edgeTileList.add(dungeonTiles[x][y])
    }

    private fun fillHorizontal(x: Int, y: Int, right: Int) {
        if (right < 0) {
            for (i in right + 1 until 1) {
                setRoomTiles(x, y + i)
            }
            setTextureToRoomEdge(x, y + 1)
            addEdgeTileList(x, y + 1)
        } else {
            for (i in 0 until right) {
                setRoomTiles(x, y + i)
            }
            setTextureToRoomEdge(x, y - 1)
            addEdgeTileList(x, y - 1)
        }
        setTextureToRoomEdge(x, y + right)
        addEdgeTileList(x, y + right)
    }

    private fun getDownRight(vertical: Int, horizontal: Int): IntArray {
        var down = Utils.getRandomInt(2, min(abs(vertical), roomSize))
        var right = Utils.getRandomInt(2, min(abs(horizontal), roomSize))
        if (vertical < 0) {
            down = -down
        }
        if (horizontal < 0) {
            right = -right
        }
        return intArrayOf(down, right)
    }

    private fun checkPossible(vertical: Int, horizontal: Int, door: DungeonTile): Boolean {
        if (vertical == 0 || horizontal == 0) {
            dungeonTiles[door.i][door.j].texture = Textures.ROOM_EDGE
            return false
        }
        return true
    }

    private fun checkUp(x: Int, y: Int): Int {
        var tile: Boolean
        var edge: Boolean
        var temp = x
        var count = 0
        do {
            tile = checkTile(temp, y)
            edge = checkDungeonTilesEdge(temp, y)
            temp--
            count--
        } while (!tile && !edge)
        if (edge) {
            count++
        }
        return count
    }

    private fun checkDown(x: Int, y: Int): Int {
        var tile: Boolean
        var edge: Boolean
        var temp = x
        var count = 0
        do {
            tile = checkTile(temp, y)
            edge = checkDungeonTilesEdge(temp, y)
            temp++
            count++
        } while (!tile && !edge)
        if (edge) {
            count--
        }
        return count
    }

    private fun checkVerticalOneWay(x: Int, y: Int, down: Int): Int {
        val count = if (down < 0) {
            checkUp(x, y)
        } else {
            checkDown(x, y)
        }
        return if (abs(count) > 2) count else 0
    }

    private fun checkLeft(x: Int, y: Int): Int {
        var tile: Boolean
        var edge: Boolean
        var temp = y
        var count = 0
        do {
            tile = checkTile(x, temp)
            edge = checkDungeonTilesEdge(x, temp)
            temp--
            count--
        } while (!tile && !edge)
        if (edge) {
            count++
        }
        return count
    }

    private fun checkRight(x: Int, y: Int): Int {
        var tile: Boolean
        var edge: Boolean
        var temp = y
        var count = 0
        do {
            tile = checkTile(x, temp)
            edge = checkDungeonTilesEdge(x, temp)
            temp++
            count++
        } while (!tile && !edge)
        if (edge) {
            count--
        }
        return count
    }

    private fun checkHorizontalOneWay(x: Int, y: Int, right: Int): Int {
        val count = if (right < 0) {
            checkLeft(x, y)
        } else {
            checkRight(x, y)
        }
        return if (abs(count) > 2) count else 0
    }

    private fun checkHorizontal(x: Int, y: Int): Int {
        return getCheckResult(checkLeft(x, y), checkRight(x, y))
    }

    private fun checkDungeonTilesEdge(x: Int, y: Int): Boolean {
        return dungeonTiles[x][y].texture == Textures.EDGE
    }

    private fun checkTile(x: Int, y: Int): Boolean {
        return dungeonTiles[x][y].texture == Textures.ROOM_EDGE || Door.checkNCDoor(
            dungeonTiles,
            x,
            y
        )
    }

    private fun checkVertical(x: Int, y: Int): Int {
        return getCheckResult(checkUp(x, y), checkDown(x, y))
    }

    private fun getCheckResult(x: Int, y: Int): Int {
        if (abs(x) >= abs(y) && abs(x) > 2) {
            return x
        }
        if (abs(y) > abs(x) && abs(y) > 2) {
            return y
        }
        return 0
    }

    private fun checkPos(): Boolean {
        return !(RoomPosition.isUp && RoomPosition.isDown || RoomPosition.isLeft && RoomPosition.isRight)
    }

    override fun checkDoor(x: Int, y: Int): Boolean {
        for (i in x - 1..x + 1) {
            for (j in y - 1..y + 1) {
                if (Door.checkNCDoor(dungeonTiles, i, j)) {
                    return false
                }
            }
        }
        return checkEnvironment(x, y)
    }

    override fun setDoor(x: Int, y: Int) {
        if (Utils.getRandomInt(0, 101) < 40) {
            dungeonTiles[x][y].texture = Textures.NO_CORRIDOR_DOOR_TRAPPED
        } else if (Utils.getRandomInt(0, 101) < 50) {
            dungeonTiles[x][y].texture = Textures.NO_CORRIDOR_DOOR_LOCKED
        } else {
            dungeonTiles[x][y].texture = Textures.NO_CORRIDOR_DOOR
        }
        openDoorList.add(dungeonTiles[x][y])
        doors.add(dungeonTiles[x][y])
    }
}
