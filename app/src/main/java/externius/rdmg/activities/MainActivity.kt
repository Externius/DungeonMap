package externius.rdmg.activities

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.database.Cursor
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.Button
import android.widget.CursorAdapter
import android.widget.ListView
import android.widget.SimpleCursorAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.loader.app.LoaderManager
import androidx.loader.content.CursorLoader
import androidx.loader.content.Loader
import externius.rdmg.R
import externius.rdmg.database.DBOpenHelper
import externius.rdmg.database.DungeonsProvider
import externius.rdmg.helpers.MultiSelectMonster
import androidx.core.net.toUri

class MainActivity : AppCompatActivity(), LoaderManager.LoaderCallbacks<Cursor> {
    private lateinit var spinnerDifficulty: Spinner
    private lateinit var spinnerPartyLevel: Spinner
    private lateinit var spinnerPartySize: Spinner
    private lateinit var spinnerTreasureValue: Spinner
    private lateinit var spinnerItemsRarity: Spinner
    private lateinit var spinnerSize: Spinner
    private lateinit var spinnerRoomDens: Spinner
    private lateinit var spinnerRoomSize: Spinner
    private lateinit var spinnerTraps: Spinner
    private lateinit var spinnerCorridors: Spinner
    private lateinit var spinnerRoamingMonsters: Spinner
    private lateinit var spinnerMonsterType: MultiSelectMonster
    private lateinit var spinnerDeadEnds: Spinner
    private lateinit var spinnerTheme: Spinner

