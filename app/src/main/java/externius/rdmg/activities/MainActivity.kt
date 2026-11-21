package externius.rdmg.activities

import android.app.AlertDialog
import android.app.Dialog
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
import android.view.Window
import android.widget.AdapterView
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import externius.rdmg.R
import externius.rdmg.databinding.ActivityMainBinding
import externius.rdmg.databinding.ListItemDungeonBinding
import externius.rdmg.databinding.LoadDungeonsPopupBinding
import externius.rdmg.database.DBOpenHelper
import externius.rdmg.database.DungeonEntity
import externius.rdmg.database.DungeonSummary
import externius.rdmg.helpers.ThemeHelper
import externius.rdmg.helpers.applyPrimaryStyle
import externius.rdmg.viewmodels.MainViewModel
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()
    private var loadDialog: Dialog? = null
    private var dungeonAdapter: DungeonSummaryAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setToolbar()
        setDefaultValues()
        addListeners()
        if (savedInstanceState != null) {
            val monsterType = savedInstanceState.getString(DBOpenHelper.MONSTER_TYPE)
            if (monsterType != null) {
                binding.contentMain.monsterType.setAllText(monsterType)
            }
        }
    }

    private fun setToolbar() {
        setSupportActionBar(binding.toolbar)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val text = binding.contentMain.monsterType.allText
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
                if (binding.contentMain.corridors.selectedItem.toString()
                        .equals("no", ignoreCase = true)
                ) {
                    binding.contentMain.traps.isEnabled = false
                    binding.contentMain.roomDensity.isEnabled = false
                    binding.contentMain.deadEnd.isEnabled = false
                    binding.contentMain.roamingMonsters.isEnabled = false
                } else {
                    binding.contentMain.traps.isEnabled = true
                    binding.contentMain.roomDensity.isEnabled = true
                    binding.contentMain.deadEnd.isEnabled = true
                    checkMonsterType()
                }
            }

            override fun onNothingSelected(adapterView: AdapterView<*>?) {}
        }
    }

    private fun addListeners() {
        binding.contentMain.corridors.onItemSelectedListener = getCorridorItemSelectedListener()
        binding.contentMain.monsterType.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                checkMonsterType()
            }
        })
        binding.contentMain.generateButton.setOnClickListener { generateDungeon() }
        binding.contentMain.loadButton.setOnClickListener { showLoadDialog() }
    }

    private fun checkMonsterType() {
        val allText = binding.contentMain.monsterType.allText ?: ""
        if (allText.equals("none", ignoreCase = true)) {
            binding.contentMain.roamingMonsters.isEnabled = false
            binding.contentMain.roamingMonsters.setSelection(0)
        } else if (binding.contentMain.corridors.selectedItem.toString()
                .equals("yes", ignoreCase = true)
        ) {
            binding.contentMain.roamingMonsters.isEnabled = true
        }
    }

    private fun setDefaultValues() {
        binding.contentMain.dungeonDifficulty.setSelection(1)
        binding.contentMain.partySize.setSelection(3)
        binding.contentMain.traps.setSelection(1)
        binding.contentMain.treasureValue.setSelection(1)
        binding.contentMain.itemsRarity.setSelection(1)
    }

    override fun onDestroy() {
        super.onDestroy()
        loadDialog?.dismiss()
        binding.contentMain.monsterType.dialog?.dismiss()
    }

    private fun showLoadDialog() {
        val dlg = Dialog(this, R.style.Dialog)
        loadDialog = dlg
        dlg.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val popupBinding = LoadDungeonsPopupBinding.inflate(layoutInflater)
        dlg.setContentView(popupBinding.root)
        popupBinding.popupCloseButton.applyPrimaryStyle()
        popupBinding.deleteAllDungeonButton.applyPrimaryStyle()
        setupLoadDialogList(dlg, popupBinding)
        popupBinding.popupCloseButton.setOnClickListener { dlg.dismiss() }
        popupBinding.deleteAllDungeonButton.setOnClickListener { deleteAllData() }
        dlg.show()
    }

    private fun setupLoadDialogList(dialog: Dialog, popupBinding: LoadDungeonsPopupBinding) {
        popupBinding.loadDungeonList.layoutManager = LinearLayoutManager(this)
        val adapter = DungeonSummaryAdapter(
            onItemClick = { summary -> getSelectedDungeon(summary.id) },
            onItemLongClick = { summary -> deleteSingleDungeon(summary.id) }
        )
        dungeonAdapter = adapter
        popupBinding.loadDungeonList.adapter = adapter

        val collectorJob = lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.dungeonSummaries.collect { summaries ->
                    adapter.submitList(summaries)
                    val hasItems = summaries.isNotEmpty()
                    popupBinding.deleteAllDungeonButton.isVisible = hasItems
                    popupBinding.loadDungeonList.isVisible = hasItems
                    popupBinding.emptyTextView.isVisible = !hasItems
                }
            }
        }
        dialog.setOnDismissListener {
            collectorJob.cancel()
            dungeonAdapter = null
            loadDialog = null
        }
    }

    private fun deleteAllData() {
        val adapter = dungeonAdapter
        if (adapter != null && adapter.itemCount > 0) {
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
            putExtra("THEME", binding.contentMain.theme.selectedItem.toString())
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
        binding.contentMain.dungeonDifficulty.setSelection(
            getIndex(
                binding.contentMain.dungeonDifficulty,
                dungeon.dungeonDifficulty
            )
        )
        binding.contentMain.partyLevel.setSelection(
            getIndex(
                binding.contentMain.partyLevel,
                dungeon.partyLevel
            )
        )
        binding.contentMain.partySize.setSelection(
            getIndex(
                binding.contentMain.partySize,
                dungeon.partySize
            )
        )
        binding.contentMain.treasureValue.setSelection(
            getIndex(
                binding.contentMain.treasureValue,
                dungeon.treasureValue
            )
        )
        binding.contentMain.itemsRarity.setSelection(
            getIndex(
                binding.contentMain.itemsRarity,
                dungeon.itemsRarity
            )
        )
        binding.contentMain.dungeonSize.setSelection(
            getIndex(
                binding.contentMain.dungeonSize,
                dungeon.dungeonSize
            )
        )
        binding.contentMain.roomDensity.setSelection(
            getIndex(
                binding.contentMain.roomDensity,
                dungeon.roomDensity
            )
        )
        binding.contentMain.roomSize.setSelection(
            getIndex(
                binding.contentMain.roomSize,
                dungeon.roomSize
            )
        )
        binding.contentMain.traps.setSelection(getIndex(binding.contentMain.traps, dungeon.traps))
        binding.contentMain.corridors.setSelection(
            getIndex(
                binding.contentMain.corridors,
                dungeon.corridors
            )
        )
        if (dungeon.monsterType != null) {
            binding.contentMain.monsterType.setAllText(dungeon.monsterType)
        }
        binding.contentMain.deadEnd.setSelection(
            getIndex(
                binding.contentMain.deadEnd,
                dungeon.deadEnds
            )
        )
        binding.contentMain.roamingMonsters.setSelection(
            getIndex(
                binding.contentMain.roamingMonsters,
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
            putString(
                DBOpenHelper.DUNGEON_DIFFICULTY,
                binding.contentMain.dungeonDifficulty.selectedItem.toString()
            )
            putString(
                DBOpenHelper.PARTY_LEVEL,
                binding.contentMain.partyLevel.selectedItem.toString()
            )
            putString(
                DBOpenHelper.PARTY_SIZE,
                binding.contentMain.partySize.selectedItem.toString()
            )
            putString(
                DBOpenHelper.TREASURE_VALUE,
                binding.contentMain.treasureValue.selectedItem.toString()
            )
            putString(
                DBOpenHelper.ITEMS_RARITY,
                binding.contentMain.itemsRarity.selectedItem.toString()
            )
            putString(
                DBOpenHelper.DUNGEON_SIZE,
                binding.contentMain.dungeonSize.selectedItem.toString()
            )
            putString(
                DBOpenHelper.ROOM_DENSITY,
                binding.contentMain.roomDensity.selectedItem.toString()
            )
            putString(DBOpenHelper.ROOM_SIZE, binding.contentMain.roomSize.selectedItem.toString())
            putString(DBOpenHelper.TRAPS, binding.contentMain.traps.selectedItem.toString())
            putString(DBOpenHelper.CORRIDORS, binding.contentMain.corridors.selectedItem.toString())
            putString(DBOpenHelper.MONSTER_TYPE, binding.contentMain.monsterType.allText)
            putString(DBOpenHelper.DEAD_ENDS, binding.contentMain.deadEnd.selectedItem.toString())
            putString(
                DBOpenHelper.ROAMING_MONSTERS,
                binding.contentMain.roamingMonsters.selectedItem.toString()
            )
            putString("THEME", binding.contentMain.theme.selectedItem.toString())
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

    private class DungeonSummaryAdapter(
        private val onItemClick: (DungeonSummary) -> Unit,
        private val onItemLongClick: (DungeonSummary) -> Unit
    ) : ListAdapter<DungeonSummary, DungeonSummaryAdapter.ViewHolder>(DungeonDiffCallback) {

        class ViewHolder(val binding: ListItemDungeonBinding) :
            RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = ListItemDungeonBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = getItem(position)
            holder.binding.text1.text = item?.dungeonName ?: ""
            holder.itemView.setOnClickListener {
                val pos = holder.bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    val currentItem = getItem(pos)
                    if (currentItem != null) onItemClick(currentItem)
                }
            }
            holder.itemView.setOnLongClickListener {
                val pos = holder.bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    val currentItem = getItem(pos)
                    if (currentItem != null) onItemLongClick(currentItem)
                }
                true
            }
        }
    }

    private object DungeonDiffCallback : DiffUtil.ItemCallback<DungeonSummary>() {
        override fun areItemsTheSame(oldItem: DungeonSummary, newItem: DungeonSummary): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: DungeonSummary, newItem: DungeonSummary): Boolean {
            return oldItem == newItem
        }
    }
}
