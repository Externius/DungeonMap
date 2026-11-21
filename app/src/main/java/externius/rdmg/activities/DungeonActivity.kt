package externius.rdmg.activities

import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.database.sqlite.SQLiteConstraintException
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Point
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.SystemClock
import android.text.Html
import android.util.Log
import android.view.GestureDetector
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.createBitmap
import androidx.core.graphics.drawable.toDrawable
import androidx.core.view.isNotEmpty
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.AndroidEntryPoint
import externius.rdmg.R
import externius.rdmg.databinding.ActivityDungeonBinding
import externius.rdmg.databinding.ItemDescriptionCardBinding
import externius.rdmg.databinding.ItemRoomCardBinding
import externius.rdmg.databinding.RoomDetailsPopupBinding
import externius.rdmg.databinding.RoomEditPopupBinding
import externius.rdmg.databinding.TrapEditPopupBinding
import externius.rdmg.database.DBOpenHelper
import externius.rdmg.database.DungeonEntity
import externius.rdmg.helpers.Export
import externius.rdmg.helpers.applyPrimaryStyle
import externius.rdmg.models.DescriptionItem
import externius.rdmg.models.DungeonTile
import externius.rdmg.models.EditPopupState
import externius.rdmg.models.RoamingMonsterDescription
import externius.rdmg.models.RoomDescription
import externius.rdmg.models.Textures
import externius.rdmg.models.TrapDescription
import externius.rdmg.viewmodels.DungeonViewModel
import externius.rdmg.views.DungeonMapView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.Random
import java.util.Scanner
import kotlin.math.min

const val SMALL = "Small"
const val MEDIUM = "Medium"
const val LARGE = "Large"

