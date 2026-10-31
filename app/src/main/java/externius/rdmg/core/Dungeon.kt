package externius.rdmg.core

import externius.rdmg.helpers.Utils
import externius.rdmg.models.DungeonTile
import externius.rdmg.models.RoamingMonsterDescription
import externius.rdmg.models.RoomDescription
import externius.rdmg.models.Textures
import externius.rdmg.models.TrapDescription
import kotlin.math.abs
import kotlin.math.roundToInt

open class Dungeon {
    val rooms: MutableList<DungeonTile> = ArrayList()
    lateinit var dungeonTiles: Array<Array<DungeonTile>>
    var roomDescription: MutableList<RoomDescription> = ArrayList()
    var dungeonWidth: Int = 0
    var dungeonHeight: Int = 0
    var dungeonSize: Int = 0
    var roomSizePercent: Int = 0
    var roomSize: Int = 0
    var doors: MutableList<DungeonTile> = ArrayList()

    private var result: MutableList<DungeonTile> = ArrayList()
    private var corridors: MutableList<DungeonTile> = ArrayList()
    var trapDescription: MutableList<TrapDescription> = ArrayList()
        private set
    var roamingMonsterDescriptions: MutableList<RoamingMonsterDescription> = ArrayList()
        private set

    private var roomDensity: Int = 0
    private var trapPercent: Int = 0
    private var roamingPercent: Int = 0
    private var trapCount: Int = 0
    private var roamingCount: Int = 0
    private var roomCount: Int = 0
    private var hasDeadEnds: Boolean = false

    constructor()

    constructor(
        dungeonWidth: Int,
        dungeonHeight: Int,
        dungeonSize: Int,
        roomDensity: Int,
        roomSizePercent: Int,
        trapPercent: Int,
        hasDeadEnds: Boolean,
        roamingPercent: Int
    ) {
        this.dungeonWidth = dungeonWidth
        this.dungeonHeight = dungeonHeight
        this.dungeonSize = dungeonSize
        this.roomDensity = roomDensity
        this.roomSizePercent = roomSizePercent
        this.trapPercent = trapPercent
        this.roamingPercent = roamingPercent
        this.hasDeadEnds = hasDeadEnds
    }

    open fun generate() {
        init()
        generateRoom()
        addEntryPoint()
        generateCorridors()
        if (hasDeadEnds) {
            addDeadEnds()
        }
        addCorridorItem(trapCount, Item.TRAP)
        addCorridorItem(roamingCount, Item.ROAMING_MONSTER)
    }

