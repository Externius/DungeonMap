package externius.rdmg.activities

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import externius.rdmg.R
import externius.rdmg.database.DBOpenHelper
import externius.rdmg.database.DungeonEntity
import externius.rdmg.database.DungeonSummary
import externius.rdmg.helpers.MultiSelectMonster
import externius.rdmg.helpers.ThemeHelper
import externius.rdmg.helpers.applyPrimaryStyle
import externius.rdmg.viewmodels.MainViewModel
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
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

    private val viewModel: MainViewModel by viewModels()
    private var loadDialog: Dialog? = null
    private var dungeonAdapter: DungeonSummaryAdapter? = null
    private var list: ListView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setToolbar()
        getSpinners()
        setDefaultValues()
        addListeners()
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
            override fun onItemSelected(
                adapterView: AdapterView<*>?,
                view: View?,
                i: Int,
                l: Long
            ) {
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
        loadButton.setOnClickListener { showLoadDialog() }
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

    private fun showLoadDialog() {
        val dlg = Dialog(this, R.style.Dialog)
        loadDialog = dlg
        dlg.setContentView(R.layout.load_dungeons_popup)
        dlg.setTitle(getString(R.string.select_saved_dungeon))
        val closePopupBtn: Button = dlg.findViewById(R.id.popup_close_button)
        val deleteAllBtn: Button = dlg.findViewById(R.id.delete_all_dungeon_button)
        closePopupBtn.applyPrimaryStyle()
        deleteAllBtn.applyPrimaryStyle()
        setupLoadDialogList(dlg)
        closePopupBtn.setOnClickListener { dlg.dismiss() }
        deleteAllBtn.setOnClickListener { deleteAllData() }
        dlg.show()
    }

    private fun setupLoadDialogList(dialog: Dialog) {
        val listView: ListView = dialog.findViewById(R.id.load_dungeon_list)
        list = listView
        val adapter = DungeonSummaryAdapter(this, mutableListOf())
        dungeonAdapter = adapter
        listView.adapter = adapter

        listView.setOnItemClickListener { _, _, position, _ ->
            val summary = adapter.getItem(position) ?: return@setOnItemClickListener
            getSelectedDungeon(summary.id)
        }
        listView.setOnItemLongClickListener { _, _, position, _ ->
            val summary = adapter.getItem(position) ?: return@setOnItemLongClickListener true
            deleteSingleDungeon(summary.id)
            true
        }

        val collectorJob = lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.dungeonSummaries.collect { summaries ->
                    adapter.clear()
                    adapter.addAll(summaries)
                }
            }
        }
        dialog.setOnDismissListener {
            collectorJob.cancel()
            list = null
            dungeonAdapter = null
            loadDialog = null
        }
    }

    private fun deleteAllData() {
        val listView = list
        if (listView != null && listView.count > 0) {
            AlertDialog.Builder(this)
                .setMessage(getString(R.string.delete_confirmation_text))
                .setPositiveButton(getString(android.R.string.ok)) { _, _ ->
                    viewModel.deleteAllDungeons()
                    Toast.makeText(
                        this@MainActivity,
                        getString(R.string.all_deleted),
                        Toast.LENGTH_SHORT
                    ).show()
                }
                .setNegativeButton(getString(android.R.string.cancel), null)
                .show()
        }
    }

    private fun deleteSingleDungeon(id: Long) {
        AlertDialog.Builder(this)
            .setMessage(getString(R.string.delete_confirmation_single_text))
            .setPositiveButton(getString(android.R.string.ok)) { _, _ ->
                viewModel.deleteDungeon(id)
                Toast.makeText(
                    this@MainActivity,
                    getString(R.string.selected_deleted),
                    Toast.LENGTH_SHORT
                ).show()
            }
            .setNegativeButton(getString(android.R.string.cancel), null)
            .show()
    }

    private fun getSelectedDungeon(id: Long) {
        lifecycleScope.launch {
            val entity = viewModel.getDungeonById(id)
            if (entity != null) {
                loadDungeon(entity)
            }
        }
    }

    private fun loadDungeon(dungeon: DungeonEntity) {
        val intent = Intent(this, DungeonActivity::class.java).apply {
            putExtra("DUNGEON_ID", dungeon.id)
            putExtra("THEME", spinnerTheme.selectedItem.toString())
            putExtra(DBOpenHelper.CORRIDORS, dungeon.corridors)
            putExtra(DBOpenHelper.DEAD_ENDS, dungeon.deadEnds)
            putExtra(DBOpenHelper.DUNGEON_DIFFICULTY, dungeon.dungeonDifficulty)
            putExtra(DBOpenHelper.DUNGEON_SIZE, dungeon.dungeonSize)
            putExtra(DBOpenHelper.ITEMS_RARITY, dungeon.itemsRarity)
            putExtra(DBOpenHelper.MONSTER_TYPE, dungeon.monsterType)
            putExtra(DBOpenHelper.PARTY_LEVEL, dungeon.partyLevel)
            putExtra(DBOpenHelper.PARTY_SIZE, dungeon.partySize)
            putExtra(DBOpenHelper.ROAMING_MONSTERS, dungeon.roamingMonsters)
            putExtra(DBOpenHelper.ROOM_DENSITY, dungeon.roomDensity)
            putExtra(DBOpenHelper.ROOM_SIZE, dungeon.roomSize)
            putExtra(DBOpenHelper.TRAPS, dungeon.traps)
            putExtra(DBOpenHelper.TREASURE_VALUE, dungeon.treasureValue)
        }
        setSpinnersFromEntity(dungeon)
        startActivity(intent)
        loadDialog?.dismiss()
    }

    private fun setSpinnersFromEntity(dungeon: DungeonEntity) {
        spinnerDifficulty.setSelection(getIndex(spinnerDifficulty, dungeon.dungeonDifficulty))
        spinnerPartyLevel.setSelection(getIndex(spinnerPartyLevel, dungeon.partyLevel))
        spinnerPartySize.setSelection(getIndex(spinnerPartySize, dungeon.partySize))
        spinnerTreasureValue.setSelection(getIndex(spinnerTreasureValue, dungeon.treasureValue))
        spinnerItemsRarity.setSelection(getIndex(spinnerItemsRarity, dungeon.itemsRarity))
        spinnerSize.setSelection(getIndex(spinnerSize, dungeon.dungeonSize))
        spinnerRoomDens.setSelection(getIndex(spinnerRoomDens, dungeon.roomDensity))
        spinnerRoomSize.setSelection(getIndex(spinnerRoomSize, dungeon.roomSize))
        spinnerTraps.setSelection(getIndex(spinnerTraps, dungeon.traps))
        spinnerCorridors.setSelection(getIndex(spinnerCorridors, dungeon.corridors))
        if (dungeon.monsterType != null) {
            spinnerMonsterType.setAllText(dungeon.monsterType)
        }
        spinnerDeadEnds.setSelection(getIndex(spinnerDeadEnds, dungeon.deadEnds))
        spinnerRoamingMonsters.setSelection(
            getIndex(
                spinnerRoamingMonsters,
                dungeon.roamingMonsters
            )
        )
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
            putLong("DUNGEON_ID", -1L)
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

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        val menuItem = menu?.findItem(R.id.action_theme_toggle)
        val currentMode = ThemeHelper.getSavedThemeMode(this)
        val isNightMode = when (currentMode) {
            ThemeHelper.MODE_NIGHT -> true
            ThemeHelper.MODE_LIGHT -> false
            else -> {
                val nightModeFlags =
                    resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
                nightModeFlags == Configuration.UI_MODE_NIGHT_YES
            }
        }
        if (isNightMode) {
            menuItem?.setIcon(R.drawable.ic_light_mode)
        } else {
            menuItem?.setIcon(R.drawable.ic_night_mode)
        }
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_about -> {
                val about = Intent(this, AboutActivity::class.java)
                startActivity(about)
                true
            }

            R.id.action_theme_toggle -> {
                showThemeSelectionDialog()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showThemeSelectionDialog() {
        val modes = arrayOf(
            getString(R.string.theme_system_default),
            getString(R.string.theme_light),
            getString(R.string.theme_dark)
        )
        val currentMode = ThemeHelper.getSavedThemeMode(this)
        val selectedIndex = when (currentMode) {
            ThemeHelper.MODE_LIGHT -> 1
            ThemeHelper.MODE_NIGHT -> 2
            else -> 0
        }

        AlertDialog.Builder(this)
            .setTitle(R.string.select_theme)
            .setSingleChoiceItems(modes, selectedIndex) { dialog, which ->
                val newMode = when (which) {
                    1 -> ThemeHelper.MODE_LIGHT
                    2 -> ThemeHelper.MODE_NIGHT
                    else -> ThemeHelper.MODE_SYSTEM
                }
                ThemeHelper.setThemeMode(this, newMode)
                dialog.dismiss()
            }
            .show()
    }

    private class ViewHolder(val textView: TextView)

    private class DungeonSummaryAdapter(
        context: Context,
        summaries: MutableList<DungeonSummary>
    ) : ArrayAdapter<DungeonSummary>(
        context,
        R.layout.list_item_dungeon,
        android.R.id.text1,
        summaries
    ) {
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view: View
            val holder: ViewHolder
            if (convertView == null) {
                view =
                    LayoutInflater.from(context).inflate(R.layout.list_item_dungeon, parent, false)
                holder = ViewHolder(view.findViewById(android.R.id.text1))
                view.tag = holder
            } else {
                view = convertView
                holder = view.tag as ViewHolder
            }
            val item = getItem(position)
            holder.textView.text = item?.dungeonName ?: ""
            return view
        }
    }
}
