package externius.rdmg.helpers

import android.app.Dialog
import android.content.Context
import android.database.DataSetObserver
import android.graphics.Color
import android.util.AttributeSet
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import androidx.appcompat.widget.AppCompatTextView
import externius.rdmg.R

class MultiSelectMonster @JvmOverloads constructor(
    context: Context,
    attr: AttributeSet? = null,
    defStyle: Int = androidx.appcompat.R.attr.spinnerStyle
) : AppCompatTextView(context, attr, defStyle) {

    private var mAdapter: ArrayAdapter<String>? = null
    private var mOldSelection: BooleanArray = BooleanArray(0)
    private var mSelected: BooleanArray = BooleanArray(0)
    var allText: String? = null
        private set

    private val monsterTypeList: List<String> =
        context.resources.getStringArray(R.array.monster_type_array).toList()

    var dialog: Dialog? = null
        private set

    private val dataSetObserver = object : DataSetObserver() {
        override fun onChanged() {
            selectAll(true)
        }
    }

    private val onClickListener = OnClickListener {
        val adapter = mAdapter ?: return@OnClickListener
        dialog = Dialog(context, R.style.Dialog).apply {
            setContentView(R.layout.monster_type_popup)
            setTitle("Select monster type")
            val closePopupBtn: Button = findViewById(R.id.monster_cancel)
            val okBtn: Button = findViewById(R.id.monster_ok)
            val invBtn: Button = findViewById(R.id.monster_inverse)
            setButtonStyle(closePopupBtn)
            setButtonStyle(okBtn)
            setButtonStyle(invBtn)
            val list: ListView = findViewById(R.id.monster_type_list)
            list.adapter = adapter
            saveSelection()
            setChecked(list)
            list.setOnItemClickListener { _, _, position, _ ->
                mSelected[position] = !mSelected[position]
            }
            closePopupBtn.setOnClickListener {
                restoreSelection()
                dismiss()
            }
            okBtn.setOnClickListener {
                refreshSpinnerText()
                dismiss()
            }
            invBtn.setOnClickListener {
                selectInverse()
                setChecked(list)
            }
            show()
        }
    }

    init {
        init(context)
    }

    private fun init(context: Context) {
        val adapter = ArrayAdapter<String>(context, android.R.layout.simple_list_item_multiple_choice)
        adapter.addAll(monsterTypeList)
        setAdapter(adapter)
    }

    private fun setButtonStyle(button: Button) {
        button.setBackgroundColor(context.getColor(R.color.primaryAccent))
        button.setTextColor(Color.WHITE)
    }

    private fun setChecked(list: ListView) {
        for (i in mSelected.indices) {
            list.setItemChecked(i, mSelected[i])
        }
    }

    private fun restoreSelection() {
        System.arraycopy(mOldSelection, 0, mSelected, 0, mSelected.size)
    }

    private fun saveSelection() {
        System.arraycopy(mSelected, 0, mOldSelection, 0, mOldSelection.size)
    }

    private fun selectInverse() {
        for (i in mSelected.indices) {
            mSelected[i] = !mSelected[i]
        }
    }

    private fun setAdapter(adapter: ArrayAdapter<String>) {
        setOnClickListener(null)
        mAdapter = adapter
        adapter.registerDataSetObserver(dataSetObserver)
        selectAll(true)
        setOnClickListener(onClickListener)
        allText = ANY
        addText(ANY)
    }

    private fun selectAll(isSelected: Boolean) {
        val count = mAdapter?.count ?: 0
        mOldSelection = BooleanArray(count)
        mSelected = BooleanArray(count) { isSelected }
    }

    private fun refreshSpinnerText() {
        val adapter = mAdapter ?: return
        val stringBuilder = StringBuilder()
        var someUnselected = false
        var allUnselected = true
        var count = 0
        for (i in 0 until adapter.count) {
            if (mSelected[i]) {
                stringBuilder.append(adapter.getItem(i))
                stringBuilder.append(", ")
                allUnselected = false
                count++
            } else {
                someUnselected = true
            }
        }
        if (adapter.count == count) {
            allText = ANY
            addText(ANY)
            return
        }
        var spinnerText = NONE
        allText = NONE
        if (!allUnselected) {
            if (someUnselected) {
                spinnerText = stringBuilder.toString()
                if (spinnerText.length > 2) {
                    spinnerText = spinnerText.substring(0, spinnerText.length - 2)
                    allText = spinnerText
                }
            } else {
                allText = ANY
                spinnerText = ANY
            }
        }
        addText(spinnerText)
    }

    fun setAllText(text: String) {
        var textVar = text
        this.allText = textVar
        when (textVar) {
            ANY -> selectAll(true)
            NONE -> selectAll(false)
            else -> {
                textVar = textVar.replace(" ", "")
                val array = textVar.trim().split(",")
                val adapter = mAdapter
                if (array.isNotEmpty() && adapter != null) {
                    mSelected = BooleanArray(adapter.count)
                    for (i in mSelected.indices) {
                        for (monster in array) {
                            if (monsterTypeList[i].contains(monster)) {
                                mSelected[i] = true
                                break
                            }
                        }
                    }
                }
            }
        }
        addText(textVar)
    }

    private fun addText(text: String) {
        var displayText = text
        val displayMetrics = context.resources.displayMetrics
        val density = displayMetrics.density
        val w = displayMetrics.widthPixels / (density * 12)
        if (displayText.length > w && w != 0f) {
            displayText = displayText.substring(0, w.toInt()) + "..."
        }
        setText(displayText)
        textSize = 15f
    }

    companion object {
        private const val ANY = "Any"
        private const val NONE = "None"
    }
}