    open fun init() {
        corridors = ArrayList()
        val imgSizeX = dungeonWidth / dungeonSize
        val imgSizeY = dungeonHeight / dungeonSize
        roomCount = ((dungeonSize.toFloat() / 100) * roomDensity.toFloat()).roundToInt()
        roomSize =
            ((dungeonSize - (dungeonSize * 0.35f).roundToInt()).toFloat() / 100 * roomSizePercent).roundToInt()
        roomDescription = ArrayList()
        trapDescription = ArrayList()
        roamingMonsterDescriptions = ArrayList()
        trapCount = dungeonSize * trapPercent / 100
        roamingCount = dungeonSize * roamingPercent / 100
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

    fun addCorridorItem(inCount: Int, item: Item) {
        var count = 0
        var maxAttempts = corridors.size
        while (inCount > count && corridors.isNotEmpty() && maxAttempts > 0) {
            val x = Utils.getRandomInt(0, corridors.size)
            val i = corridors[x].i
            val j = corridors[x].j
            if (dungeonTiles[i][j].texture == Textures.CORRIDOR) {
                addItem(i, j, item)
                count++
                maxAttempts = corridors.size
            }
            maxAttempts--
        }
    }

    private fun addItem(x: Int, y: Int, item: Item) {
        when (item) {
            Item.TRAP -> addTrap(x, y)
            Item.ROAMING_MONSTER -> addRoamingMonster(x, y)
        }
    }

    private fun addTrap(x: Int, y: Int) {
        dungeonTiles[x][y].texture = Textures.TRAP
        Utils.addTrapDescription(dungeonTiles, x, y, trapDescription)
    }

    private fun addRoamingMonster(x: Int, y: Int) {
        dungeonTiles[x][y].texture = Textures.ROAMING_MONSTER
        Utils.addRoamingMonsterDescription(dungeonTiles, x, y, roamingMonsterDescriptions)
    }

    fun addDeadEnds() {
        val deadEnds = generateDeadEnds()
        if (doors.isEmpty()) return
        val firstDoor = doors[0]
        for (end in deadEnds) {
            doors = ArrayList()
            doors.add(firstDoor)
            doors.add(end)
            generateCorridors()
        }
    }

    private fun generateDeadEnds(): List<DungeonTile> {
        val count = roomCount / 2
        var deadEndsCount = 0
        val deadEnds = ArrayList<DungeonTile>()
        val croppedDungeonTiles = Array(dungeonTiles.size - 4) { i ->
            Array(dungeonTiles.size - 4) { j ->
                dungeonTiles[i + 2][j + 2]
            }
        }
        val dungeonList = twoDArrayToList(croppedDungeonTiles)
        dungeonList.removeAll(rooms)
        dungeonList.removeAll(doors)
        dungeonList.removeAll(corridors)
        var maxAttempt = dungeonList.size * 2
        do {
            if (dungeonList.isEmpty()) break
            val tile = dungeonList[Utils.getRandomInt(0, dungeonList.size)]
            if (checkTileForDeadEnd(tile.i, tile.j)) {
                dungeonTiles[tile.i][tile.j].texture = Textures.CORRIDOR
                deadEnds.add(dungeonTiles[tile.i][tile.j])
                deadEndsCount++
            }
            maxAttempt--
        } while (count != deadEndsCount && maxAttempt > 0)
        return deadEnds
    }

    private fun <T> twoDArrayToList(twoDArray: Array<Array<T>>): MutableList<T> {
        val list = ArrayList<T>()
        for (array in twoDArray) {
            list.addAll(array)
        }
        return list
    }

    private fun checkTileForDeadEnd(x: Int, y: Int): Boolean {
        for (i in x - 1..x + 1) {
            for (j in y - 1..y + 1) {
                if (dungeonTiles[i][j].texture != Textures.MARBLE) {
                    return false
                }
            }
        }
        return true
    }

    open fun addEntryPoint() {
        var entryIsOk: Boolean
        var x: Int
        var y: Int
        do {
            x = Utils.getRandomInt(1, dungeonTiles.size - 1)
            y = Utils.getRandomInt(1, dungeonTiles.size - 1)
            entryIsOk = dungeonTiles[x][y].texture == Textures.MARBLE
        } while (!entryIsOk)
        dungeonTiles[x][y].texture = Textures.ENTRY
        doors.add(dungeonTiles[x][y])
    }

    open fun generateCorridors() {
        for (d in 0 until doors.size - 1) {
            result = ArrayList()
            val openList = ArrayList<DungeonTile>()
            val closedList = ArrayList<DungeonTile>()
            var start = doors[d]
            val end = doors[d + 1]
            for (i in 1 until dungeonTiles.size - 1) {
                for (j in 1 until dungeonTiles.size - 1) {
                    dungeonTiles[i][j].h = Utils.manhattan(abs(i - end.i), abs(j - end.j))
                    dungeonTiles[i][j].g = 0
                    dungeonTiles[i][j].parent = null
                    dungeonTiles[i][j].f = 9999
                }
            }
            addToClosedList(closedList, start)
            addToOpen(start, openList, closedList, end)
            while (result.isEmpty() && openList.isNotEmpty()) {
                start = openList[0]
                addToClosedList(closedList, start)
                removeFromOpen(openList, start)
                addToOpen(start, openList, closedList, end)
            }
            setPath()
        }
    }

    private fun setPath() {
        for (tile in result) {
            if (tile.texture == Textures.MARBLE) {
                dungeonTiles[tile.i][tile.j].texture = Textures.CORRIDOR
                corridors.add(tile)
            }
        }
    }

    internal open fun removeFromOpen(openList: MutableList<DungeonTile>, node: DungeonTile) {
        openList.remove(node)
    }

    private fun addToOpen(
        node: DungeonTile,
        openList: MutableList<DungeonTile>,
        closedList: List<DungeonTile>,
        end: DungeonTile
    ) {
        addToOpenList(node, node.i, node.j - 1, openList, closedList, end)
        addToOpenList(node, node.i - 1, node.j, openList, closedList, end)
        addToOpenList(node, node.i + 1, node.j, openList, closedList, end)
        addToOpenList(node, node.i, node.j + 1, openList, closedList, end)
        calcGValue(openList)
        calcFValue(openList)
    }

    private fun calcFValue(openList: MutableList<DungeonTile>) {
        for (tile in openList) {
            tile.f = tile.g + tile.h
        }
        openList.sortBy { it.f }
    }

    private fun calcGValue(openList: List<DungeonTile>) {
        for (tile in openList) {
            val parent = tile.parent
            if (parent != null) {
                tile.g = parent.g + MOVEMENT
            }
        }
    }

    private fun addToOpenList(
        node: DungeonTile,
        x: Int,
        y: Int,
        openList: MutableList<DungeonTile>,
        closedList: List<DungeonTile>,
        end: DungeonTile
    ) {
        if (!checkEnd(node, x, y, end)) {
            checkG(node, x, y, openList)
            if (checkTileForOpenList(
                    x,
                    y
                ) && !closedList.contains(dungeonTiles[x][y]) && !openList.contains(dungeonTiles[x][y])
            ) {
                setParent(node, x, y)
                openList.add(dungeonTiles[x][y])
            }
        }
    }

    internal open fun checkTileForOpenList(x: Int, y: Int): Boolean {
        return dungeonTiles[x][y].h != 0 && dungeonTiles[x][y].texture != Textures.ROOM && dungeonTiles[x][y].texture != Textures.ROOM_EDGE
    }

    private fun setParent(node: DungeonTile, x: Int, y: Int) {
        dungeonTiles[x][y].parent = node
    }

    private fun checkG(node: DungeonTile, x: Int, y: Int, openList: List<DungeonTile>) {
        if (openList.contains(dungeonTiles[x][y]) && dungeonTiles[x][y].g > (node.g + MOVEMENT)) {
            setParent(node, x, y)
        }
    }

    private fun checkEnd(node: DungeonTile, x: Int, y: Int, end: DungeonTile): Boolean {
        if (end.i == x && end.j == y) {
            setParent(node, x, y)
            getParents(node)
            return true
        }
        return false
    }

    private fun getParents(node: DungeonTile) {
        result.add(node)
        var parent: DungeonTile? = node
        while (parent?.parent != null) {
            parent = parent.parent
            if (parent != null) {
                result.add(parent)
            }
        }
    }

    internal open fun addToClosedList(closedList: MutableList<DungeonTile>, node: DungeonTile) {
        closedList.add(dungeonTiles[node.i][node.j])
    }

    open fun generateRoom() {
        var coordinates: IntArray
        repeat(roomCount) {
            coordinates = setTilesForRoom()
            if (coordinates[0] != 0) {
                fillRoom(coordinates[0], coordinates[1], coordinates[2], coordinates[3])
            }
        }
    }

    internal open fun getDoorCount(down: Int, right: Int): Int {
        return if (down < 4 || right < 4) {
            Utils.getRandomInt(1, 3)
        } else {
            Utils.getRandomInt(2, 5)
        }
    }

    internal open fun fillRoom(x: Int, y: Int, right: Int, down: Int) {
        val doorCount = getDoorCount(down, right)
        for (i in 0 until down + 2) {
            for (j in 0 until right + 2) {
                dungeonTiles[x + i - 1][y + j - 1].texture = Textures.ROOM_EDGE
            }
        }
        for (i in 0 until down) {
            for (j in 0 until right) {
                dungeonTiles[x + i][y + j].texture = Textures.ROOM
                rooms.add(dungeonTiles[x + i][y + j])
                dungeonTiles[x + i][y + j].description = " "
                dungeonTiles[x + i][y + j].index = roomDescription.size
            }
        }
        val currentSize = doors.size
        repeat(doorCount) {
            addDoor(x, y, down, right)
        }
        val newSize = doors.size
        val currentDoors = ArrayList(doors.subList(currentSize, newSize))
        Utils.addRoomDescription(dungeonTiles, x, y, roomDescription, currentDoors)
    }

    internal open fun addDoor(x: Int, y: Int, down: Int, right: Int) {
        var doorIsOK: Boolean
        var doorX: Int
        var doorY: Int
        do {
            doorX = Utils.getRandomInt(x, x + down)
            doorY = Utils.getRandomInt(y, y + right)
            doorIsOK = checkDoor(doorX, doorY)
        } while (!doorIsOK)
    }

    open fun checkDoor(x: Int, y: Int): Boolean {
        for (i in x - 1..x + 1) {
            for (j in y - 1..y + 1) {
                val tex = dungeonTiles[i][j].texture
                if (tex == Textures.DOOR || tex == Textures.DOOR_LOCKED || tex == Textures.DOOR_TRAPPED) {
                    return false
                }
            }
        }
        return checkEnvironment(x, y)
    }

    internal open fun checkEnvironment(x: Int, y: Int): Boolean {
        if (dungeonTiles[x][y - 1].texture == Textures.ROOM_EDGE) {
            setDoor(x, y - 1)
            return true
        } else if (dungeonTiles[x][y + 1].texture == Textures.ROOM_EDGE) {
            setDoor(x, y + 1)
            return true
        } else if (dungeonTiles[x + 1][y].texture == Textures.ROOM_EDGE) {
            setDoor(x + 1, y)
            return true
        } else if (dungeonTiles[x - 1][y].texture == Textures.ROOM_EDGE) {
            setDoor(x - 1, y)
            return true
        }
        return false
    }

    internal open fun setDoor(x: Int, y: Int) {
        if (Utils.getRandomInt(0, 101) < 40) {
            dungeonTiles[x][y].texture = Textures.DOOR_TRAPPED
        } else if (Utils.getRandomInt(0, 101) < 50) {
            dungeonTiles[x][y].texture = Textures.DOOR_LOCKED
        } else {
            dungeonTiles[x][y].texture = Textures.DOOR
        }
        doors.add(dungeonTiles[x][y])
    }

    private fun setTilesForRoom(): IntArray {
        var roomIsOk: Boolean
        var x: Int
        var y: Int
        val max = dungeonTiles.size - (roomSize + 2)
        var failSafeCount = dungeonTiles.size * dungeonTiles.size / 2
        var right: Int
        var down: Int
        do {
            x = Utils.getRandomInt(3, max)
            y = Utils.getRandomInt(3, max)
            right = Utils.getRandomInt(2, roomSize + 1)
            down = Utils.getRandomInt(2, roomSize + 1)
            roomIsOk = checkTileGoodForRoom(x - 2, y - 2, right + 2, down + 2)
            failSafeCount--
        } while (!roomIsOk && failSafeCount > 0)
        return if (failSafeCount > 0) {
            intArrayOf(x, y, right, down)
        } else {
            intArrayOf(0, 0, 0, 0)
        }
    }

    private fun checkTileGoodForRoom(x: Int, y: Int, right: Int, down: Int): Boolean {
        val maxX = x + down + 2
        val maxY = y + right + 2
        for (i in x until maxX) {
            for (j in y until maxY) {
                if (checkIsRoom(i, j)) {
                    return false
                }
            }
        }
        return true
    }

    private fun checkIsRoom(x: Int, y: Int): Boolean {
        val tex = dungeonTiles[x][y].texture
        return tex == Textures.ROOM || tex == Textures.ROOM_EDGE
    }

    enum class Item {
        TRAP, ROAMING_MONSTER
    }

    companion object {
        private const val MOVEMENT = 10
    }
}
