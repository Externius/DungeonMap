package externius.rdmg.activities

import android.app.Dialog
import android.content.ContentValues
import android.content.Intent
import android.database.sqlite.SQLiteConstraintException
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Point
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.SystemClock
import android.text.Html
import android.util.Log
import android.view.GestureDetector
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.res.ResourcesCompat
import androidx.core.net.toUri
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import externius.rdmg.R
import externius.rdmg.database.DBOpenHelper
import externius.rdmg.database.DungeonsProvider
import externius.rdmg.helpers.Export
import externius.rdmg.models.DungeonTile
import externius.rdmg.models.RoamingMonsterDescription
import externius.rdmg.models.RoomDescription
import externius.rdmg.models.Textures
import externius.rdmg.models.TrapDescription
import externius.rdmg.views.DungeonMapView
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.Random
import java.util.Scanner
import java.util.concurrent.Executors
import kotlin.math.min
import androidx.core.graphics.drawable.toDrawable
import androidx.core.graphics.createBitmap

const val SMALL = "Small"
const val MEDIUM = "Medium"
const val LARGE = "Large"

class DungeonActivity : AppCompatActivity() {
    private val gson = Gson()
    private var exported = false
    private var dungeonDifficulty = 0
    private var partyLevel = 0
    private var partySize = 0
    private var dungeonSize = 0
    private var roomDensity = 0
    private var roomSize = 0
    private var traps = 0
    private var roamingMonsters = 0
    private var hasCorridor = false
    private var hasDeadEnds = false
    private var monsterType: String? = null
    private var dungeonView: DungeonMapView? = null

