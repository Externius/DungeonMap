package externius.rdmg.views

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import externius.rdmg.R
import externius.rdmg.core.Dungeon
import externius.rdmg.core.DungeonNoCorridor
import externius.rdmg.helpers.Utils
import externius.rdmg.models.DungeonTile
import externius.rdmg.models.RoamingMonsterDescription
import externius.rdmg.models.RoomDescription
import externius.rdmg.models.Textures
import externius.rdmg.models.TrapDescription
import androidx.core.graphics.scale
import kotlin.math.roundToLong

class DungeonMapView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var marble: Bitmap? = null
    private var corridor: Bitmap? = null
    private var door: Bitmap? = null
    private var doorLocked: Bitmap? = null
    private var doorTrapped: Bitmap? = null
    private var room: Bitmap? = null
    private var entry: Bitmap? = null
    private var trap: Bitmap? = null
    private var roamingMonster: Bitmap? = null
    private var ncDoor: Bitmap? = null
    private var ncDoorLocked: Bitmap? = null
    private var ncDoorTrapped: Bitmap? = null
    private var roomEdge: Bitmap? = null

    var treasureValue: Double = 0.0
    var itemsRarity: Int = 0
    var dungeonTiles: Array<Array<DungeonTile>>? = null
    private val paint = Paint()

    var roomDescription: List<RoomDescription> = ArrayList()
    var trapDescription: List<TrapDescription> = ArrayList()
    var roamingMonsterDescription: List<RoamingMonsterDescription> = ArrayList()

    var jsonMonster: String? = null
    var jsonTreasure: String? = null
    var dungeonWidth: Int = 0
    var dungeonHeight: Int = 0
    var dungeonSize: Int = 0
    var roomDensity: Int = 0
    var roomSizePercent: Int = 0
    var trapPercent: Int = 0
    var roamingPercent: Int = 0
    var partyLevel: Int = 0
    var hasCorridor: Boolean = false
    var hasDeadEnds: Boolean = false
    var dungeonDifficulty: Int = 0
    var partySize: Int = 0
    var monsterType: String? = null
    var theme: String? = null

    fun loadDungeon(
        dungeonTiles: Array<Array<DungeonTile>>,
        roomDescription: List<RoomDescription>,
        trapDescription: List<TrapDescription>,
        hasCorridor: Boolean,
        roamingMonsterDescription: List<RoamingMonsterDescription>
    ) {
        this.dungeonTiles = dungeonTiles
        this.roomDescription = roomDescription
        this.trapDescription = trapDescription
        this.roamingMonsterDescription = roamingMonsterDescription
        setBitmaps(hasCorridor)
        setPaint()
    }

    fun generateDungeon() {
        init()
        setPaint()
        setBitmaps(hasCorridor)
        if (hasCorridor) {
            val dungeon = Dungeon(
                dungeonWidth, dungeonHeight, dungeonSize, roomDensity,
                roomSizePercent, trapPercent, hasDeadEnds, roamingPercent
            )
            dungeon.generate()
            roomDescription = dungeon.roomDescription
            trapDescription = dungeon.trapDescription
            roamingMonsterDescription = dungeon.roamingMonsterDescriptions
            dungeonTiles = dungeon.dungeonTiles
        } else {
            val dungeonNoCorridor = DungeonNoCorridor(
                dungeonWidth, dungeonHeight, dungeonSize, roomSizePercent
            )
            dungeonNoCorridor.generate()
            roomDescription = dungeonNoCorridor.roomDescription
            dungeonTiles = dungeonNoCorridor.dungeonTiles
        }
    }

    private fun setPaint() {
        paint.textSize = getFontSize(dungeonSize).toFloat()
    }

    private fun init() {
        Utils.partySize = partySize
        Utils.partyLevel = partyLevel
        Utils.dungeonDifficulty = dungeonDifficulty
        Utils.monsterType = monsterType ?: ""
        Utils.setJsonMonster(jsonMonster ?: "")
        Utils.setJsonTreasure(jsonTreasure ?: "")
        Utils.treasureValue = treasureValue
        Utils.itemsRarity = itemsRarity
    }

    private fun setBitmaps(hasCorridor: Boolean) {
        corridor = BitmapFactory.decodeResource(resources, R.drawable.corridor_dark)
        door = BitmapFactory.decodeResource(resources, R.drawable.door_dark)
        doorLocked = BitmapFactory.decodeResource(resources, R.drawable.door_locked_dark)
        doorTrapped = BitmapFactory.decodeResource(resources, R.drawable.door_trapped_dark)
        trap = BitmapFactory.decodeResource(resources, R.drawable.trap_dark)
        roamingMonster = BitmapFactory.decodeResource(resources, R.drawable.monster_dark)
        entry = BitmapFactory.decodeResource(resources, R.drawable.entry_dark)
        when (theme) {
            "light" -> setTheme(
                hasCorridor, R.drawable.marble_light, R.drawable.room_edge_light,
                R.drawable.room_light, R.drawable.nc_door_light, R.drawable.nc_door_locked_light,
                R.drawable.nc_door_trapped_light
            )

            "minimal" -> setTheme(
                hasCorridor, R.drawable.marble_white, R.drawable.marble_white,
                R.drawable.room_white, R.drawable.nc_door_white, R.drawable.nc_door_locked_white,
                R.drawable.nc_door_trapped_white
            )

            else -> setTheme(
                hasCorridor, R.drawable.marble_dark, R.drawable.room_edge_dark,
                R.drawable.room_dark, R.drawable.nc_door_dark, R.drawable.nc_door_locked_dark,
                R.drawable.nc_door_trapped_dark
            )
        }
    }

    private fun setTheme(
        hasCorridor: Boolean,
        marbleID: Int,
        roomEdgeID: Int,
        roomID: Int,
        ncDoorID: Int,
        ncDoorLockedID: Int,
        ncDoorTrappedID: Int
    ) {
        roomEdge = if (hasCorridor) {
            BitmapFactory.decodeResource(resources, marbleID)
        } else {
            BitmapFactory.decodeResource(resources, roomEdgeID)
        }
        marble = BitmapFactory.decodeResource(resources, marbleID)
        room = BitmapFactory.decodeResource(resources, roomID)
        ncDoor = BitmapFactory.decodeResource(resources, ncDoorID)
        ncDoorLocked = BitmapFactory.decodeResource(resources, ncDoorLockedID)
        ncDoorTrapped = BitmapFactory.decodeResource(resources, ncDoorTrappedID)
    }

    private fun getFontSize(dungeonSize: Int): Int {
        return if (dungeonSize <= 15) {
            resources.getDimensionPixelSize(R.dimen.drawTextSmallFontSize)
        } else if (dungeonSize <= 20) {
            resources.getDimensionPixelSize(R.dimen.drawTextMediumFontSize)
        } else {
            resources.getDimensionPixelSize(R.dimen.drawTextLargeFontSize)
        }
    }

    override fun onDraw(canvas: Canvas) {
        val tiles = dungeonTiles ?: return
        for (i in 1 until tiles.size - 1) {
            for (j in 1 until tiles[i].size - 1) {
                val tile = tiles[i][j]
                when (tile.texture) {
                    Textures.ROOM_EDGE -> {
                        roomEdge?.let {
                            canvas.drawBitmap(
                                scaleBitmap(it, i, j),
                                tile.x.toFloat(),
                                tile.y.toFloat(),
                                null
                            )
                        }
                    }

                    Textures.MARBLE -> {
                        marble?.let {
                            canvas.drawBitmap(
                                scaleBitmap(it, i, j),
                                tile.x.toFloat(),
                                tile.y.toFloat(),
                                null
                            )
                        }
                    }

                    Textures.CORRIDOR -> {
                        corridor?.let {
                            canvas.drawBitmap(
                                scaleBitmap(it, i, j),
                                tile.x.toFloat(),
                                tile.y.toFloat(),
                                null
                            )
                        }
                    }

                    Textures.DOOR -> {
                        door?.let {
                            canvas.drawBitmap(
                                scaleBitmap(
                                    rotateBitmap(
                                        it,
                                        getDegree(i, j).toFloat()
                                    ), i, j
                                ), tile.x.toFloat(), tile.y.toFloat(), null
                            )
                        }
                    }

                    Textures.DOOR_LOCKED -> {
                        doorLocked?.let {
                            canvas.drawBitmap(
                                scaleBitmap(
                                    rotateBitmap(
                                        it,
                                        getDegree(i, j).toFloat()
                                    ), i, j
                                ), tile.x.toFloat(), tile.y.toFloat(), null
                            )
                        }
                    }

                    Textures.DOOR_TRAPPED -> {
                        doorTrapped?.let {
                            canvas.drawBitmap(
                                scaleBitmap(
                                    rotateBitmap(
                                        it,
                                        getDegree(i, j).toFloat()
                                    ), i, j
                                ), tile.x.toFloat(), tile.y.toFloat(), null
                            )
                        }
                    }

                    Textures.ROOM -> {
                        room?.let {
                            canvas.drawBitmap(
                                scaleBitmap(it, i, j),
                                tile.x.toFloat(),
                                tile.y.toFloat(),
                                null
                            )
                        }
                        canvas.drawText(
                            tile.description ?: "",
                            (tile.x + tile.width * 0.1).roundToLong().toFloat(),
                            (tile.y + tile.height * 0.65).roundToLong().toFloat(),
                            paint
                        )
                    }

                    Textures.ENTRY -> {
                        entry?.let {
                            canvas.drawBitmap(
                                scaleBitmap(it, i, j),
                                tile.x.toFloat(),
                                tile.y.toFloat(),
                                null
                            )
                        }
                    }

                    Textures.TRAP -> {
                        trap?.let {
                            canvas.drawBitmap(
                                scaleBitmap(it, i, j),
                                tile.x.toFloat(),
                                tile.y.toFloat(),
                                null
                            )
                        }
                        canvas.drawText(
                            tile.description ?: "",
                            (tile.x + tile.width * 0.1).roundToLong().toFloat(),
                            (tile.y + tile.height * 0.5).roundToLong().toFloat(),
                            paint
                        )
                    }

                    Textures.ROAMING_MONSTER -> {
                        roamingMonster?.let {
                            canvas.drawBitmap(
                                scaleBitmap(it, i, j),
                                tile.x.toFloat(),
                                tile.y.toFloat(),
                                null
                            )
                        }
                        canvas.drawText(
                            tile.description ?: "",
                            (tile.x + tile.width * 0.1).roundToLong().toFloat(),
                            (tile.y + tile.height * 0.4).roundToLong().toFloat(),
                            paint
                        )
                    }

                    Textures.NO_CORRIDOR_DOOR -> {
                        ncDoor?.let {
                            canvas.drawBitmap(
                                scaleBitmap(
                                    rotateBitmap(
                                        it,
                                        getDegree(i, j).toFloat()
                                    ), i, j
                                ), tile.x.toFloat(), tile.y.toFloat(), null
                            )
                        }
                    }

                    Textures.NO_CORRIDOR_DOOR_LOCKED -> {
                        ncDoorLocked?.let {
                            canvas.drawBitmap(
                                scaleBitmap(
                                    rotateBitmap(
                                        it,
                                        getDegree(i, j).toFloat()
                                    ), i, j
                                ), tile.x.toFloat(), tile.y.toFloat(), null
                            )
                        }
                    }

                    Textures.NO_CORRIDOR_DOOR_TRAPPED -> {
                        ncDoorTrapped?.let {
                            canvas.drawBitmap(
                                scaleBitmap(
                                    rotateBitmap(
                                        it,
                                        getDegree(i, j).toFloat()
                                    ), i, j
                                ), tile.x.toFloat(), tile.y.toFloat(), null
                            )
                        }
                    }

                    else -> {}
                }
            }
        }
    }

    private fun scaleBitmap(bitmap: Bitmap, i: Int, j: Int): Bitmap {
        val tiles = dungeonTiles ?: return bitmap
        val width = tiles[i][j].width
        val height = tiles[i][j].height
        if (width <= 0 || height <= 0) return bitmap
        return bitmap.scale(width, height, false)
    }

    private fun getDegree(i: Int, j: Int): Int {
        val tiles = dungeonTiles ?: return 0
        if (tiles[i][j - 1].texture == Textures.ROOM) {
            return -90
        }
        if (tiles[i][j + 1].texture == Textures.ROOM) {
            return 90
        }
        if (tiles[i + 1][j].texture == Textures.ROOM) {
            return 180
        }
        return 0
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val w = MeasureSpec.getSize(widthMeasureSpec)
        setMeasuredDimension(w, w)
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    companion object {
        private fun rotateBitmap(source: Bitmap, angle: Float): Bitmap {
            val matrix = Matrix()
            matrix.postRotate(angle)
            return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
        }
    }
}