    private var loadDialog: Dialog? = null
    private var cursorAdapter: CursorAdapter? = null
    private var filter: String? = null
    private var uri: Uri? = null
    private var list: ListView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setToolbar()
        getSpinners()
        setDefaultValues()
        addListeners()
        initDB()
        if (savedInstanceState != null) {
            val monsterType = savedInstanceState.getString(DBOpenHelper.MONSTER_TYPE)
            if (monsterType != null) {
                spinnerMonsterType.setAllText(monsterType)
            }
        }
    }

    private fun setToolbar() {
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val text = spinnerMonsterType.allText
        if (text != null) {
            outState.putString(DBOpenHelper.MONSTER_TYPE, text)
        }
    }

    private fun getCorridorItemSelectedListener(): AdapterView.OnItemSelectedListener {
        return object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>?, view: View?, i: Int, l: Long) {
                if (spinnerCorridors.selectedItem.toString().equals("no", ignoreCase = true)) {
                    spinnerTraps.isEnabled = false
                    spinnerRoomDens.isEnabled = false
                    spinnerDeadEnds.isEnabled = false
                    spinnerRoamingMonsters.isEnabled = false
                } else {
                    spinnerTraps.isEnabled = true
                    spinnerRoomDens.isEnabled = true
                    spinnerDeadEnds.isEnabled = true
                    checkMonsterType()
                }
            }

            override fun onNothingSelected(adapterView: AdapterView<*>?) {}
        }
    }

    private fun initDB() {
        val from = arrayOf(DBOpenHelper.DUNGEON_NAME)
        val to = intArrayOf(android.R.id.text1)
        cursorAdapter = SimpleCursorAdapter(this, android.R.layout.simple_list_item_1, null, from, to, 0)
        LoaderManager.getInstance(this).initLoader(0, null, this)
    }

    private fun addListeners() {
        spinnerCorridors.onItemSelectedListener = getCorridorItemSelectedListener()
        spinnerMonsterType.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                checkMonsterType()
            }
        })
        val generateButton: Button = findViewById(R.id.generate_button)
        generateButton.setOnClickListener { generateDungeon() }
        val loadButton: Button = findViewById(R.id.load_button)
        loadButton.setOnClickListener(loadListener())
    }

    private fun checkMonsterType() {
        val allText = spinnerMonsterType.allText ?: ""
        if (allText.equals("none", ignoreCase = true)) {
            spinnerRoamingMonsters.isEnabled = false
            spinnerRoamingMonsters.setSelection(0)
        } else if (spinnerCorridors.selectedItem.toString().equals("yes", ignoreCase = true)) {
            spinnerRoamingMonsters.isEnabled = true
        }
    }

    private fun setDefaultValues() {
        spinnerDifficulty.setSelection(1)
        spinnerPartySize.setSelection(3)
        spinnerTraps.setSelection(1)
        spinnerTreasureValue.setSelection(1)
        spinnerItemsRarity.setSelection(1)
    }

    private fun getSpinners() {
        spinnerDifficulty = findViewById(R.id.dungeon_difficulty)
        spinnerPartyLevel = findViewById(R.id.party_level)
        spinnerPartySize = findViewById(R.id.party_size)
        spinnerTreasureValue = findViewById(R.id.treasure_value)
        spinnerItemsRarity = findViewById(R.id.items_rarity)
        spinnerSize = findViewById(R.id.dungeon_size)
        spinnerRoomDens = findViewById(R.id.room_density)
        spinnerRoomSize = findViewById(R.id.room_size)
        spinnerTraps = findViewById(R.id.traps)
        spinnerCorridors = findViewById(R.id.corridors)
        spinnerMonsterType = findViewById(R.id.monster_type)
        spinnerDeadEnds = findViewById(R.id.dead_end)
        spinnerTheme = findViewById(R.id.theme)
        spinnerRoamingMonsters = findViewById(R.id.roaming_monsters)
    }

    override fun onDestroy() {
        super.onDestroy()
        loadDialog?.dismiss()
        spinnerMonsterType.dialog?.dismiss()
    }

    private fun loadListener(): View.OnClickListener {
        return View.OnClickListener { showLoadDialog() }
    }

    private fun showLoadDialog() {
        val dlg = Dialog(this, R.style.Dialog)
        loadDialog = dlg
        dlg.setContentView(R.layout.load_dungeons_popup)
        dlg.setTitle("Select saved dungeon")
        val closePopupBtn: Button = dlg.findViewById(R.id.popup_close_button)
        val deleteAllBtn: Button = dlg.findViewById(R.id.delete_all_dungeon_button)
        setButtonStyle(closePopupBtn)
        setButtonStyle(deleteAllBtn)
        getDataFromDB(dlg)
        closePopupBtn.setOnClickListener { dlg.dismiss() }
        deleteAllBtn.setOnClickListener { deleteAllData() }
        dlg.show()
    }

    private fun setButtonStyle(button: Button) {
        button.setBackgroundColor(getColor(R.color.primaryAccent))
        button.setTextColor(Color.WHITE)
    }

    private fun deleteAllData() {
        val dialogClickListener = DialogInterface.OnClickListener { _, button ->
            if (button == DialogInterface.BUTTON_POSITIVE) {
                contentResolver.delete(DungeonsProvider.CONTENT_URI, null, null)
                restartLoader()
                Toast.makeText(this@MainActivity, getString(R.string.all_deleted), Toast.LENGTH_SHORT).show()
            }
        }
        val listView = list
        if (listView != null && listView.count > 0) {
            AlertDialog.Builder(this)
                .setMessage(getString(R.string.delete_confirmation_text))
                .setPositiveButton(getString(android.R.string.ok), dialogClickListener)
                .setNegativeButton(getString(android.R.string.cancel), dialogClickListener)
                .show()
        }
    }

    private fun getDataFromDB(dialog: Dialog) {
        restartLoader()
        val listView: ListView = dialog.findViewById(R.id.load_dungeon_list)
        list = listView
        listView.adapter = cursorAdapter
        listView.setOnItemClickListener { _, _, _, l -> getSelectedDungeon(l) }
        listView.setOnItemLongClickListener { _, _, _, l ->
            deleteSingleDungeon(l)
            true
        }
    }

    private fun deleteSingleDungeon(id: Long) {
        getUriAndFilter(id)
        val dialogClickListener = DialogInterface.OnClickListener { _, button ->
            if (button == DialogInterface.BUTTON_POSITIVE) {
                contentResolver.delete(DungeonsProvider.CONTENT_URI, filter, null)
                restartLoader()
                Toast.makeText(this@MainActivity, getString(R.string.selected_deleted), Toast.LENGTH_SHORT).show()
            }
        }
        AlertDialog.Builder(this)
            .setMessage(getString(R.string.delete_confirmation_single_text))
            .setPositiveButton(getString(android.R.string.ok), dialogClickListener)
            .setNegativeButton(getString(android.R.string.cancel), dialogClickListener)
            .show()
    }

    private fun getSelectedDungeon(id: Long) {
        getUriAndFilter(id)
        val u = uri ?: return
        val cursor = contentResolver.query(u, DBOpenHelper.ALL_COLUMNS, filter, null, null)
        if (cursor != null) {
            loadDungeon(cursor)
        }
    }

    private fun getUriAndFilter(id: Long) {
        uri = "${DungeonsProvider.CONTENT_URI}/$id".toUri()
        filter = "${DBOpenHelper.DUNGEON_ID}=${uri?.lastPathSegment}"
    }

    private fun restartLoader() {
        LoaderManager.getInstance(this).restartLoader(0, null, this)
    }

    private fun loadDungeon(cursor: Cursor) {
        val dungeon = Intent(this, DungeonActivity::class.java)
        val extras = Bundle()
        cursor.moveToFirst()
        for (column in DBOpenHelper.TO_BUNDLE) {
            extras.putString(column, cursor.getString(cursor.getColumnIndexOrThrow(column)))
        }
        cursor.close()
        extras.putString("URI", uri.toString())
        extras.putString("FILTER", filter)
        extras.putString("THEME", spinnerTheme.selectedItem.toString())
        dungeon.putExtras(extras)
        setSpinners(extras)
        startActivity(dungeon)
        loadDialog?.dismiss()
    }

    private fun setSpinners(extras: Bundle) {
        spinnerDifficulty.setSelection(getIndex(spinnerDifficulty, extras.getString(DBOpenHelper.DUNGEON_DIFFICULTY)))
        spinnerPartyLevel.setSelection(getIndex(spinnerPartyLevel, extras.getString(DBOpenHelper.PARTY_LEVEL)))
        spinnerPartySize.setSelection(getIndex(spinnerPartySize, extras.getString(DBOpenHelper.PARTY_SIZE)))
        spinnerTreasureValue.setSelection(getIndex(spinnerTreasureValue, extras.getString(DBOpenHelper.TREASURE_VALUE)))
        spinnerItemsRarity.setSelection(getIndex(spinnerItemsRarity, extras.getString(DBOpenHelper.ITEMS_RARITY)))
        spinnerSize.setSelection(getIndex(spinnerSize, extras.getString(DBOpenHelper.DUNGEON_SIZE)))
        spinnerRoomDens.setSelection(getIndex(spinnerRoomDens, extras.getString(DBOpenHelper.ROOM_DENSITY)))
        spinnerRoomSize.setSelection(getIndex(spinnerRoomSize, extras.getString(DBOpenHelper.ROOM_SIZE)))
        spinnerTraps.setSelection(getIndex(spinnerTraps, extras.getString(DBOpenHelper.TRAPS)))
        spinnerCorridors.setSelection(getIndex(spinnerCorridors, extras.getString(DBOpenHelper.CORRIDORS)))
        val mt = extras.getString(DBOpenHelper.MONSTER_TYPE)
        if (mt != null) {
            spinnerMonsterType.setAllText(mt)
        }
        spinnerDeadEnds.setSelection(getIndex(spinnerDeadEnds, extras.getString(DBOpenHelper.DEAD_ENDS)))
        spinnerRoamingMonsters.setSelection(getIndex(spinnerRoamingMonsters, extras.getString(DBOpenHelper.ROAMING_MONSTERS)))
    }

    private fun getIndex(spinner: Spinner, myString: String?): Int {
        if (myString == null) return 0
        for (i in 0 until spinner.count) {
            if (spinner.getItemAtPosition(i).toString().equals(myString, ignoreCase = true)) {
                return i
            }
        }
        return 0
    }

    private fun generateDungeon() {
        val dungeon = Intent(this, DungeonActivity::class.java)
        val extras = Bundle().apply {
            putString(DBOpenHelper.LOADED_DUNGEON, null)
            putString(DBOpenHelper.LOADED_ROOM_DESCRIPTION, null)
            putString(DBOpenHelper.LOADED_TRAP_DESCRIPTION, null)
            putString(DBOpenHelper.LOADED_ROAMING_MONSTERS, null)
            putString(DBOpenHelper.DUNGEON_DIFFICULTY, spinnerDifficulty.selectedItem.toString())
            putString(DBOpenHelper.PARTY_LEVEL, spinnerPartyLevel.selectedItem.toString())
            putString(DBOpenHelper.PARTY_SIZE, spinnerPartySize.selectedItem.toString())
            putString(DBOpenHelper.TREASURE_VALUE, spinnerTreasureValue.selectedItem.toString())
            putString(DBOpenHelper.ITEMS_RARITY, spinnerItemsRarity.selectedItem.toString())
            putString(DBOpenHelper.DUNGEON_SIZE, spinnerSize.selectedItem.toString())
            putString(DBOpenHelper.ROOM_DENSITY, spinnerRoomDens.selectedItem.toString())
            putString(DBOpenHelper.ROOM_SIZE, spinnerRoomSize.selectedItem.toString())
            putString(DBOpenHelper.TRAPS, spinnerTraps.selectedItem.toString())
            putString(DBOpenHelper.CORRIDORS, spinnerCorridors.selectedItem.toString())
            putString(DBOpenHelper.MONSTER_TYPE, spinnerMonsterType.allText)
            putString(DBOpenHelper.DEAD_ENDS, spinnerDeadEnds.selectedItem.toString())
            putString(DBOpenHelper.ROAMING_MONSTERS, spinnerRoamingMonsters.selectedItem.toString())
            putString("THEME", spinnerTheme.selectedItem.toString())
        }
        dungeon.putExtras(extras)
        startActivity(dungeon)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_about) {
            val about = Intent(this, AboutActivity::class.java)
            startActivity(about)
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> {
        return CursorLoader(this, DungeonsProvider.CONTENT_URI, null, null, null, null)
    }

    override fun onLoadFinished(loader: Loader<Cursor>, data: Cursor?) {
        cursorAdapter?.swapCursor(data)
    }

    override fun onLoaderReset(loader: Loader<Cursor>) {
        cursorAdapter?.changeCursor(null)
    }
}
