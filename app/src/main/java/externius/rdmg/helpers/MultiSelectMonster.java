package externius.rdmg.helpers;

import static android.content.res.Resources.getSystem;

import android.app.Dialog;
import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import java.util.Arrays;
import java.util.List;

import externius.rdmg.R;

public class MultiSelectMonster extends androidx.appcompat.widget.AppCompatTextView {
    private static final String ANY = "Any";
    private static final String NONE = "None";
    private ArrayAdapter<String> mAdapter;
    private boolean[] mOldSelection;
    private boolean[] mSelected;
    private String mAllText;
    private final List<String> monsterTypeList = Arrays.asList(getResources().getStringArray(R.array.monster_type_array));
    private Dialog dialog;

    public MultiSelectMonster(Context context) {
        super(context);
    }

    public MultiSelectMonster(Context context, AttributeSet attr) {
        this(context, attr, androidx.appcompat.R.attr.spinnerStyle);
    }

    public MultiSelectMonster(Context context, AttributeSet attr, int defStyle) {
        super(context, attr, defStyle);
        init(context);
    }

    private void init(Context context) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_multiple_choice);
        adapter.addAll(monsterTypeList);
        setAdapter(adapter);
    }

    private void setButtonStyle(Button button) {
        button.setBackgroundColor(getResources().getColor(R.color.primaryAccent, null));
        button.setTextColor(Color.WHITE);
    }

    private final OnClickListener onClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            dialog = new Dialog(getContext(), R.style.Dialog);
            dialog.setContentView(R.layout.monster_type_popup);
            dialog.setTitle("Select monster type");
            Button closePopupBtn = dialog.findViewById(R.id.monster_cancel);
            Button okBtn = dialog.findViewById(R.id.monster_ok);
            Button invBtn = dialog.findViewById(R.id.monster_inverse);
            setButtonStyle(closePopupBtn);
            setButtonStyle(okBtn);
            setButtonStyle(invBtn);
            ListView list = dialog.findViewById(R.id.monster_type_list);
            list.setAdapter(mAdapter);
            saveSelection();
            setChecked(list);
            list.setOnItemClickListener((parent, view, position, id) -> mSelected[position] = !mSelected[position]);
            closePopupBtn.setOnClickListener(view -> {
                restoreSelection();
                dialog.dismiss();
            });
            okBtn.setOnClickListener(view -> {
                refreshSpinnerText();
                dialog.dismiss();
            });
            invBtn.setOnClickListener(view -> {
                selectInverse();
                setChecked(list);
            });
            dialog.show();
        }
    };

    private void setChecked(ListView list) {
        for (int i = 0; i < mSelected.length; i++) {
            list.setItemChecked(i, mSelected[i]);
        }
    }

    private void restoreSelection() {
        System.arraycopy(mOldSelection, 0, mSelected, 0, mSelected.length);
    }

    private void saveSelection() {
        System.arraycopy(mSelected, 0, mOldSelection, 0, mOldSelection.length);
    }

    private void selectInverse() {
        for (int i = 0; i < mSelected.length; i++) {
            mSelected[i] = !mSelected[i];
        }
    }

    private final DataSetObserver dataSetObserver = new DataSetObserver() {
        @Override
        public void onChanged() {
            selectAll(true);
        }
    };

    private void setAdapter(ArrayAdapter<String> adapter) {
        setOnClickListener(null);
        mAdapter = adapter;
        mAdapter.registerDataSetObserver(dataSetObserver);
        selectAll(true);
        setOnClickListener(onClickListener);
        mAllText = ANY;
        addText(mAllText);
    }

    private void selectAll(boolean isSelected) {
        mOldSelection = new boolean[mAdapter.getCount()];
        mSelected = new boolean[mAdapter.getCount()];
        Arrays.fill(mSelected, isSelected);
    }

    private void refreshSpinnerText() {
        StringBuilder stringBuilder = new StringBuilder();
        boolean someUnselected = false;
        boolean allUnselected = true;
        int count = 0;
        for (int i = 0; i < mAdapter.getCount(); i++) {
            if (mSelected[i]) {
                stringBuilder.append(mAdapter.getItem(i));
                stringBuilder.append(", ");
                allUnselected = false;
                count++;
            } else {
                someUnselected = true;
            }
        }
        if (mAdapter.getCount() == count) {
            mAllText = ANY;
            addText(mAllText);
            return;
        }
        String spinnerText = NONE;
        mAllText = NONE;
        if (!allUnselected) {
            if (someUnselected) {
                spinnerText = stringBuilder.toString();
                if (spinnerText.length() > 2) {
                    spinnerText = spinnerText.substring(0, spinnerText.length() - 2);
                    mAllText = spinnerText;
                }
            } else {
                mAllText = ANY;
                spinnerText = ANY;
            }
        }
        addText(spinnerText);
    }

    public String getAllText() {
        return mAllText;
    }

    public void setAllText(String allText) {
        this.mAllText = allText;
        switch (allText) {
            case ANY -> selectAll(true);
            case NONE -> selectAll(false);
            default -> {
                allText = allText.replaceAll(" ", "");
                String[] array = allText.trim().split(",");
                if (array.length > 0) {
                    mSelected = new boolean[mAdapter.getCount()];
                    for (int i = 0; i < mSelected.length; i++) {
                        for (String monster : array) {
                            if (monsterTypeList.get(i).contains(monster)) {
                                mSelected[i] = true;
                                break;
                            }
                        }
                    }
                }
            }
        }
        addText(allText);
    }

    private void addText(String Text) {
        float density = getSystem().getDisplayMetrics().density;
        float w = getSystem().getDisplayMetrics().widthPixels / (density * 12);
        if (Text.length() > w && w != 0) {
            Text = Text.substring(0, (int) w) + "...";
        }
        setText(Text);
        setTextSize(15);
    }

    public Dialog getDialog() {
        return dialog;
    }
}