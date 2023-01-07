package externius.rdmg.activities;

import android.app.Activity;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.os.SystemClock;
import android.text.Html;
import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.res.ResourcesCompat;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;
import java.util.Scanner;

import externius.rdmg.R;
import externius.rdmg.database.DBOpenHelper;
import externius.rdmg.database.DungeonsProvider;
import externius.rdmg.helpers.Export;
import externius.rdmg.models.DungeonTile;
import externius.rdmg.models.RoamingMonsterDescription;
import externius.rdmg.models.RoomDescription;
import externius.rdmg.models.Textures;
import externius.rdmg.models.TrapDescription;
import externius.rdmg.views.DungeonMapView;

public class DungeonActivity extends AppCompatActivity {
    private final String SMALL = "Small";
    private final String MEDIUM = "Medium";
    private final String LARGE = "Large";
    private final Gson gson = new Gson();
    private boolean exported = false;
    private int dungeonDifficulty;
    private int partyLevel;
    private int partySize;
    private int dungeonSize;
    private int roomDensity;
    private int roomSize;
    private int traps;
    private int roamingMonsters;
    private boolean hasCorridor;
    private boolean hasDeadEnds;
    private String monsterType;
    private DungeonMapView dungeonView;
    private final ActivityResultLauncher<Intent> exportActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    if (data != null) {
                        saveDungeonMap(data);
                    }
                }
            });
    private String jsonMonster;
    private String jsonTreasure;
    private String theme;
    private String filter;
    private double treasureValue;
    private int itemsRarity;
    private DungeonTile[][] loadedDungeon;
    private List<RoomDescription> loadedRoomDescription;
    private List<TrapDescription> loadedTrapDescription;
    private List<RoamingMonsterDescription> loadedRoamingMonsterDescription;
    private Bundle extras = null;
    private long mLastClickTime = 0;
    private int area;
    private Dialog dialog;
    private GestureDetector mGestureDetector;
    private RelativeLayout layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dungeon);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        addLoadingScreen();
        addScreenText();
        mGestureDetector = new GestureDetector(this, new DungeonActivityGestureListener());
        new Thread(() -> {
            extras = getIntent().getExtras();
            setParameters();
            jsonMonster = readJSON(R.raw.monsters);
            jsonTreasure = readJSON(R.raw.treasures);
            dungeonView = getDungeonView(loadedDungeon != null);
            runOnUiThread(() -> drawDungeon(dungeonView));
        }).start();
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void addScreenText() {
        layout = findViewById(R.id.dungeon_layout);
        TextView screenText = new TextView(this);
        setScreenText(screenText);
        RelativeLayout.LayoutParams params = getScreenTextLayoutParams();
        screenText.setLayoutParams(params);
        layout.addView(screenText);
    }

    @NonNull
    private RelativeLayout.LayoutParams getScreenTextLayoutParams() {
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        params.addRule(RelativeLayout.CENTER_HORIZONTAL);
        Point size = getScreenSize();
        setArea(size);
        params.setMargins(10, 10, 10, size.y / 12);
        return params;
    }

    @NonNull
    private Point getScreenSize() {
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size;
    }

    private void setArea(Point size) {
        area = Math.min(size.x, size.y);
    }

    private void setScreenText(TextView screenText) {
        String[] text = getResources().getStringArray(R.array.loading_screen_gen_array);
        Random random = new Random();
        if (getIntent().getExtras() != null && getIntent().getExtras().getString("URI") != null) {
            screenText.setText(R.string.loading_text);
        } else {
            screenText.setText(text[random.nextInt(text.length)]);
        }
        screenText.setTextColor(Color.WHITE);
    }

    private void addLoadingScreen() {
        layout = findViewById(R.id.dungeon_layout);
        ImageView imageView = new ImageView(this);
        imageView.setImageResource(R.drawable.generating_screen);
        RelativeLayout.LayoutParams relativeParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        imageView.setLayoutParams(relativeParams);
        imageView.setScaleType(ImageView.ScaleType.FIT_XY);
        layout.addView(imageView);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dialog != null) {
            dialog.dismiss();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        this.finish();
    }

    private void setParameters() {
        if (extras.getString("URI") != null) {
            getLoadedColumns();
        } else {
            loadedDungeon = null;
            loadedRoomDescription = null;
            loadedTrapDescription = null;
            loadedRoamingMonsterDescription = null;
            filter = null;
        }
        String mt = extras.getString(DBOpenHelper.MONSTER_TYPE);
        String th = extras.getString("THEME");
        dungeonDifficulty = setDungeonDifficulty(extras.getString(DBOpenHelper.DUNGEON_DIFFICULTY));
        dungeonSize = setDungeonSize(extras.getString(DBOpenHelper.DUNGEON_SIZE));
        roomDensity = setRoomDensity(extras.getString(DBOpenHelper.ROOM_DENSITY));
        roomSize = setRoomSize(extras.getString(DBOpenHelper.ROOM_SIZE));
        traps = setTraps(extras.getString(DBOpenHelper.TRAPS));
        hasDeadEnds = setBooleans(extras.getString(DBOpenHelper.DEAD_ENDS));
        hasCorridor = setBooleans(extras.getString(DBOpenHelper.CORRIDORS));
        partySize = Integer.parseInt(Objects.requireNonNull(extras.getString(DBOpenHelper.PARTY_SIZE)));
        partyLevel = Integer.parseInt(Objects.requireNonNull(extras.getString(DBOpenHelper.PARTY_LEVEL)));
        treasureValue = setTreasureValue(extras.getString(DBOpenHelper.TREASURE_VALUE));
        itemsRarity = setItemsRarity(extras.getString(DBOpenHelper.ITEMS_RARITY));
        roamingMonsters = setRoamingMonsters(extras.getString(DBOpenHelper.ROAMING_MONSTERS));
        if (mt != null && !mt.isEmpty()) {
            monsterType = mt.toLowerCase();
        }
        if (th != null && !th.isEmpty()) {
            theme = th.toLowerCase();
        }
        exported = false;
    }

    private String readJSON(int id) {
        String result;
        try (InputStream is = getResources().openRawResource(id)) {
            Scanner scanner = new Scanner(is);
            StringBuilder sb = new StringBuilder();
            while (scanner.hasNextLine()) {
                sb.append(scanner.nextLine());
            }
            result = sb.toString();
        } catch (Exception ex) {
            Log.e("DungeonActivity", "Read JSON file failed: " + ex);
            return null;
        }
        return result;
    }

    private Boolean setBooleans(String input) {
        return input != null && !input.isEmpty() && Objects.equals(input.toUpperCase(), "YES");
    }

    private int setDungeonDifficulty(String dd) {
        if (dd != null && !dd.isEmpty()) {
            switch (dd) {
                case "Easy":
                    return 0;
                case MEDIUM:
                    return 1;
                case "Hard":
                    return 2;
                case "Deadly":
                    return 3;
                default:
                    break;
            }
        }
        return 0;
    }

    private int setDungeonSize(String ds) {
        if (ds != null && !ds.isEmpty()) {
            switch (ds) {
                case SMALL:
                    return 15;
                case MEDIUM:
                    return 20;
                case LARGE:
                    return 25;
                default:
                    break;
            }
        }
        return 15;
    }

    private int setRoomDensity(String rd) {
        if (rd != null && !rd.isEmpty()) {
            switch (rd) {
                case "Low":
                    return 20;
                case MEDIUM:
                    return 30;
                case "High":
                    return 40;
                default:
                    break;
            }
        }
        return 20;
    }

    private int setRoomSize(String rs) {
        if (rs != null && !rs.isEmpty()) {
            switch (rs) {
                case SMALL:
                    return 20;
                case MEDIUM:
                    return 25;
                case LARGE:
                    return 30;
                default:
                    break;
            }
        }
        return 20;
    }

    private int setTraps(String tr) {
        if (tr != null && !tr.isEmpty()) {
            switch (tr) {
                case "None":
                    return 0;
                case "Few":
                    return 15;
                case "More":
                    return 30;
                default:
                    break;
            }
        }
        return 0;
    }

    private int setRoamingMonsters(String rm) {
        if (rm != null && !rm.isEmpty()) {
            switch (rm) {
                case "None":
                    return 0;
                case "Few":
                    return 10;
                case "More":
                    return 20;
                default:
                    break;
            }
        }
        return 0;
    }

    private double setTreasureValue(String tV) {
        if (tV != null && !tV.isEmpty()) {
            switch (tV) {
                case "Low":
                    return 0.5;
                case "Standard":
                    return 1;
                case "High":
                    return 1.5;
                default:
                    break;
            }
        }
        return 0;
    }

    private int setItemsRarity(String iR) {
        if (iR != null && !iR.isEmpty()) {
            switch (iR) {
                case "Common":
                    return 0;
                case "Uncommon":
                    return 1;
                case "Rare":
                    return 2;
                case "Very Rare":
                    return 3;
                case "Legendary":
                    return 4;
                default:
                    break;
            }
        }
        return 0;
    }

    private void getLoadedColumns() {
        Uri uri = Uri.parse(extras.getString("URI"));
        filter = extras.getString("FILTER");
        Cursor cursor = getContentResolver().query(uri, DBOpenHelper.ALL_COLUMNS, filter, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
            loadedDungeon = gson.fromJson(cursor.getString(cursor.getColumnIndexOrThrow(DBOpenHelper.LOADED_DUNGEON)), DungeonTile[][].class);
            Type roomListType = new TypeToken<ArrayList<RoomDescription>>() {
            }.getType();
            loadedRoomDescription = gson.fromJson(cursor.getString(cursor.getColumnIndexOrThrow(DBOpenHelper.LOADED_ROOM_DESCRIPTION)), roomListType);
            Type trapListType = new TypeToken<ArrayList<TrapDescription>>() {
            }.getType();
            loadedTrapDescription = gson.fromJson(cursor.getString(cursor.getColumnIndexOrThrow(DBOpenHelper.LOADED_TRAP_DESCRIPTION)), trapListType);
            Type monsterListType = new TypeToken<ArrayList<RoamingMonsterDescription>>() {
            }.getType();
            loadedRoamingMonsterDescription = gson.fromJson(cursor.getString(cursor.getColumnIndexOrThrow(DBOpenHelper.LOADED_ROAMING_MONSTERS)), monsterListType);
            cursor.close();
        }
    }

    private void generateDungeon() {
        RelativeLayout layout = findViewById(R.id.dungeon_layout);
        if (findViewById(R.id.dungeonMap_view) != null) {
            layout.removeAllViews();
            exported = false;
        }
        layout.addView(getDungeonView(false));
        addButtons(layout);
        addDescription(layout, dungeonView.getRoomDescription(), dungeonView.getTrapDescription(), dungeonView.getRoamingMonsterDescription());
        addTouchListener();
        filter = null;
    }

    private void addTouchListener() {
        fillLoadedVariables();
        DungeonMapView view = findViewById(R.id.dungeonMap_view);
        view.setOnTouchListener((v, event) -> {
            mGestureDetector.onTouchEvent(event);
            v.performClick();
            return true;
        });
    }

    private void fillLoadedVariables() {
        loadedTrapDescription = dungeonView.getTrapDescription();
        loadedRoomDescription = dungeonView.getRoomDescription();
        loadedRoamingMonsterDescription = dungeonView.getRoamingMonsterDescription();
        loadedDungeon = dungeonView.getDungeonTiles();
    }

    private void CheckMotion(MotionEvent motionEvent) {
        float x = motionEvent.getX();
        float y = motionEvent.getY();
        int imgSize = area / dungeonSize; // get the image size on the dungeon tiles
        int xIndex = ((int) y / imgSize) + 1; // get the dungeonTile 2D array x index
        int yIndex = ((int) x / imgSize) + 1; // get the dungeonTile 2D array y index
        if (xIndex >= loadedDungeon.length || yIndex >= loadedDungeon[0].length) {
            return;
        }
        Textures texture = loadedDungeon[xIndex][yIndex].getTexture();
        if (motionEvent.getAction() == MotionEvent.ACTION_UP) { // single tap
            switch (texture) {
                case ROOM:
                    showRoomPopUp(loadedDungeon[xIndex][yIndex].getIndex());
                    break;
                case TRAP:
                    showTrapPopUp(loadedDungeon[xIndex][yIndex].getIndex());
                    break;
                case DOOR:
                case DOOR_LOCKED:
                case DOOR_TRAPPED:
                case NO_CORRIDOR_DOOR:
                case NO_CORRIDOR_DOOR_LOCKED:
                case NO_CORRIDOR_DOOR_TRAPPED:
                    showDoorPopUp(loadedDungeon[xIndex][yIndex].getDescription());
                    break;
                case ENTRY:
                    showEntryPopUp();
                    break;
                case ROAMING_MONSTER:
                    showRoamingMonsterPopUp(loadedDungeon[xIndex][yIndex].getIndex());
                    break;
                default:
                    break;
            }
        } else if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) { // long tap
            switch (texture) {
                case ROOM:
                case TRAP:
                case ROAMING_MONSTER:
                    editPopUp(loadedDungeon[xIndex][yIndex].getIndex(), texture);
                default:
                    break;
            }
        }
    }

    private void editPopUp(int index, Textures texture) {
        dialog = new Dialog(this, R.style.Dialog);
        EditText description;
        Button saveButton;
        Button cancelButton;
        switch (texture) {
            case ROOM:
                RoomDescription room = loadedRoomDescription.get(index);
                dialog.setContentView(R.layout.room_edit_popup);
                dialog.setTitle(room.getName());
                EditText monsters = dialog.findViewById(R.id.edit_monster);
                monsters.setText(room.getMonster().substring(10)); // Monsters:
                setTextStyle(monsters, false);
                EditText treasures = dialog.findViewById(R.id.edit_treasure);
                treasures.setText(room.getTreasure().substring(11)); // Treasures:
                setTextStyle(treasures, false);
                saveButton = dialog.findViewById(R.id.editDialogSaveButton);
                saveButton.setOnClickListener(v -> {
                    RoomDescription newRoom = new RoomDescription(room.getName(), "Treasures: " + treasures.getText().toString(), "Monsters: " + monsters.getText().toString(), room.getDoors());
                    loadedRoomDescription.set(index, newRoom);
                    drawDungeon(null);
                    dialog.dismiss();
                });
                break;
            case TRAP:
                TrapDescription trap = loadedTrapDescription.get(index);
                dialog.setContentView(R.layout.trap_edit_popup);
                dialog.setTitle(trap.getName());
                description = dialog.findViewById(R.id.edit_trap);
                description.setText(trap.getDescription());
                setTextStyle(description, false);
                saveButton = dialog.findViewById(R.id.editDialogSaveButton);
                saveButton.setOnClickListener(v -> {
                    TrapDescription newTrap = new TrapDescription(trap.getName(), description.getText().toString());
                    loadedTrapDescription.set(index, newTrap);
                    drawDungeon(null);
                    dialog.dismiss();
                });
                break;
            case ROAMING_MONSTER:
                RoamingMonsterDescription monster = loadedRoamingMonsterDescription.get(index);
                dialog.setContentView(R.layout.trap_edit_popup);
                dialog.setTitle(monster.getName());
                description = dialog.findViewById(R.id.edit_trap);
                description.setText(monster.getDescription());
                setTextStyle(description, false);
                saveButton = dialog.findViewById(R.id.editDialogSaveButton);
                saveButton.setOnClickListener(v -> {
                    RoamingMonsterDescription newMonster = new RoamingMonsterDescription(monster.getName(), description.getText().toString());
                    loadedRoamingMonsterDescription.set(index, newMonster);
                    drawDungeon(null);
                    dialog.dismiss();
                });
                break;
            default:
                break;
        }
        cancelButton = dialog.findViewById(R.id.editDialogCancelButton);
        cancelButton.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void showEntryPopUp() {
        createDialog("Dungeon Entry", null);
    }

    private void showDoorPopUp(String description) {
        int start = description.indexOf(':');
        int end = description.indexOf('(');
        TextView details = new TextView(this);
        details.setText(description.substring(end));
        setTextStyle(details, false);
        createDialog(description.substring(start + 1, end), details);
    }

    private void showTrapPopUp(int index) {
        TrapDescription trap = loadedTrapDescription.get(index);
        TextView details = new TextView(this);
        details.setText(trap.getDescription());
        setTextStyle(details, false);
        createDialog(trap.getName(), details);
    }

    private void showRoamingMonsterPopUp(int index) {
        RoamingMonsterDescription monster = loadedRoamingMonsterDescription.get(index);
        TextView details = new TextView(this);
        details.setText(monster.getDescription());
        setTextStyle(details, false);
        createDialog(monster.getName(), details);
    }

    private void showRoomPopUp(int index) {
        RoomDescription room = loadedRoomDescription.get(index);
        TextView details = new TextView(this);
        String text = room.getMonster() + "\n" + room.getTreasure();
        details.setText(text);
        setTextStyle(details, false);
        createDialog(room.getName(), details);
    }

    private void createDialog(String title, View view) {
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        dialog = new Dialog(this, R.style.Dialog);
        dialog.setContentView(R.layout.room_details_popup);
        dialog.setTitle(title);
        if (view != null) {
            dialog.addContentView(view, params);
        }
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(ResourcesCompat.getColor(getResources(), R.color.transparentWhite, null)));
        }
        dialog.show();
    }

    private void saveData() {
        if (filter != null) {
            try {
                updateDungeon();
                Toast.makeText(this, "Dungeon updated", Toast.LENGTH_SHORT).show();
                return;
            } catch (Exception ex) {
                Toast.makeText(this, "Something went wrong during the update", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        try {
            saveCurrentDungeon();
            Toast.makeText(this, "Dungeon saved", Toast.LENGTH_SHORT).show();
        } catch (SQLiteConstraintException ex) {
            Toast.makeText(this, "Dungeon already saved", Toast.LENGTH_SHORT).show();
        }
    }

    private void drawDungeon(Object result) {
        RelativeLayout layout = findViewById(R.id.dungeon_layout);
        layout.removeAllViews(); // remove the loading screen + text view
        layout.setBackgroundColor(Color.WHITE);
        if (result != null) { // not a loaded dungeon
            layout.addView((View) result);
            addButtons(layout);
            addDescription(layout, dungeonView.getRoomDescription(), dungeonView.getTrapDescription(), dungeonView.getRoamingMonsterDescription());
        } else if (loadedDungeon != null) { // its a loaded dungeon
            layout.addView(getDungeonView(true));
            addButtons(layout);
            addDescription(layout, loadedRoomDescription, loadedTrapDescription, loadedRoamingMonsterDescription);
        }
        addTouchListener();
    }

    private void saveCurrentDungeon() {
        Uri uri = getContentResolver().insert(DungeonsProvider.CONTENT_URI, getDungeonContentValues());
        if (uri != null) {
            filter = DBOpenHelper.DUNGEON_ID + "=" + uri.getLastPathSegment();
        }
    }

    private void updateDungeon() {
        getContentResolver().update(DungeonsProvider.CONTENT_URI, getUpdateValues(), filter, null);
    }

    @NonNull
    private ContentValues getUpdateValues() {
        ContentValues values = new ContentValues();
        values.put(DBOpenHelper.LOADED_ROOM_DESCRIPTION, gson.toJson(loadedRoomDescription));
        values.put(DBOpenHelper.LOADED_TRAP_DESCRIPTION, gson.toJson(loadedTrapDescription));
        values.put(DBOpenHelper.LOADED_ROAMING_MONSTERS, gson.toJson(loadedRoamingMonsterDescription));
        return values;
    }

    @NonNull
    private ContentValues getDungeonContentValues() {
        ContentValues values = new ContentValues();
        values.put(DBOpenHelper.DUNGEON_NAME, "Dungeon_" + getFormattedDate());
        values.put(DBOpenHelper.LOADED_DUNGEON, gson.toJson(dungeonView.getDungeonTiles()));
        values.put(DBOpenHelper.LOADED_ROOM_DESCRIPTION, gson.toJson(dungeonView.getRoomDescription()));
        values.put(DBOpenHelper.LOADED_TRAP_DESCRIPTION, gson.toJson(dungeonView.getTrapDescription()));
        values.put(DBOpenHelper.LOADED_ROAMING_MONSTERS, gson.toJson(dungeonView.getRoamingMonsterDescription()));
        values.put(DBOpenHelper.DUNGEON_DIFFICULTY, extras.getString(DBOpenHelper.DUNGEON_DIFFICULTY));
        values.put(DBOpenHelper.PARTY_LEVEL, extras.getString(DBOpenHelper.PARTY_LEVEL));
        values.put(DBOpenHelper.PARTY_SIZE, extras.getString(DBOpenHelper.PARTY_SIZE));
        values.put(DBOpenHelper.TREASURE_VALUE, extras.getString(DBOpenHelper.TREASURE_VALUE));
        values.put(DBOpenHelper.ITEMS_RARITY, extras.getString(DBOpenHelper.ITEMS_RARITY));
        values.put(DBOpenHelper.DUNGEON_SIZE, extras.getString(DBOpenHelper.DUNGEON_SIZE));
        values.put(DBOpenHelper.ROOM_DENSITY, extras.getString(DBOpenHelper.ROOM_DENSITY));
        values.put(DBOpenHelper.ROOM_SIZE, extras.getString(DBOpenHelper.ROOM_SIZE));
        values.put(DBOpenHelper.TRAPS, extras.getString(DBOpenHelper.TRAPS));
        values.put(DBOpenHelper.CORRIDORS, extras.getString(DBOpenHelper.CORRIDORS));
        values.put(DBOpenHelper.MONSTER_TYPE, extras.getString(DBOpenHelper.MONSTER_TYPE));
        values.put(DBOpenHelper.DEAD_ENDS, extras.getString(DBOpenHelper.DEAD_ENDS));
        values.put(DBOpenHelper.ROAMING_MONSTERS, extras.getString(DBOpenHelper.ROAMING_MONSTERS));
        return values;
    }

    private String getFormattedDate() {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd HH:mm:ss", Locale.ENGLISH);
        return dateFormat.format(c.getTime());
    }

    private void addButtons(RelativeLayout layout) {
        addSaveButton(layout);
        addGenerateButton(layout);
        addExportButton(layout);
    }

    private void addExportButton(RelativeLayout layout) {
        Button button = new Button(this);
        button.setText(R.string.export_button_text);
        button.setId(R.id.dungeon_activity_export_button);
        RelativeLayout.LayoutParams relativeParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        relativeParams.addRule(RelativeLayout.BELOW, R.id.dungeon_activity_generate_button);
        button.setLayoutParams(relativeParams);
        button.setOnClickListener(v -> {
            if (checkMassClick()) {
                return;
            }
            export(this);
        });
        buttonStyle(button);
        layout.addView(button);
    }

    private void addGenerateButton(RelativeLayout layout) {
        Button button = new Button(this);
        button.setText(R.string.generate_button_text);
        RelativeLayout.LayoutParams relativeParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        relativeParams.addRule(RelativeLayout.BELOW, R.id.dungeon_activity_save_button);
        button.setLayoutParams(relativeParams);
        button.setOnClickListener(v -> {
            if (checkMassClick()) {
                return;
            }
            generateDungeon();
        });
        button.setId(R.id.dungeon_activity_generate_button);
        buttonStyle(button);
        layout.addView(button);
    }

    private void addSaveButton(RelativeLayout layout) {
        Button button = new Button(this);
        button.setText(R.string.save_button_text);
        RelativeLayout.LayoutParams relativeParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        relativeParams.addRule(RelativeLayout.BELOW, R.id.dungeonMap_view);
        button.setLayoutParams(relativeParams);
        button.setOnClickListener(v -> {
            if (checkMassClick()) {
                return;
            }
            saveData();
        });
        button.setId(R.id.dungeon_activity_save_button);
        buttonStyle(button);
        layout.addView(button);
    }

    private boolean checkMassClick() {
        if (SystemClock.elapsedRealtime() - mLastClickTime < 800) {
            return true;
        }
        mLastClickTime = SystemClock.elapsedRealtime();
        return false;
    }

    private void buttonStyle(Button button) {
        button.setTextColor(Color.WHITE);
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.RECTANGLE);
        if (Build.VERSION.SDK_INT < 23) {
            drawable.setColor(getResources().getColor(R.color.primaryAccent));
            drawable.setStroke(4, getResources().getColor(R.color.primaryDivider));
        } else {
            drawable.setColor(getResources().getColor(R.color.primaryAccent, null));
            drawable.setStroke(4, getResources().getColor(R.color.primaryDivider, null));
        }
        button.setBackground(drawable);
    }

    private void export(DungeonActivity self) {
        if (exported) {
            Toast.makeText(this, "You are already exported this dungeon", Toast.LENGTH_SHORT).show();
            return;
        }
        if (isExternalStorageWritableAndHasSpace()) {
            self.popUpForExport();
        }
    }

    private void popUpForExport() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/html");
        intent.putExtra(Intent.EXTRA_TITLE, "dungeon.html");
        exportActivityResultLauncher.launch(intent);
    }

    private void saveDungeonMap(Intent data) {
        Uri uri = data.getData();
        Bitmap dungeonBitmap = getBitmapFromView(findViewById(R.id.dungeonMap_view));
        String html = Export.generateHTML(dungeonBitmap, dungeonView.getRoomDescription(), dungeonView.getTrapDescription(), dungeonView.getRoamingMonsterDescription());
        writeDocument(uri, html);
        exported = true;
    }

    private void writeDocument(Uri uri, String html) {
        try {
            ParcelFileDescriptor pfd = this.getApplicationContext().getContentResolver().
                    openFileDescriptor(uri, "w");
            if (pfd != null) {
                FileOutputStream fileOutputStream = new FileOutputStream(pfd.getFileDescriptor());
                fileOutputStream.write(html.getBytes());
                fileOutputStream.close();
                pfd.close();
            }
        } catch (IOException e) {
            Log.e("DungeonActivity", "File write failed: " + e);
        }
    }

    private Bitmap getBitmapFromView(View view) {
        //Define a bitmap with the same size as the view
        Bitmap returnedBitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        //Bind a canvas to it
        Canvas canvas = new Canvas(returnedBitmap);
        //Get the view's background
        Drawable bgDrawable = view.getBackground();
        if (bgDrawable != null)
            //has background drawable, then draw it on the canvas
            bgDrawable.draw(canvas);
        else
            //does not have background drawable, then draw white background on the canvas
            canvas.drawColor(Color.WHITE);
        // draw the view on the canvas
        view.draw(canvas);
        //return the bitmap
        return returnedBitmap;
    }

    private boolean isExternalStorageWritableAndHasSpace() {
        String state = Environment.getExternalStorageState();
        long space = Environment.getExternalStorageDirectory().getFreeSpace() / 1024 / 1024;
        return Environment.MEDIA_MOUNTED.equals(state) && space > 1;
    }

    private DungeonMapView getDungeonView(Boolean load) {
        dungeonView = new DungeonMapView(this);
        setDungeonParameters();
        if (load) {
            dungeonView.loadDungeon(loadedDungeon, loadedRoomDescription, loadedTrapDescription, hasCorridor, loadedRoamingMonsterDescription);
        } else {
            dungeonView.generateDungeon();
        }
        return dungeonView;
    }

    private void setDungeonParameters() {
        dungeonView.setDungeonHeight(area);
        dungeonView.setDungeonWidth(area);
        dungeonView.setDungeonSize(dungeonSize);
        dungeonView.setHasCorridor(hasCorridor);
        dungeonView.setJsonMonster(jsonMonster);
        dungeonView.setJsonTreasure(jsonTreasure);
        dungeonView.setRoomDensity(roomDensity);
        dungeonView.setRoomSizePercent(roomSize);
        dungeonView.setTrapPercent(traps);
        dungeonView.setRoamingPercent(roamingMonsters);
        dungeonView.setPartyLevel(partyLevel);
        dungeonView.setItemsRarity(itemsRarity);
        dungeonView.setTreasureValue(treasureValue);
        dungeonView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        dungeonView.setDungeonDifficulty(dungeonDifficulty);
        dungeonView.setPartySize(partySize);
        dungeonView.setMonsterType(monsterType);
        dungeonView.setHasDeadEnds(hasDeadEnds);
        dungeonView.setTheme(theme);
        dungeonView.setId(R.id.dungeonMap_view);
    }

    private void addDescription(RelativeLayout layout, List<RoomDescription> roomDescription, List<TrapDescription> trapDescription, List<RoamingMonsterDescription> roamingMonsterDescription) {
        List<TextView> rooms = getRoomTextViews(layout, roomDescription);
        for (int i = 4; i < rooms.size(); i += 4) { // 4 because the first 4 manually added
            addViewToLayout(layout, rooms.get(i), rooms.get(i - 1), true);
            addViewToLayout(layout, rooms.get(i + 1), rooms.get(i), false);
            addViewToLayout(layout, rooms.get(i + 2), rooms.get(i + 1), false);
            addViewToLayout(layout, rooms.get(i + 3), rooms.get(i + 2), false);
        }
        if (trapDescription != null && !trapDescription.isEmpty()) {
            List<TextView> trapsD = getTrapTextViews(trapDescription);
            addViewToLayout(layout, trapsD.get(0), rooms.get(rooms.size() - 1), true);
            addViewToLayout(layout, trapsD.get(1), trapsD.get(0), false);
            for (int i = 2; i < trapsD.size(); i += 2) { // 2 because the first 2 manually added
                addViewToLayout(layout, trapsD.get(i), trapsD.get(i - 1), true);
                addViewToLayout(layout, trapsD.get(i + 1), trapsD.get(i), false);
            }
            addMonstersDescription(layout, roamingMonsterDescription, trapsD);
        } else {
            addMonstersDescription(layout, roamingMonsterDescription, rooms);
        }
    }

    private void addMonstersDescription(RelativeLayout layout, List<RoamingMonsterDescription> roamingMonsterDescription, List<TextView> parent) {
        if (roamingMonsterDescription != null && !roamingMonsterDescription.isEmpty()) {
            List<TextView> roamingD = getMonsterTextViews(roamingMonsterDescription);
            addViewToLayout(layout, roamingD.get(0), parent.get(parent.size() - 1), true);
            addViewToLayout(layout, roamingD.get(1), roamingD.get(0), false);
            for (int i = 2; i < roamingD.size(); i += 2) { // 2 because the first 2 manually added
                addViewToLayout(layout, roamingD.get(i), roamingD.get(i - 1), true);
                addViewToLayout(layout, roamingD.get(i + 1), roamingD.get(i), false);
            }
        }
    }

    @NonNull
    private List<TextView> getMonsterTextViews(List<RoamingMonsterDescription> roamingMonsterDescription) {
        List<TextView> result = new ArrayList<>();
        for (RoamingMonsterDescription monster : roamingMonsterDescription) { // generate monster TextViews
            addToTextViewList(result, monster.getName(), monster.getDescription());
        }
        return result;
    }

    @NonNull
    private List<TextView> getTrapTextViews(List<TrapDescription> trapDescription) {
        List<TextView> result = new ArrayList<>();
        for (TrapDescription trap : trapDescription) { // generate traps TextViews
            addToTextViewList(result, trap.getName(), trap.getDescription().replace("\n", "")); // cleanup if it's an old saved dungeon
        }
        return result;
    }

    private void addToTextViewList(List<TextView> list, String name, String description) {
        TextView itemName = new TextView(this);
        TextView itemDescription = new TextView(this);
        itemName.setText(Html.fromHtml("<b>" + name + "</b>"));
        itemName.setId(View.generateViewId());
        itemDescription.setText(description);
        itemDescription.setId(View.generateViewId());
        setTextStyle(itemName, true);
        setTextStyle(itemDescription, false);
        list.add(itemName);
        list.add(itemDescription);
    }

    @NonNull
    private List<TextView> getRoomTextViews(RelativeLayout layout, List<RoomDescription> roomDescription) {
        List<TextView> rooms = new ArrayList<>();
        for (RoomDescription room : roomDescription) { // generate rooms TextViews
            TextView roomName = new TextView(this);
            TextView monster = new TextView(this);
            TextView treasure = new TextView(this);
            TextView doors = new TextView(this);
            roomName.setText(Html.fromHtml("<b>" + room.getName() + "</b>"));
            roomName.setId(View.generateViewId());
            setRoomTexts(room.getMonster(), monster);
            monster.setId(View.generateViewId());
            setRoomTexts(room.getTreasure(), treasure);
            treasure.setId(View.generateViewId());
            setRoomTexts(room.getDoors(), doors);
            doors.setId(View.generateViewId());
            setTextStyle(roomName, true);
            setTextStyle(monster, false);
            setTextStyle(treasure, false);
            setTextStyle(doors, false);
            rooms.add(roomName);
            rooms.add(monster);
            rooms.add(treasure);
            rooms.add(doors);
        }
        addViewToLayout(layout, rooms.get(0), findViewById(R.id.dungeon_activity_export_button), true);
        addViewToLayout(layout, rooms.get(1), rooms.get(0), false);
        addViewToLayout(layout, rooms.get(2), rooms.get(1), false);
        addViewToLayout(layout, rooms.get(3), rooms.get(2), false);
        return rooms;
    }

    private void setRoomTexts(String text, TextView view) {
        text = text.replaceAll(",\n", ""); // cleanup if it's an old save
        text = text.replaceAll("\n", "<br>");
        if (text.contains("Entry #") && !text.contains("Monsters") && !text.contains("Treasures")) {
            text = text.replaceAll("West Entry #", "<b>West Entry #");
            text = text.replaceAll("South Entry #", "<b>South Entry #");
            text = text.replaceAll("East Entry #", "<b>East Entry #");
            text = text.replaceAll("North Entry #", "<b>North Entry #");
            text = text.replaceAll(":", "</b>:");
        } else {
            text = "<b>" + text;
            int index = text.indexOf(":");
            text = text.substring(0, index) + "</b>" + text.substring(index);
        }
        view.setText(Html.fromHtml(text));
    }

    private void setTextStyle(TextView textView, boolean stripe) {
        if (Build.VERSION.SDK_INT < 23) {
            textView.setTextColor(getResources().getColor(R.color.primaryText));
            if (stripe)
                textView.setBackgroundColor(getResources().getColor(R.color.striped));
        } else {
            textView.setTextColor(getResources().getColor(R.color.primaryText, null));
            if (stripe) {
                textView.setBackgroundColor(getResources().getColor(R.color.striped, null));
            }
        }
    }

    private void addViewToLayout(RelativeLayout relativeLayout, View view, View parent, boolean root) {
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        params.addRule(RelativeLayout.BELOW, parent.getId());
        if (root) {
            view.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        } else {
            params.addRule(RelativeLayout.ALIGN_PARENT_START);
        }
        relativeLayout.addView(view, params);
    }

    class DungeonActivityGestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onDown(@NonNull MotionEvent motionEvent) {
            return true;
        }

        @Override
        public boolean onSingleTapUp(@NonNull MotionEvent motionEvent) {
            CheckMotion(motionEvent);
            return true;
        }

        @Override
        public void onLongPress(@NonNull MotionEvent motionEvent) {
            CheckMotion(motionEvent);
            super.onLongPress(motionEvent);
        }
    }

}