    private val exportActivityResultLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val data = result.data
                if (data != null) {
                    saveDungeonMap(data)
                }
            }
        }

    private var jsonMonster: String? = null
    private var jsonTreasure: String? = null
    private var theme: String? = null
    private var filter: String? = null
    private var treasureValue = 0.0
    private var itemsRarity = 0
    private var loadedDungeon: Array<Array<DungeonTile>>? = null
    private var loadedRoomDescription: MutableList<RoomDescription>? = null
    private var loadedTrapDescription: MutableList<TrapDescription>? = null
    private var loadedRoamingMonsterDescription: MutableList<RoamingMonsterDescription>? = null
    private var extras: Bundle? = null
    private var mLastClickTime: Long = 0
    private var area = 0
    private var dialog: Dialog? = null
    private var mGestureDetector: GestureDetector? = null
    private var layout: RelativeLayout? = null
    private val executor = Executors.newSingleThreadExecutor()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dungeon)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        addLoadingScreen()
        addScreenText()
        mGestureDetector = GestureDetector(this, DungeonActivityGestureListener())
        Thread {
            extras = intent.extras
            setParameters()
            jsonMonster = readJSON(R.raw.monsters)
            jsonTreasure = readJSON(R.raw.treasures)
            runOnUiThread {
                val dv = getDungeonView(loadedDungeon != null)
                dungeonView = dv
                drawDungeon(dv)
            }
        }.start()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun addScreenText() {
        layout = findViewById(R.id.dungeon_layout)
        val screenText = TextView(this)
        setScreenText(screenText)
        val params = getScreenTextLayoutParams()
        screenText.layoutParams = params
        layout?.addView(screenText)
    }

    private fun getScreenTextLayoutParams(): RelativeLayout.LayoutParams {
        val params = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.WRAP_CONTENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT
        )
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
        params.addRule(RelativeLayout.CENTER_HORIZONTAL)
        val size = getScreenSize()
        setArea(size)
        params.setMargins(10, 10, 10, size.y / 12)
        return params
    }

    private fun getScreenSize(): Point {
        val metrics = windowManager.currentWindowMetrics
        val windowInsets = metrics.windowInsets
        val insets = windowInsets.getInsetsIgnoringVisibility(
            WindowInsets.Type.navigationBars() or WindowInsets.Type.displayCutout()
        )
        val insetsWidth = insets.right + insets.left
        val insetsHeight = insets.top + insets.bottom
        val bounds = metrics.bounds
        return Point(bounds.width() - insetsWidth, bounds.height() - insetsHeight)
    }

    private fun setArea(size: Point) {
        area = min(size.x, size.y)
    }

    private fun setScreenText(screenText: TextView) {
        val text = resources.getStringArray(R.array.loading_screen_gen_array)
        val random = Random()
        val intentExtras = intent.extras
        if (intentExtras?.getString("URI") != null) {
            screenText.setText(R.string.loading_text)
        } else {
            screenText.text = text[random.nextInt(text.size)]
        }
        screenText.setTextColor(Color.WHITE)
    }

    private fun addLoadingScreen() {
        layout = findViewById(R.id.dungeon_layout)
        val imageView = ImageView(this)
        imageView.setImageResource(R.drawable.generating_screen)
        val relativeParams = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.MATCH_PARENT,
            RelativeLayout.LayoutParams.MATCH_PARENT
        )
        imageView.layoutParams = relativeParams
        imageView.scaleType = ImageView.ScaleType.FIT_XY
        layout?.addView(imageView)
    }

    override fun onDestroy() {
        super.onDestroy()
        dialog?.dismiss()
        executor.shutdown()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setParameters() {
        val ex = extras ?: return
        if (ex.getString("URI") != null) {
            getLoadedColumns()
        } else {
            loadedDungeon = null
            loadedRoomDescription = null
            loadedTrapDescription = null
            loadedRoamingMonsterDescription = null
            filter = null
        }
        val mt = ex.getString(DBOpenHelper.MONSTER_TYPE)
        val th = ex.getString("THEME")
        dungeonDifficulty = setDungeonDifficulty(ex.getString(DBOpenHelper.DUNGEON_DIFFICULTY))
        dungeonSize = setDungeonSize(ex.getString(DBOpenHelper.DUNGEON_SIZE))
        roomDensity = setRoomDensity(ex.getString(DBOpenHelper.ROOM_DENSITY))
        roomSize = setRoomSize(ex.getString(DBOpenHelper.ROOM_SIZE))
        traps = setTraps(ex.getString(DBOpenHelper.TRAPS))
        hasDeadEnds = setBooleans(ex.getString(DBOpenHelper.DEAD_ENDS))
        hasCorridor = setBooleans(ex.getString(DBOpenHelper.CORRIDORS))
        partySize = ex.getString(DBOpenHelper.PARTY_SIZE)?.toInt() ?: 4
        partyLevel = ex.getString(DBOpenHelper.PARTY_LEVEL)?.toInt() ?: 1
        treasureValue = setTreasureValue(ex.getString(DBOpenHelper.TREASURE_VALUE))
        itemsRarity = setItemsRarity(ex.getString(DBOpenHelper.ITEMS_RARITY))
        roamingMonsters = setRoamingMonsters(ex.getString(DBOpenHelper.ROAMING_MONSTERS))
        if (!mt.isNullOrEmpty()) {
            monsterType = mt.lowercase()
        }
        if (!th.isNullOrEmpty()) {
            theme = th.lowercase()
        }
        exported = false
    }

    private fun readJSON(id: Int): String? {
        return try {
            resources.openRawResource(id).use { isStream ->
                Scanner(isStream).use { scanner ->
                    val sb = StringBuilder()
                    while (scanner.hasNextLine()) {
                        sb.append(scanner.nextLine())
                    }
                    sb.toString()
                }
            }
        } catch (ex: Exception) {
            Log.e("DungeonActivity", "Read JSON file failed: $ex")
            null
        }
    }

    private fun setBooleans(input: String?): Boolean {
        return !input.isNullOrEmpty() && input.equals("YES", ignoreCase = true)
    }

    private fun setDungeonDifficulty(dd: String?): Int {
        if (!dd.isNullOrEmpty()) {
            when (dd) {
                "Easy" -> return 0
                MEDIUM -> return 1
                "Hard" -> return 2
                "Deadly" -> return 3
            }
        }
        return 0
    }

    private fun setDungeonSize(ds: String?): Int {
        if (!ds.isNullOrEmpty()) {
            when (ds) {
                SMALL -> return 15
                MEDIUM -> return 20
                LARGE -> return 25
            }
        }
        return 15
    }

    private fun setRoomDensity(rd: String?): Int {
        if (!rd.isNullOrEmpty()) {
            when (rd) {
                "Low" -> return 20
                MEDIUM -> return 30
                "High" -> return 40
            }
        }
        return 20
    }

    private fun setRoomSize(rs: String?): Int {
        if (!rs.isNullOrEmpty()) {
            when (rs) {
                SMALL -> return 20
                MEDIUM -> return 25
                LARGE -> return 30
            }
        }
        return 20
    }

    private fun setTraps(tr: String?): Int {
        if (!tr.isNullOrEmpty()) {
            when (tr) {
                "None" -> return 0
                "Few" -> return 15
                "More" -> return 30
            }
        }
        return 0
    }

    private fun setRoamingMonsters(rm: String?): Int {
        if (!rm.isNullOrEmpty()) {
            when (rm) {
                "None" -> return 0
                "Few" -> return 10
                "More" -> return 20
            }
        }
        return 0
    }

    private fun setTreasureValue(tV: String?): Double {
        if (!tV.isNullOrEmpty()) {
            when (tV) {
                "Low" -> return 0.5
                "Standard" -> return 1.0
                "High" -> return 1.5
            }
        }
        return 0.0
    }

    private fun setItemsRarity(iR: String?): Int {
        if (!iR.isNullOrEmpty()) {
            when (iR) {
                "Common" -> return 0
                "Uncommon" -> return 1
                "Rare" -> return 2
                "Very Rare" -> return 3
                "Legendary" -> return 4
            }
        }
        return 0
    }

    private fun getLoadedColumns() {
        val ex = extras ?: return
        val uriStr = ex.getString("URI") ?: return
        val uri = uriStr.toUri()
        filter = ex.getString("FILTER")
        val cursor = contentResolver.query(uri, DBOpenHelper.ALL_COLUMNS, filter, null, null)
        if (cursor != null) {
            cursor.moveToFirst()
            loadedDungeon = gson.fromJson(
                cursor.getString(cursor.getColumnIndexOrThrow(DBOpenHelper.LOADED_DUNGEON)),
                Array<Array<DungeonTile>>::class.java
            )
            val roomListType = object : TypeToken<ArrayList<RoomDescription>>() {}.type
            loadedRoomDescription = gson.fromJson(
                cursor.getString(cursor.getColumnIndexOrThrow(DBOpenHelper.LOADED_ROOM_DESCRIPTION)),
                roomListType
            )
            val trapListType = object : TypeToken<ArrayList<TrapDescription>>() {}.type
            loadedTrapDescription = gson.fromJson(
                cursor.getString(cursor.getColumnIndexOrThrow(DBOpenHelper.LOADED_TRAP_DESCRIPTION)),
                trapListType
            )
            val monsterListType = object : TypeToken<ArrayList<RoamingMonsterDescription>>() {}.type
            loadedRoamingMonsterDescription = gson.fromJson(
                cursor.getString(cursor.getColumnIndexOrThrow(DBOpenHelper.LOADED_ROAMING_MONSTERS)),
                monsterListType
            )
            cursor.close()
        }
    }

    private fun generateDungeon() {
        val layoutView: RelativeLayout = findViewById(R.id.dungeon_layout)
        if (findViewById<View>(R.id.dungeonMap_view) != null) {
            layoutView.removeAllViews()
            exported = false
        }
        val dv = getDungeonView(false)
        dungeonView = dv
        layoutView.addView(dv)
        addButtons(layoutView)
        addDescription(
            layoutView,
            dv.roomDescription,
            dv.trapDescription,
            dv.roamingMonsterDescription
        )
        addTouchListener()
        filter = null
    }

    private fun addTouchListener() {
        fillLoadedVariables()
        val view: DungeonMapView = findViewById(R.id.dungeonMap_view)
        view.setOnTouchListener { v, event ->
            mGestureDetector?.onTouchEvent(event)
            v.performClick()
            true
        }
    }

    private fun fillLoadedVariables() {
        val dv = dungeonView ?: return
        loadedTrapDescription = dv.trapDescription.toMutableList()
        loadedRoomDescription = dv.roomDescription.toMutableList()
        loadedRoamingMonsterDescription = dv.roamingMonsterDescription.toMutableList()
        loadedDungeon = dv.dungeonTiles
    }

    private fun checkMotion(motionEvent: MotionEvent) {
        val dungeon = loadedDungeon ?: return
        val x = motionEvent.x
        val y = motionEvent.y
        val imgSize = area / dungeonSize
        val xIndex = (y.toInt() / imgSize) + 1
        val yIndex = (x.toInt() / imgSize) + 1
        if (xIndex >= dungeon.size || yIndex >= dungeon[0].size) {
            return
        }
        val texture = dungeon[xIndex][yIndex].texture
        val index = dungeon[xIndex][yIndex].index
        val desc = dungeon[xIndex][yIndex].description ?: ""
        if (motionEvent.action == MotionEvent.ACTION_UP) {
            when (texture) {
                Textures.ROOM -> showRoomPopUp(index)
                Textures.TRAP -> showTrapPopUp(index)
                Textures.DOOR, Textures.DOOR_LOCKED, Textures.DOOR_TRAPPED,
                Textures.NO_CORRIDOR_DOOR, Textures.NO_CORRIDOR_DOOR_LOCKED,
                Textures.NO_CORRIDOR_DOOR_TRAPPED -> showDoorPopUp(desc)

                Textures.ENTRY -> showEntryPopUp()
                Textures.ROAMING_MONSTER -> showRoamingMonsterPopUp(index)
                else -> {}
            }
        } else if (motionEvent.action == MotionEvent.ACTION_DOWN) {
            when (texture) {
                Textures.ROOM, Textures.TRAP, Textures.ROAMING_MONSTER -> editPopUp(index, texture)
                else -> {}
            }
        }
    }

    private fun editPopUp(index: Int, texture: Textures) {
        val dlg = Dialog(this, R.style.Dialog)
        dialog = dlg
        val descriptionView: EditText
        val saveButton: Button
        when (texture) {
            Textures.ROOM -> {
                val roomsList = loadedRoomDescription ?: return
                if (index < 0 || index >= roomsList.size) return
                val room = roomsList[index]
                dlg.setContentView(R.layout.room_edit_popup)
                dlg.setTitle(room.name)
                val monsters: EditText = dlg.findViewById(R.id.edit_monster)
                monsters.setText((room.monster ?: "").removePrefix("Monsters: "))
                setTextStyle(monsters, false)
                val treasures: EditText = dlg.findViewById(R.id.edit_treasure)
                treasures.setText((room.treasure ?: "").removePrefix("Treasures: "))
                setTextStyle(treasures, false)
                saveButton = dlg.findViewById(R.id.editDialogSaveButton)
                saveButton.setOnClickListener {
                    val newRoom = RoomDescription(
                        room.name,
                        "Treasures: " + treasures.text.toString(),
                        "Monsters: " + monsters.text.toString(),
                        room.doors
                    )
                    loadedRoomDescription?.set(index, newRoom)
                    drawDungeon(null)
                    dlg.dismiss()
                }
            }

            Textures.TRAP -> {
                val trapsList = loadedTrapDescription ?: return
                if (index < 0 || index >= trapsList.size) return
                val trap = trapsList[index]
                dlg.setContentView(R.layout.trap_edit_popup)
                dlg.setTitle(trap.name)
                descriptionView = dlg.findViewById(R.id.edit_trap)
                descriptionView.setText(trap.description)
                setTextStyle(descriptionView, false)
                saveButton = dlg.findViewById(R.id.editDialogSaveButton)
                saveButton.setOnClickListener {
                    val newTrap = TrapDescription(trap.name, descriptionView.text.toString())
                    loadedTrapDescription?.set(index, newTrap)
                    drawDungeon(null)
                    dlg.dismiss()
                }
            }

            Textures.ROAMING_MONSTER -> {
                val monstersList = loadedRoamingMonsterDescription ?: return
                if (index < 0 || index >= monstersList.size) return
                val monster = monstersList[index]
                dlg.setContentView(R.layout.trap_edit_popup)
                dlg.setTitle(monster.name)
                descriptionView = dlg.findViewById(R.id.edit_trap)
                descriptionView.setText(monster.description)
                setTextStyle(descriptionView, false)
                saveButton = dlg.findViewById(R.id.editDialogSaveButton)
                saveButton.setOnClickListener {
                    val newMonster =
                        RoamingMonsterDescription(monster.name, descriptionView.text.toString())
                    loadedRoamingMonsterDescription?.set(index, newMonster)
                    drawDungeon(null)
                    dlg.dismiss()
                }
            }

            else -> {}
        }
        val cancelButton: Button = dlg.findViewById(R.id.editDialogCancelButton)
        cancelButton.setOnClickListener { dlg.dismiss() }
        dlg.show()
    }

    private fun showEntryPopUp() {
        createDialog("Dungeon Entry", null)
    }

    private fun showDoorPopUp(description: String) {
        val start = description.indexOf(':')
        val end = description.indexOf('(')
        if (start < 0 || end < 0) return
        val details = TextView(this)
        details.text = description.substring(end)
        setTextStyle(details, false)
        createDialog(description.substring(start + 1, end), details)
    }

    private fun showTrapPopUp(index: Int) {
        val trapsList = loadedTrapDescription ?: return
        if (index < 0 || index >= trapsList.size) return
        val trap = trapsList[index]
        val details = TextView(this)
        details.text = trap.description
        setTextStyle(details, false)
        createDialog(trap.name ?: "", details)
    }

    private fun showRoamingMonsterPopUp(index: Int) {
        val monstersList = loadedRoamingMonsterDescription ?: return
        if (index < 0 || index >= monstersList.size) return
        val monster = monstersList[index]
        val details = TextView(this)
        details.text = monster.description
        setTextStyle(details, false)
        createDialog(monster.name ?: "", details)
    }

    private fun showRoomPopUp(index: Int) {
        val roomsList = loadedRoomDescription ?: return
        if (index < 0 || index >= roomsList.size) return
        val room = roomsList[index]
        val details = TextView(this)
        val text = "${room.monster}\n${room.treasure}"
        details.text = text
        setTextStyle(details, false)
        createDialog(room.name ?: "", details)
    }

    private fun createDialog(title: String, view: View?) {
        val params = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.WRAP_CONTENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT
        )
        val dlg = Dialog(this, R.style.Dialog)
        dialog = dlg
        dlg.setContentView(R.layout.room_details_popup)
        dlg.setTitle(title)
        if (view != null) {
            dlg.addContentView(view, params)
        }
        dlg.window?.setBackgroundDrawable(
            ResourcesCompat.getColor(resources, R.color.transparentWhite, null).toDrawable()
        )
        dlg.show()
    }

    private fun saveData() {
        if (filter != null) {
            try {
                updateDungeon()
                Toast.makeText(this, "Dungeon updated", Toast.LENGTH_SHORT).show()
                return
            } catch (_: Exception) {
                Toast.makeText(this, "Something went wrong during the update", Toast.LENGTH_SHORT)
                    .show()
                return
            }
        }
        try {
            saveCurrentDungeon()
            Toast.makeText(this, "Dungeon saved", Toast.LENGTH_SHORT).show()
        } catch (_: SQLiteConstraintException) {
            Toast.makeText(this, "Dungeon already saved", Toast.LENGTH_SHORT).show()
        }
    }

    private fun drawDungeon(result: Any?) {
        val layoutView: RelativeLayout = findViewById(R.id.dungeon_layout)
        layoutView.removeAllViews()
        layoutView.setBackgroundColor(Color.WHITE)
        val dv = dungeonView
        if (result != null && dv != null) {
            layoutView.addView(result as View)
            addButtons(layoutView)
            addDescription(
                layoutView,
                dv.roomDescription,
                dv.trapDescription,
                dv.roamingMonsterDescription
            )
        } else if (loadedDungeon != null) {
            val loadedView = getDungeonView(true)
            layoutView.addView(loadedView)
            addButtons(layoutView)
            addDescription(
                layoutView,
                loadedRoomDescription ?: emptyList(),
                loadedTrapDescription ?: emptyList(),
                loadedRoamingMonsterDescription ?: emptyList()
            )
        }
        addTouchListener()
    }

    private fun saveCurrentDungeon() {
        val uriInserted =
            contentResolver.insert(DungeonsProvider.CONTENT_URI, getDungeonContentValues())
        if (uriInserted != null) {
            filter = "${DBOpenHelper.DUNGEON_ID}=${uriInserted.lastPathSegment}"
        }
    }

    private fun updateDungeon() {
        contentResolver.update(DungeonsProvider.CONTENT_URI, getUpdateValues(), filter, null)
    }

    private fun getUpdateValues(): ContentValues {
        val values = ContentValues()
        values.put(DBOpenHelper.LOADED_ROOM_DESCRIPTION, gson.toJson(loadedRoomDescription))
        values.put(DBOpenHelper.LOADED_TRAP_DESCRIPTION, gson.toJson(loadedTrapDescription))
        values.put(
            DBOpenHelper.LOADED_ROAMING_MONSTERS,
            gson.toJson(loadedRoamingMonsterDescription)
        )
        return values
    }

    private fun getDungeonContentValues(): ContentValues {
        val values = ContentValues()
        val ex = extras
        val dv = dungeonView
        values.put(DBOpenHelper.DUNGEON_NAME, "Dungeon_${getFormattedDate()}")
        values.put(DBOpenHelper.LOADED_DUNGEON, gson.toJson(dv?.dungeonTiles))
        values.put(DBOpenHelper.LOADED_ROOM_DESCRIPTION, gson.toJson(dv?.roomDescription))
        values.put(DBOpenHelper.LOADED_TRAP_DESCRIPTION, gson.toJson(dv?.trapDescription))
        values.put(DBOpenHelper.LOADED_ROAMING_MONSTERS, gson.toJson(dv?.roamingMonsterDescription))
        values.put(DBOpenHelper.DUNGEON_DIFFICULTY, ex?.getString(DBOpenHelper.DUNGEON_DIFFICULTY))
        values.put(DBOpenHelper.PARTY_LEVEL, ex?.getString(DBOpenHelper.PARTY_LEVEL))
        values.put(DBOpenHelper.PARTY_SIZE, ex?.getString(DBOpenHelper.PARTY_SIZE))
        values.put(DBOpenHelper.TREASURE_VALUE, ex?.getString(DBOpenHelper.TREASURE_VALUE))
        values.put(DBOpenHelper.ITEMS_RARITY, ex?.getString(DBOpenHelper.ITEMS_RARITY))
        values.put(DBOpenHelper.DUNGEON_SIZE, ex?.getString(DBOpenHelper.DUNGEON_SIZE))
        values.put(DBOpenHelper.ROOM_DENSITY, ex?.getString(DBOpenHelper.ROOM_DENSITY))
        values.put(DBOpenHelper.ROOM_SIZE, ex?.getString(DBOpenHelper.ROOM_SIZE))
        values.put(DBOpenHelper.TRAPS, ex?.getString(DBOpenHelper.TRAPS))
        values.put(DBOpenHelper.CORRIDORS, ex?.getString(DBOpenHelper.CORRIDORS))
        values.put(DBOpenHelper.MONSTER_TYPE, ex?.getString(DBOpenHelper.MONSTER_TYPE))
        values.put(DBOpenHelper.DEAD_ENDS, ex?.getString(DBOpenHelper.DEAD_ENDS))
        values.put(DBOpenHelper.ROAMING_MONSTERS, ex?.getString(DBOpenHelper.ROAMING_MONSTERS))
        return values
    }

    private fun getFormattedDate(): String {
        val c = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy_MM_dd HH:mm:ss", Locale.ENGLISH)
        return dateFormat.format(c.time)
    }

    private fun addButtons(layoutView: RelativeLayout) {
        addSaveButton(layoutView)
        addGenerateButton(layoutView)
        addExportButton(layoutView)
    }

    private fun addExportButton(layoutView: RelativeLayout) {
        val button = Button(this)
        button.setText(R.string.export_button_text)
        button.id = R.id.dungeon_activity_export_button
        val relativeParams = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.MATCH_PARENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT
        )
        relativeParams.addRule(RelativeLayout.BELOW, R.id.dungeon_activity_generate_button)
        button.layoutParams = relativeParams
        button.setOnClickListener {
            if (checkMassClick()) return@setOnClickListener
            export()
        }
        buttonStyle(button)
        layoutView.addView(button)
    }

    private fun addGenerateButton(layoutView: RelativeLayout) {
        val button = Button(this)
        button.setText(R.string.generate_button_text)
        val relativeParams = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.MATCH_PARENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT
        )
        relativeParams.addRule(RelativeLayout.BELOW, R.id.dungeon_activity_save_button)
        button.layoutParams = relativeParams
        button.setOnClickListener {
            if (checkMassClick()) return@setOnClickListener
            generateDungeon()
        }
        button.id = R.id.dungeon_activity_generate_button
        buttonStyle(button)
        layoutView.addView(button)
    }

    private fun addSaveButton(layoutView: RelativeLayout) {
        val button = Button(this)
        button.setText(R.string.save_button_text)
        val relativeParams = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.MATCH_PARENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT
        )
        relativeParams.addRule(RelativeLayout.BELOW, R.id.dungeonMap_view)
        button.layoutParams = relativeParams
        button.setOnClickListener {
            if (checkMassClick()) return@setOnClickListener
            saveData()
        }
        button.id = R.id.dungeon_activity_save_button
        buttonStyle(button)
        layoutView.addView(button)
    }

    private fun checkMassClick(): Boolean {
        if (SystemClock.elapsedRealtime() - mLastClickTime < 800) {
            return true
        }
        mLastClickTime = SystemClock.elapsedRealtime()
        return false
    }

    private fun buttonStyle(button: Button) {
        button.setTextColor(Color.WHITE)
        val drawable = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            setColor(getColor(R.color.primaryAccent))
            setStroke(4, getColor(R.color.primaryDivider))
        }
        button.background = drawable
    }

    private fun export() {
        if (exported) {
            Toast.makeText(this, "You are already exported this dungeon", Toast.LENGTH_SHORT).show()
            return
        }
        if (isExternalStorageWritableAndHasSpace()) {
            popUpForExport()
        }
    }

    private fun popUpForExport() {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "text/html"
            putExtra(Intent.EXTRA_TITLE, "dungeon.html")
        }
        exportActivityResultLauncher.launch(intent)
    }

    private fun saveDungeonMap(data: Intent?) {
        val uri = data?.data ?: return
        val dv = dungeonView ?: return
        val dungeonMapWidget: View = findViewById(R.id.dungeonMap_view) ?: return
        val dungeonBitmap = getBitmapFromView(dungeonMapWidget)
        val roomDesc = dv.roomDescription
        val trapDesc = dv.trapDescription
        val monsterDesc = dv.roamingMonsterDescription

        executor.execute {
            val html = Export.generateHTML(
                dungeonBitmap,
                roomDesc,
                trapDesc,
                monsterDesc
            )
            writeDocument(uri, html)
            runOnUiThread { exported = true }
        }
    }

    private fun writeDocument(uri: Uri, html: String) {
        try {
            applicationContext.contentResolver.openFileDescriptor(uri, "w")?.use { pfd ->
                FileOutputStream(pfd.fileDescriptor).use { fileOutputStream ->
                    fileOutputStream.write(html.toByteArray())
                }
            }
        } catch (e: IOException) {
            Log.e("DungeonActivity", "File write failed: $e")
        }
    }

    private fun getBitmapFromView(view: View): Bitmap {
        val returnedBitmap = createBitmap(view.width, view.height)
        val canvas = Canvas(returnedBitmap)
        val bgDrawable = view.background
        if (bgDrawable != null) {
            bgDrawable.draw(canvas)
        } else {
            canvas.drawColor(Color.WHITE)
        }
        view.draw(canvas)
        return returnedBitmap
    }

    private fun isExternalStorageWritableAndHasSpace(): Boolean {
        val state = Environment.getExternalStorageState()
        val space = Environment.getExternalStorageDirectory().freeSpace / 1024 / 1024
        return Environment.MEDIA_MOUNTED == state && space > 1
    }

    private fun getDungeonView(load: Boolean): DungeonMapView {
        val dv = DungeonMapView(this)
        dungeonView = dv
        setDungeonParameters()
        if (load) {
            val tiles = loadedDungeon
            val rooms = loadedRoomDescription
            val traps = loadedTrapDescription
            val monsters = loadedRoamingMonsterDescription
            if (tiles != null && rooms != null && traps != null && monsters != null) {
                dv.loadDungeon(tiles, rooms, traps, hasCorridor, monsters)
            }
        } else {
            dv.generateDungeon()
        }
        return dv
    }

    private fun setDungeonParameters() {
        val dv = dungeonView ?: return
        dv.dungeonHeight = area
        dv.dungeonWidth = area
        dv.dungeonSize = dungeonSize
        dv.hasCorridor = hasCorridor
        dv.jsonMonster = jsonMonster
        dv.jsonTreasure = jsonTreasure
        dv.roomDensity = roomDensity
        dv.roomSizePercent = roomSize
        dv.trapPercent = traps
        dv.roamingPercent = roamingMonsters
        dv.partyLevel = partyLevel
        dv.itemsRarity = itemsRarity
        dv.treasureValue = treasureValue
        dv.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dv.dungeonDifficulty = dungeonDifficulty
        dv.partySize = partySize
        dv.monsterType = monsterType
        dv.hasDeadEnds = hasDeadEnds
        dv.theme = theme
        dv.id = R.id.dungeonMap_view
    }

    private fun addDescription(
        layoutView: RelativeLayout,
        roomDescription: List<RoomDescription>,
        trapDescription: List<TrapDescription>?,
        roamingMonsterDescription: List<RoamingMonsterDescription>?
    ) {
        val rooms = getRoomTextViews(layoutView, roomDescription)
        if (rooms.isEmpty()) return
        for (i in 4 until rooms.size step 4) {
            addViewToLayout(layoutView, rooms[i], rooms[i - 1], true)
            addViewToLayout(layoutView, rooms[i + 1], rooms[i], false)
            addViewToLayout(layoutView, rooms[i + 2], rooms[i + 1], false)
            addViewToLayout(layoutView, rooms[i + 3], rooms[i + 2], false)
        }
        if (!trapDescription.isNullOrEmpty()) {
            val trapsD = getTrapTextViews(trapDescription)
            if (trapsD.size >= 2) {
                addViewToLayout(layoutView, trapsD[0], rooms[rooms.size - 1], true)
                addViewToLayout(layoutView, trapsD[1], trapsD[0], false)
                for (i in 2 until trapsD.size step 2) {
                    addViewToLayout(layoutView, trapsD[i], trapsD[i - 1], true)
                    addViewToLayout(layoutView, trapsD[i + 1], trapsD[i], false)
                }
            }
            addMonstersDescription(layoutView, roamingMonsterDescription, trapsD)
        } else {
            addMonstersDescription(layoutView, roamingMonsterDescription, rooms)
        }
    }

    private fun addMonstersDescription(
        layoutView: RelativeLayout,
        roamingMonsterDescription: List<RoamingMonsterDescription>?,
        parent: List<TextView>
    ) {
        if (!roamingMonsterDescription.isNullOrEmpty() && parent.isNotEmpty()) {
            val roamingD = getMonsterTextViews(roamingMonsterDescription)
            if (roamingD.size >= 2) {
                addViewToLayout(layoutView, roamingD[0], parent[parent.size - 1], true)
                addViewToLayout(layoutView, roamingD[1], roamingD[0], false)
                for (i in 2 until roamingD.size step 2) {
                    addViewToLayout(layoutView, roamingD[i], roamingD[i - 1], true)
                    addViewToLayout(layoutView, roamingD[i + 1], roamingD[i], false)
                }
            }
        }
    }

    private fun getMonsterTextViews(roamingMonsterDescription: List<RoamingMonsterDescription>): List<TextView> {
        val result = ArrayList<TextView>()
        for ((name, description) in roamingMonsterDescription) {
            addToTextViewList(result, name ?: "", description ?: "")
        }
        return result
    }

    private fun getTrapTextViews(trapDescription: List<TrapDescription>): List<TextView> {
        val result = ArrayList<TextView>()
        for ((name, description) in trapDescription) {
            addToTextViewList(
                result,
                name ?: "",
                (description ?: "").replace("\n", "")
            )
        }
        return result
    }

    private fun addToTextViewList(list: MutableList<TextView>, name: String, description: String) {
        val itemName = TextView(this)
        val itemDescription = TextView(this)
        itemName.text = Html.fromHtml("<b>$name</b>", Html.FROM_HTML_MODE_LEGACY)
        itemName.id = View.generateViewId()
        itemDescription.text = description
        itemDescription.id = View.generateViewId()
        setTextStyle(itemName, true)
        setTextStyle(itemDescription, false)
        list.add(itemName)
        list.add(itemDescription)
    }

    private fun getRoomTextViews(
        layoutView: RelativeLayout,
        roomDescription: List<RoomDescription>
    ): List<TextView> {
        val rooms = ArrayList<TextView>()
        for ((name, treasure1, monster1, doors1) in roomDescription) {
            val roomName = TextView(this)
            val monster = TextView(this)
            val treasure = TextView(this)
            val doors = TextView(this)
            roomName.text = Html.fromHtml("<b>$name</b>", Html.FROM_HTML_MODE_LEGACY)
            roomName.id = View.generateViewId()
            setRoomTexts(monster1 ?: "", monster)
            monster.id = View.generateViewId()
            setRoomTexts(treasure1 ?: "", treasure)
            treasure.id = View.generateViewId()
            setRoomTexts(doors1 ?: "", doors)
            doors.id = View.generateViewId()
            setTextStyle(roomName, true)
            setTextStyle(monster, false)
            setTextStyle(treasure, false)
            setTextStyle(doors, false)
            rooms.add(roomName)
            rooms.add(monster)
            rooms.add(treasure)
            rooms.add(doors)
        }
        if (rooms.size >= 4) {
            val exportBtn = findViewById<View>(R.id.dungeon_activity_export_button)
            if (exportBtn != null) {
                addViewToLayout(layoutView, rooms[0], exportBtn, true)
            }
            addViewToLayout(layoutView, rooms[1], rooms[0], false)
            addViewToLayout(layoutView, rooms[2], rooms[1], false)
            addViewToLayout(layoutView, rooms[3], rooms[2], false)
        }
        return rooms
    }

    private fun setRoomTexts(textParam: String, view: TextView) {
        var text = textParam.replace(",\n", "")
        text = text.replace("\n", "<br>")
        if (text.contains("Entry #") && !text.contains("Monsters") && !text.contains("Treasures")) {
            text = text.replace("West Entry #", "<b>West Entry #")
            text = text.replace("South Entry #", "<b>South Entry #")
            text = text.replace("East Entry #", "<b>East Entry #")
            text = text.replace("North Entry #", "<b>North Entry #")
            text = text.replace(":", "</b>:")
        } else {
            text = "<b>$text"
            val index = text.indexOf(":")
            if (index != -1) {
                text = text.substring(0, index) + "</b>" + text.substring(index)
            }
        }
        view.text = Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY)
    }

    private fun setTextStyle(textView: TextView, stripe: Boolean) {
        textView.setTextColor(getColor(R.color.primaryText))
        if (stripe) {
            textView.setBackgroundColor(getColor(R.color.striped))
        }
    }

    private fun addViewToLayout(
        relativeLayout: RelativeLayout,
        view: View,
        parent: View,
        root: Boolean
    ) {
        val params = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.MATCH_PARENT,
            RelativeLayout.LayoutParams.MATCH_PARENT
        )
        params.addRule(RelativeLayout.BELOW, parent.id)
        if (root) {
            view.textAlignment = View.TEXT_ALIGNMENT_CENTER
        } else {
            params.addRule(RelativeLayout.ALIGN_PARENT_START)
        }
        relativeLayout.addView(view, params)
    }

    inner class DungeonActivityGestureListener : GestureDetector.SimpleOnGestureListener() {
        override fun onDown(motionEvent: MotionEvent): Boolean {
            return true
        }

        override fun onSingleTapUp(motionEvent: MotionEvent): Boolean {
            checkMotion(motionEvent)
            return true
        }

        override fun onLongPress(motionEvent: MotionEvent) {
            checkMotion(motionEvent)
            super.onLongPress(motionEvent)
        }
    }
}