@AndroidEntryPoint
class DungeonActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDungeonBinding
    private val viewModel: DungeonViewModel by viewModels()
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

    private var dungeonId: Long
        get() = viewModel.dungeonId
        set(value) {
            viewModel.dungeonId = value
        }

    private var loadedEntity: DungeonEntity?
        get() = viewModel.loadedEntity
        set(value) {
            viewModel.loadedEntity = value
        }

    private var loadedDungeon: Array<Array<DungeonTile>>?
        get() = viewModel.loadedDungeon
        set(value) {
            viewModel.loadedDungeon = value
        }

    private var loadedRoomDescription: List<RoomDescription>?
        get() = viewModel.loadedRoomDescription
        set(value) {
            viewModel.loadedRoomDescription = value
        }

    private var loadedTrapDescription: List<TrapDescription>?
        get() = viewModel.loadedTrapDescription
        set(value) {
            viewModel.loadedTrapDescription = value
        }

    private var loadedRoamingMonsterDescription: List<RoamingMonsterDescription>?
        get() = viewModel.loadedRoamingMonsterDescription
        set(value) {
            viewModel.loadedRoamingMonsterDescription = value
        }

    private var treasureValue = 0.0
    private var itemsRarity = 0
    private var extras: Bundle? = null
    private var mLastClickTime: Long = 0
    private var area = 0
    private var dialog: Dialog? = null
    private var mGestureDetector: GestureDetector? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDungeonBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        addLoadingScreen()
        addScreenText()
        mGestureDetector = GestureDetector(this, DungeonActivityGestureListener())
        lifecycleScope.launch {
            extras = intent.extras
            setParameters()
            withContext(Dispatchers.IO) {
                jsonMonster = readJSON(R.raw.monsters)
                jsonTreasure = readJSON(R.raw.treasures)
            }
            val dv = getDungeonView(loadedDungeon != null)
            dungeonView = dv
            if (loadedDungeon == null) {
                fillLoadedVariables()
            }
            drawDungeon(dv)
            viewModel.editPopupState?.let { popupState ->
                editPopUp(popupState.index, popupState.texture, popupState)
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    dungeonView?.let { dv ->
                        var updated = false
                        state.loadedRoomDescription?.let {
                            if (dv.roomDescription != it) {
                                dv.roomDescription = it.toMutableList()
                                updated = true
                            }
                        }
                        state.loadedTrapDescription?.let {
                            if (dv.trapDescription != it) {
                                dv.trapDescription = it.toMutableList()
                                updated = true
                            }
                        }
                        state.loadedRoamingMonsterDescription?.let {
                            if (dv.roamingMonsterDescription != it) {
                                dv.roamingMonsterDescription = it.toMutableList()
                                updated = true
                            }
                        }
                        if (updated) {
                            drawDungeon(dv)
                        }
                    }
                }
            }
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun addScreenText() {
        val screenText = TextView(this)
        setScreenText(screenText)
        val params = getScreenTextLayoutParams()
        screenText.layoutParams = params
        binding.contentDungeon.dungeonLayout.addView(screenText)
    }

    private fun getScreenTextLayoutParams(): LinearLayout.LayoutParams {
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.gravity = Gravity.CENTER_HORIZONTAL
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
        if (intentExtras?.getLong("DUNGEON_ID", -1L) != -1L) {
            screenText.setText(R.string.loading_text)
        } else {
            screenText.text = text[random.nextInt(text.size)]
        }
        screenText.setTextColor(Color.WHITE)
    }

    private fun addLoadingScreen() {
        val imageView = ImageView(this)
        imageView.setImageResource(R.drawable.generating_screen)
        val linearParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )
        imageView.layoutParams = linearParams
        imageView.scaleType = ImageView.ScaleType.FIT_XY
        binding.contentDungeon.dungeonLayout.addView(imageView)
    }

    override fun onDestroy() {
        super.onDestroy()
        dialog?.dismiss()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private suspend fun setParameters() {
        val ex = extras ?: return
        val id = ex.getLong("DUNGEON_ID", -1L)
        if (id != -1L) {
            if (viewModel.loadedDungeon == null) {
                getLoadedColumns(id)
            }
        } else {
            if (viewModel.loadedDungeon == null) {
                viewModel.resetDungeonState()
            }
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
        partySize = ex.getString(DBOpenHelper.PARTY_SIZE)?.toIntOrNull() ?: 4
        partyLevel = ex.getString(DBOpenHelper.PARTY_LEVEL)?.toIntOrNull() ?: 1
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

    private suspend fun getLoadedColumns(id: Long) {
        val entity = viewModel.getDungeonById(id) ?: return
        val tiles = gson.fromJson(
            entity.loadedDungeon,
            Array<Array<DungeonTile>>::class.java
        )
        val roomListType = object : TypeToken<ArrayList<RoomDescription>>() {}.type
        val rooms: List<RoomDescription>? =
            gson.fromJson(entity.loadedRoomDescription, roomListType)
        val trapListType = object : TypeToken<ArrayList<TrapDescription>>() {}.type
        val traps: List<TrapDescription>? =
            gson.fromJson(entity.loadedTrapDescription, trapListType)
        val monsterListType =
            object : TypeToken<ArrayList<RoamingMonsterDescription>>() {}.type
        val monsters: List<RoamingMonsterDescription>? =
            gson.fromJson(entity.loadedRoamingMonsters, monsterListType)

        viewModel.setLoadedDungeonData(
            dungeonId = id,
            entity = entity,
            dungeon = tiles,
            rooms = rooms,
            traps = traps,
            monsters = monsters
        )
    }

    private fun generateDungeon() {
        val layoutView = binding.contentDungeon.dungeonLayout
        if (dungeonView != null || layoutView.isNotEmpty()) {
            layoutView.removeAllViews()
            exported = false
        }
        viewModel.editPopupState = null
        viewModel.resetDungeonState()
        val dv = getDungeonView(false)
        dungeonView = dv
        fillLoadedVariables()
        drawDungeon(dv)
    }

    private fun addTouchListener() {
        val view: DungeonMapView? =
            dungeonView ?: binding.contentDungeon.dungeonLayout.findViewById(R.id.dungeonMap_view)
        view?.setOnTouchListener { v, event ->
            mGestureDetector?.onTouchEvent(event)
            v.performClick()
            true
        }
    }

    private fun fillLoadedVariables() {
        val dv = dungeonView ?: return
        viewModel.setGeneratedDungeonData(
            dungeon = dv.dungeonTiles,
            rooms = dv.roomDescription,
            traps = dv.trapDescription,
            monsters = dv.roamingMonsterDescription
        )
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

    private fun editPopUp(index: Int, texture: Textures, restoredState: EditPopupState? = null) {
        val dlg = Dialog(this, R.style.Dialog)
        dialog = dlg
        val descriptionView: EditText
        val saveButton: Button
        when (texture) {
            Textures.ROOM -> {
                val roomsList = loadedRoomDescription ?: run {
                    viewModel.editPopupState = null
                    return
                }
                if (index < 0 || index >= roomsList.size) {
                    viewModel.editPopupState = null
                    return
                }
                val room = roomsList[index]
                val popupBinding = RoomEditPopupBinding.inflate(layoutInflater)
                dlg.setContentView(popupBinding.root)
                dlg.setTitle(room.name)
                val monsters = popupBinding.editMonster
                val initialMonster =
                    restoredState?.monsterText ?: (room.monster ?: "").removePrefix("Monsters: ")
                monsters.setText(initialMonster)
                setPrimaryTextColor(monsters)

                val treasures = popupBinding.editTreasure
                val initialTreasure =
                    restoredState?.treasureText ?: (room.treasure ?: "").removePrefix("Treasures: ")
                treasures.setText(initialTreasure)
                setPrimaryTextColor(treasures)

                viewModel.editPopupState = EditPopupState(
                    index = index,
                    texture = texture,
                    monsterText = monsters.text.toString(),
                    treasureText = treasures.text.toString()
                )

                val updateState = {
                    viewModel.editPopupState = viewModel.editPopupState?.copy(
                        monsterText = monsters.text.toString(),
                        treasureText = treasures.text.toString()
                    )
                }
                monsters.doAfterTextChanged { updateState() }
                treasures.doAfterTextChanged { updateState() }

                saveButton = popupBinding.editDialogSaveButton
                saveButton.setOnClickListener {
                    val newRoom = RoomDescription(
                        room.name,
                        "Treasures: " + treasures.text.toString(),
                        "Monsters: " + monsters.text.toString(),
                        room.doors
                    )
                    viewModel.updateRoomDescriptionAt(index, newRoom)
                    viewModel.editPopupState = null
                    dlg.dismiss()
                }

                popupBinding.editDialogCancelButton.applyPrimaryStyle()
                popupBinding.editDialogCancelButton.setOnClickListener {
                    viewModel.editPopupState = null
                    dlg.dismiss()
                }
            }

            Textures.TRAP -> {
                val trapsList = loadedTrapDescription ?: run {
                    viewModel.editPopupState = null
                    return
                }
                if (index < 0 || index >= trapsList.size) {
                    viewModel.editPopupState = null
                    return
                }
                val trap = trapsList[index]
                val popupBinding = TrapEditPopupBinding.inflate(layoutInflater)
                dlg.setContentView(popupBinding.root)
                dlg.setTitle(trap.name)
                descriptionView = popupBinding.editTrap
                val initialDesc = restoredState?.descriptionText ?: trap.description
                descriptionView.setText(initialDesc)
                setPrimaryTextColor(descriptionView)

                viewModel.editPopupState = EditPopupState(
                    index = index,
                    texture = texture,
                    descriptionText = descriptionView.text.toString()
                )

                descriptionView.doAfterTextChanged {
                    viewModel.editPopupState = viewModel.editPopupState?.copy(
                        descriptionText = descriptionView.text.toString()
                    )
                }

                saveButton = popupBinding.editDialogSaveButton
                saveButton.setOnClickListener {
                    val newTrap = TrapDescription(trap.name, descriptionView.text.toString())
                    viewModel.updateTrapDescriptionAt(index, newTrap)
                    viewModel.editPopupState = null
                    dlg.dismiss()
                }

                popupBinding.editDialogCancelButton.applyPrimaryStyle()
                popupBinding.editDialogCancelButton.setOnClickListener {
                    viewModel.editPopupState = null
                    dlg.dismiss()
                }
            }

            Textures.ROAMING_MONSTER -> {
                val monstersList = loadedRoamingMonsterDescription ?: run {
                    viewModel.editPopupState = null
                    return
                }
                if (index < 0 || index >= monstersList.size) {
                    viewModel.editPopupState = null
                    return
                }
                val monster = monstersList[index]
                val popupBinding = TrapEditPopupBinding.inflate(layoutInflater)
                dlg.setContentView(popupBinding.root)
                dlg.setTitle(monster.name)
                descriptionView = popupBinding.editTrap
                val initialDesc = restoredState?.descriptionText ?: monster.description
                descriptionView.setText(initialDesc)
                setPrimaryTextColor(descriptionView)

                viewModel.editPopupState = EditPopupState(
                    index = index,
                    texture = texture,
                    descriptionText = descriptionView.text.toString()
                )

                descriptionView.doAfterTextChanged {
                    viewModel.editPopupState = viewModel.editPopupState?.copy(
                        descriptionText = descriptionView.text.toString()
                    )
                }

                saveButton = popupBinding.editDialogSaveButton
                saveButton.setOnClickListener {
                    val newMonster =
                        RoamingMonsterDescription(monster.name, descriptionView.text.toString())
                    viewModel.updateRoamingMonsterDescriptionAt(index, newMonster)
                    viewModel.editPopupState = null
                    dlg.dismiss()
                }

                popupBinding.editDialogCancelButton.applyPrimaryStyle()
                popupBinding.editDialogCancelButton.setOnClickListener {
                    viewModel.editPopupState = null
                    dlg.dismiss()
                }
            }

            else -> {
                viewModel.editPopupState = null
                return
            }
        }
        saveButton.applyPrimaryStyle()
        dlg.setOnCancelListener {
            if (!isChangingConfigurations) {
                viewModel.editPopupState = null
            }
        }
        dlg.show()
    }

    private fun showEntryPopUp() {
        createDialog(getString(R.string.dungeon_entry), null)
    }

    private fun showDoorPopUp(description: String) {
        val start = description.indexOf(':')
        val end = description.indexOf('(')
        if (start < 0 || end < 0) return
        val details = TextView(this)
        details.text = description.substring(end)
        setPrimaryTextColor(details)
        createDialog(description.substring(start + 1, end), details)
    }

    private fun showTrapPopUp(index: Int) {
        val trapsList = loadedTrapDescription ?: return
        if (index < 0 || index >= trapsList.size) return
        val trap = trapsList[index]
        val details = TextView(this)
        details.text = trap.description
        setPrimaryTextColor(details)
        createDialog(trap.name ?: "", details)
    }

    private fun showRoamingMonsterPopUp(index: Int) {
        val monstersList = loadedRoamingMonsterDescription ?: return
        if (index < 0 || index >= monstersList.size) return
        val monster = monstersList[index]
        val details = TextView(this)
        details.text = monster.description
        setPrimaryTextColor(details)
        createDialog(monster.name ?: "", details)
    }

    private fun showRoomPopUp(index: Int) {
        val roomsList = loadedRoomDescription ?: return
        if (index < 0 || index >= roomsList.size) return
        val room = roomsList[index]
        val details = TextView(this)
        val text = "${room.monster}\n${room.treasure}"
        details.text = text
        setPrimaryTextColor(details)
        createDialog(room.name ?: "", details)
    }

    private fun createDialog(title: String, view: View?) {
        val params = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        val dlg = Dialog(this, R.style.Dialog)
        dialog = dlg
        val popupBinding = RoomDetailsPopupBinding.inflate(layoutInflater)
        dlg.setContentView(popupBinding.root)
        dlg.setTitle(title)
        if (view != null) {
            dlg.addContentView(view, params)
        }
        dlg.window?.setBackgroundDrawable(
            ResourcesCompat.getColor(resources, R.color.dialogOverlayColor, null).toDrawable()
        )
        dlg.show()
    }

    private fun saveData() {
        val id = dungeonId
        val currentEntity = loadedEntity

        if (id != -1L && currentEntity != null) {
            val updatedEntity = currentEntity.copy(
                loadedRoomDescription = gson.toJson(loadedRoomDescription),
                loadedTrapDescription = gson.toJson(loadedTrapDescription),
                loadedRoamingMonsters = gson.toJson(loadedRoamingMonsterDescription)
            )
            lifecycleScope.launch {
                try {
                    val count = viewModel.updateDungeon(updatedEntity)
                    if (count > 0) {
                        loadedEntity = updatedEntity
                        Toast.makeText(
                            this@DungeonActivity,
                            getString(R.string.dungeon_updated),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } catch (_: Exception) {
                    Toast.makeText(
                        this@DungeonActivity,
                        getString(R.string.dungeon_update_error),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        } else {
            val ex = extras
            val dv = dungeonView
            val createdTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).apply {
                timeZone = java.util.TimeZone.getTimeZone("UTC")
            }.format(java.util.Date())
            val newEntity = DungeonEntity(
                dungeonName = "Dungeon_${getFormattedDate()}",
                loadedDungeon = gson.toJson(dv?.dungeonTiles),
                loadedRoomDescription = gson.toJson(dv?.roomDescription),
                loadedTrapDescription = gson.toJson(dv?.trapDescription),
                loadedRoamingMonsters = gson.toJson(dv?.roamingMonsterDescription),
                dungeonDifficulty = ex?.getString(DBOpenHelper.DUNGEON_DIFFICULTY),
                partyLevel = ex?.getString(DBOpenHelper.PARTY_LEVEL),
                partySize = ex?.getString(DBOpenHelper.PARTY_SIZE),
                treasureValue = ex?.getString(DBOpenHelper.TREASURE_VALUE),
                itemsRarity = ex?.getString(DBOpenHelper.ITEMS_RARITY),
                dungeonSize = ex?.getString(DBOpenHelper.DUNGEON_SIZE),
                roomDensity = ex?.getString(DBOpenHelper.ROOM_DENSITY),
                roomSize = ex?.getString(DBOpenHelper.ROOM_SIZE),
                traps = ex?.getString(DBOpenHelper.TRAPS),
                corridors = if (hasCorridor) "YES" else "NO",
                monsterType = ex?.getString(DBOpenHelper.MONSTER_TYPE),
                deadEnds = if (hasDeadEnds) "YES" else "NO",
                roamingMonsters = ex?.getString(DBOpenHelper.ROAMING_MONSTERS),
                dungeonCreated = createdTime
            )
            lifecycleScope.launch {
                try {
                    val newId = viewModel.insertDungeon(newEntity)
                    if (newId > 0) {
                        dungeonId = newId
                        loadedEntity = newEntity.copy(id = newId)
                        Toast.makeText(
                            this@DungeonActivity,
                            getString(R.string.dungeon_saved),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } catch (_: SQLiteConstraintException) {
                    Toast.makeText(
                        this@DungeonActivity,
                        getString(R.string.dungeon_already_saved),
                        Toast.LENGTH_SHORT
                    ).show()
                } catch (_: Exception) {
                    Toast.makeText(
                        this@DungeonActivity,
                        getString(R.string.dungeon_save_error),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun drawDungeon(result: Any?) {
        val layoutView = binding.contentDungeon.dungeonLayout
        layoutView.removeAllViews()
        val typedValue = android.util.TypedValue()
        theme?.let { _ ->
            getTheme().resolveAttribute(android.R.attr.colorBackground, typedValue, true)
        }
        val backgroundColor = if (typedValue.resourceId != 0) {
            ContextCompat.getColor(this, typedValue.resourceId)
        } else {
            typedValue.data
        }
        layoutView.setBackgroundColor(backgroundColor)
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

    private fun getFormattedDate(): String {
        val c = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy_MM_dd HH:mm:ss", Locale.ENGLISH)
        return dateFormat.format(c.time)
    }

    private fun addButtons(layoutView: LinearLayout) {
        addSaveButton(layoutView)
        addGenerateButton(layoutView)
        addExportButton(layoutView)
    }

    private fun addExportButton(layoutView: LinearLayout) {
        val button = Button(this)
        button.setText(R.string.export_button_text)
        button.id = R.id.dungeon_activity_export_button
        val topMargin = (resources.displayMetrics.density * 4).toInt()
        val linearParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            setMargins(0, topMargin, 0, resources.displayMetrics.density.toInt())
        }
        button.layoutParams = linearParams
        button.setOnClickListener {
            if (checkMassClick()) return@setOnClickListener
            export()
        }
        button.applyPrimaryStyle()
        layoutView.addView(button)
    }

    private fun addGenerateButton(layoutView: LinearLayout) {
        val button = Button(this)
        button.setText(R.string.generate_button_text)
        val topMargin = (resources.displayMetrics.density * 4).toInt()
        val linearParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            setMargins(0, topMargin, 0, 0)
        }
        button.layoutParams = linearParams
        button.setOnClickListener {
            if (checkMassClick()) return@setOnClickListener
            generateDungeon()
        }
        button.id = R.id.dungeon_activity_generate_button
        button.applyPrimaryStyle()
        layoutView.addView(button)
    }

    private fun addSaveButton(layoutView: LinearLayout) {
        val button = Button(this)
        button.setText(R.string.save_button_text)
        val linearParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            setMargins(0, resources.displayMetrics.density.toInt(), 0, 0)
        }
        button.layoutParams = linearParams
        button.setOnClickListener {
            if (checkMassClick()) return@setOnClickListener
            saveData()
        }
        button.id = R.id.dungeon_activity_save_button
        button.applyPrimaryStyle()
        layoutView.addView(button)
    }

    private fun checkMassClick(): Boolean {
        if (SystemClock.elapsedRealtime() - mLastClickTime < 800) {
            return true
        }
        mLastClickTime = SystemClock.elapsedRealtime()
        return false
    }

    private fun export() {
        if (exported) {
            Toast.makeText(this, getString(R.string.dungeon_already_exported), Toast.LENGTH_SHORT)
                .show()
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
        try {
            exportActivityResultLauncher.launch(intent)
        } catch (e: ActivityNotFoundException) {
            Log.e("DungeonActivity", "No activity found to handle document export", e)
            Toast.makeText(this, getString(R.string.export_error_no_app), Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e("DungeonActivity", "Failed to launch document picker", e)
            Toast.makeText(this, getString(R.string.dungeon_save_error), Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveDungeonMap(data: Intent?) {
        val uri = data?.data ?: return
        val dv = dungeonView ?: return
        val dungeonBitmap = getBitmapFromView(dv)
        val roomDesc = dv.roomDescription
        val trapDesc = dv.trapDescription
        val monsterDesc = dv.roamingMonsterDescription

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val html = Export.generateHTML(
                    dungeonBitmap,
                    roomDesc,
                    trapDesc,
                    monsterDesc
                )
                writeDocument(uri, html)
                withContext(Dispatchers.Main) {
                    exported = true
                }
            } finally {
                dungeonBitmap?.takeIf { !it.isRecycled }?.recycle()
            }
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

    private fun getBitmapFromView(view: View): Bitmap? {
        val width = view.width
        val height = view.height
        if (width <= 0 || height <= 0) return null

        return runCatching {
            val maxDim = maxOf(width, height)
            val scale = if (maxDim > Export.MAX_EXPORT_BITMAP_DIMENSION) {
                Export.MAX_EXPORT_BITMAP_DIMENSION.toFloat() / maxDim
            } else {
                1.0f
            }
            val targetWidth = (width * scale).toInt().coerceAtLeast(1)
            val targetHeight = (height * scale).toInt().coerceAtLeast(1)
            val returnedBitmap = createBitmap(targetWidth, targetHeight)
            val canvas = Canvas(returnedBitmap)
            if (scale < 1.0f) {
                canvas.scale(scale, scale)
            }
            canvas.drawColor(Color.WHITE)
            view.draw(canvas)
            returnedBitmap
        }.onFailure { e ->
            Log.e("DungeonActivity", "Failed to capture bitmap from view: $e")
        }.getOrNull()
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
        layoutView: LinearLayout,
        roomDescription: List<RoomDescription>,
        trapDescription: List<TrapDescription>?,
        roamingMonsterDescription: List<RoamingMonsterDescription>?
    ) {
        val items = mutableListOf<DescriptionItem>()
        for (room in roomDescription) {
            items.add(DescriptionItem.Room(room))
        }
        trapDescription?.forEach { trap ->
            items.add(DescriptionItem.Trap(trap))
        }
        roamingMonsterDescription?.forEach { monster ->
            items.add(DescriptionItem.Monster(monster))
        }
        if (items.isEmpty()) return

        val descriptionAdapter = DungeonDescriptionAdapter()
        val recyclerView = RecyclerView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                val margin = (resources.displayMetrics.density * 4).toInt()
                setMargins(0, margin, 0, margin)
            }
            isNestedScrollingEnabled = false
            layoutManager = LinearLayoutManager(this@DungeonActivity)
            adapter = descriptionAdapter
        }
        descriptionAdapter.submitList(items)
        layoutView.addView(recyclerView)
    }

    private fun setPrimaryTextColor(textView: TextView) {
        textView.setTextColor(getColor(R.color.primaryText))
    }

    class DungeonDescriptionAdapter :
        ListAdapter<DescriptionItem, RecyclerView.ViewHolder>(DescriptionDiffCallback) {

        companion object {
            private const val TYPE_ROOM = 0
            private const val TYPE_TRAP = 1
            private const val TYPE_MONSTER = 2
        }

        override fun getItemViewType(position: Int): Int {
            return when (getItem(position)) {
                is DescriptionItem.Room -> TYPE_ROOM
                is DescriptionItem.Trap -> TYPE_TRAP
                is DescriptionItem.Monster -> TYPE_MONSTER
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            return when (viewType) {
                TYPE_ROOM -> RoomViewHolder(
                    ItemRoomCardBinding.inflate(inflater, parent, false)
                )

                else -> DescriptionViewHolder(
                    ItemDescriptionCardBinding.inflate(inflater, parent, false)
                )
            }
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            when (val item = getItem(position)) {
                is DescriptionItem.Room -> (holder as RoomViewHolder).bind(item.room)
                is DescriptionItem.Trap -> (holder as DescriptionViewHolder).bind(
                    item.trap.name,
                    item.trap.description?.replace("\n", "")
                )

                is DescriptionItem.Monster -> (holder as DescriptionViewHolder).bind(
                    item.monster.name,
                    item.monster.description
                )
            }
        }
    }

    private object DescriptionDiffCallback : DiffUtil.ItemCallback<DescriptionItem>() {
        override fun areItemsTheSame(oldItem: DescriptionItem, newItem: DescriptionItem): Boolean {
            return when (oldItem) {
                is DescriptionItem.Room if newItem is DescriptionItem.Room -> oldItem.room.name == newItem.room.name
                is DescriptionItem.Trap if newItem is DescriptionItem.Trap -> oldItem.trap.name == newItem.trap.name
                is DescriptionItem.Monster if newItem is DescriptionItem.Monster -> oldItem.monster.name == newItem.monster.name
                else -> false
            }
        }

        override fun areContentsTheSame(
            oldItem: DescriptionItem,
            newItem: DescriptionItem
        ): Boolean {
            return oldItem == newItem
        }
    }

    class RoomViewHolder(val binding: ItemRoomCardBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(room: RoomDescription) {
            binding.textRoomName.text = room.name ?: ""
            bindRoomText(binding.textRoomMonster, room.monster)
            bindRoomText(binding.textRoomTreasure, room.treasure)
            bindRoomText(binding.textRoomDoors, room.doors)
        }

        private fun bindRoomText(textView: TextView, rawText: String?) {
            if (rawText.isNullOrEmpty()) {
                textView.visibility = View.GONE
            } else {
                textView.visibility = View.VISIBLE
                setRoomTexts(rawText, textView)
            }
        }

        private fun setRoomTexts(textParam: String, view: TextView) {
            val lines = textParam.replace(",\n", "").split("\n")
            val formattedText = lines
                .filter { it.isNotBlank() }
                .joinToString("<br>") { line ->
                    if (!line.contains("<b>")) {
                        val colonIndex = line.indexOf(':')
                        if (colonIndex != -1) {
                            "<b>" + line.substring(0, colonIndex) +
                                    "</b>" + line.substring(colonIndex)
                        } else {
                            line
                        }
                    } else {
                        line
                    }
                }
            view.text = Html.fromHtml(formattedText, Html.FROM_HTML_MODE_LEGACY)
        }
    }

    class DescriptionViewHolder(val binding: ItemDescriptionCardBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(name: String?, desc: String?) {
            binding.textCardTitle.text = name ?: ""
            if (desc.isNullOrEmpty()) {
                binding.textCardDescription.visibility = View.GONE
            } else {
                binding.textCardDescription.visibility = View.VISIBLE
                binding.textCardDescription.text = desc
            }
        }
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
