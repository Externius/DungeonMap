package externius.rdmg.activities;

import android.Manifest;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.pm.PackageManager;
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
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
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

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
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
import externius.rdmg.models.RoomDescription;
import externius.rdmg.models.Textures;
import externius.rdmg.models.TrapDescription;
import externius.rdmg.views.DungeonMapView;

public class DungeonActivity extends AppCompatActivity {
    private static final int REQUEST_WRITE_EXTERNAL_STORAGE = 1;
    private static boolean exported = false;
    private static final String SMALL = "Small";
    private static final String MEDIUM = "Medium";
    private static final String LARGE = "Large";
    private static int dungeonDifficulty;
    private static int partyLevel;
    private static int partySize;
    private static int dungeonSize;
    private static int roomDensity;
    private static int roomSize;
    private static int traps;
    private static boolean hasCorridor;
    private static boolean hasDeadEnds;
    private static String monsterType;
    private static DungeonMapView dungeonView;
    private static String jsonMonster;
    private static String jsonTreasure;
    private static String theme;
    private static String filter;
    private static double treasureValue;
    private static int itemsRarity;
    private static WeakReference<DungeonActivity> activity;
    private static CreateDungeon createDungeonTask = null;
    private static DungeonTile[][] loadedDungeon;
    private static List<RoomDescription> loadedRoomDescription;
    private static List<TrapDescription> loadedTrapDescription;
    private static Bundle extras = null;
    private static final Gson gson = new Gson();
    private static long mLastClickTime = 0;
    private static int area;
    private static Dialog dialog;
    private static GestureDetector mGestureDetector;
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
        createDungeonTask = new CreateDungeon(this);
        createDungeonTask.execute();
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
        if (size.x > size.y) {
            area = size.y;
        } else {
            area = size.x;
        }
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
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        if (createDungeonTask != null) {
            createDungeonTask.cancel(true);
        }
        this.finish();
    }

    static class CreateDungeon extends AsyncTask<Void, Void, Object> {

        CreateDungeon(DungeonActivity activity) {
            DungeonActivity.activity = new WeakReference<>(activity);
        }

        @Override
        protected void onPreExecute() {
            extras = activity.get().getIntent().getExtras(); // get extra parameters
            setParameters(); //set parameters from parent activity
        }

        @Override
        protected Object doInBackground(Void... args) {
            jsonMonster = readJSON(R.raw.monsters);
            jsonTreasure = readJSON(R.raw.treasures);
            return loadedDungeon == null ? getDungeonView(false) : null;
        }

        @Override
        protected void onPostExecute(Object result) {
            drawDungeon(result);
        }

        private static String readJSON(int id) {
            if (activity.get() == null) {
                return null;
            }
            String result;
            try (InputStream is = activity.get().getResources().openRawResource(id)) {
                Scanner scanner = new Scanner(is);
                StringBuilder sb = new StringBuilder();
                while (scanner.hasNextLine()) {
                    sb.append(scanner.nextLine());
                }
                result = sb.toString();
            } catch (Exception ex) {
                Log.e("DungeonActivity", "Read JSON file failed: " + ex.toString());
                return null;
            }
            return result;
        }

        private static Boolean setBooleans(String input) {
            return input != null && !input.isEmpty() && Objects.equals(input.toUpperCase(), "YES");
        }

        private static int setDungeonDifficulty(String dd) {
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

        private static int setDungeonSize(String ds) {
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

        private static int setRoomDensity(String rd) {
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

        private static int setRoomSize(String rs) {
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

        private static int setTraps(String tr) {
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

        private static double setTreasureValue(String tV) {
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

        private static int setItemsRarity(String iR) {
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

        private static void setParameters() {
            if (extras.getString("URI") != null) {
                getLoadedColumns();
            } else {
                loadedDungeon = null;
                loadedRoomDescription = null;
                loadedTrapDescription = null;
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
            partySize = Integer.parseInt(extras.getString(DBOpenHelper.PARTY_SIZE));
            partyLevel = Integer.parseInt(extras.getString(DBOpenHelper.PARTY_LEVEL));
            treasureValue = setTreasureValue(extras.getString(DBOpenHelper.TREASURE_VALUE));
            itemsRarity = setItemsRarity(extras.getString(DBOpenHelper.ITEMS_RARITY));
            if (mt != null && !mt.isEmpty()) {
                monsterType = mt.toLowerCase();
            }
            if (th != null && !th.isEmpty()) {
                theme = th.toLowerCase();
            }
            exported = false;
        }

    }

    private static void getLoadedColumns() {
        Uri uri = Uri.parse(extras.getString("URI"));
        filter = extras.getString("FILTER");
        Cursor cursor = activity.get().getContentResolver().query(uri, DBOpenHelper.ALL_COLUMNS, filter, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
            loadedDungeon = gson.fromJson(cursor.getString(cursor.getColumnIndex(DBOpenHelper.LOADED_DUNGEON)), DungeonTile[][].class);
            Type roomListType = new TypeToken<ArrayList<RoomDescription>>() {
            }.getType();
            loadedRoomDescription = gson.fromJson(cursor.getString(cursor.getColumnIndex(DBOpenHelper.LOADED_ROOM_DESCRIPTION)), roomListType);
            Type trapListType = new TypeToken<ArrayList<TrapDescription>>() {
            }.getType();
            loadedTrapDescription = gson.fromJson(cursor.getString(cursor.getColumnIndex(DBOpenHelper.LOADED_TRAP_DESCRIPTION)), trapListType);
            cursor.close();
        }
    }

    private static void generateDungeon() {
        RelativeLayout layout = activity.get().findViewById(R.id.dungeon_layout);
        if (activity.get().findViewById(R.id.dungeonMap_view) != null) {
            layout.removeAllViews();
            exported = false;
        }
        layout.addView(getDungeonView(false));
        addButtons(layout);
        addDescription(layout, dungeonView.getRoomDescription(), dungeonView.getTrapDescription());
        addTouchListener();
        filter = null;
    }

    private static void addTouchListener() {
        loadedTrapDescription = dungeonView.getTrapDescription();
        loadedRoomDescription = dungeonView.getRoomDescription();
        loadedDungeon = dungeonView.getDungeonTiles();
        DungeonMapView view = activity.get().findViewById(R.id.dungeonMap_view);
        view.setOnTouchListener((v, event) -> {
            mGestureDetector.onTouchEvent(event);
            v.performClick();
            return true;
        });
    }

    class DungeonActivityGestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onDown(MotionEvent motionEvent) {
            return true;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent motionEvent) {
            CheckMotion(motionEvent);
            return true;
        }

        @Override
        public void onLongPress(MotionEvent motionEvent) {
            CheckMotion(motionEvent);
            super.onLongPress(motionEvent);
        }
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
        if (motionEvent.getAction() == MotionEvent.ACTION_UP) { // single tap
            Textures texture = loadedDungeon[xIndex][yIndex].getTexture();
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
                default:
                    break;
            }
        } else if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) { // long tap
            Textures texture = loadedDungeon[xIndex][yIndex].getTexture();
            switch (texture) {
                case ROOM:
                    editRoomPopUp(loadedDungeon[xIndex][yIndex].getIndex());
                    break;
                case TRAP:
                    editTrapPopUp(loadedDungeon[xIndex][yIndex].getIndex());
                    break;
                default:
                    break;
            }
        }
    }

    private static void editTrapPopUp(int index) {
        TrapDescription trap = loadedTrapDescription.get(index);
        dialog = new Dialog(activity.get(), R.style.Dialog);
        dialog.setContentView(R.layout.trap_edit_popup);
        dialog.setTitle(trap.getName());
        EditText description = dialog.findViewById(R.id.edit_trap);
        description.setText(trap.getDescription());
        setTextStyle(description);
        Button saveButton = dialog.findViewById(R.id.editDialogSaveButton);
        saveButton.setOnClickListener(v -> {
            TrapDescription newTrap = new TrapDescription(trap.getName(), description.getText().toString());
            loadedTrapDescription.set(index, newTrap);
            drawDungeon(null);
            dialog.dismiss();
        });
        Button cancelButton = dialog.findViewById(R.id.editDialogCancelButton);
        cancelButton.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private static void showEntryPopUp() {
        createDialog("Dungeon Entry", null);
    }

    private static void showDoorPopUp(String description) {
        int start = description.indexOf(':');
        int end = description.indexOf('(');
        TextView details = new TextView(activity.get());
        details.setText(description.substring(end));
        setTextStyle(details);
        createDialog(description.substring(start + 1, end), details);
    }

    private static void showTrapPopUp(int index) {
        TrapDescription trap = loadedTrapDescription.get(index);
        TextView details = new TextView(activity.get());
        details.setText(trap.getDescription());
        setTextStyle(details);
        createDialog(trap.getName(), details);
    }

    private static void showRoomPopUp(int index) {
        RoomDescription room = loadedRoomDescription.get(index);
        TextView details = new TextView(activity.get());
        String text = room.getMonster() + "\n" + room.getTreasure();
        details.setText(text);
        setTextStyle(details);
        createDialog(room.getName(), details);
    }

    private static void editRoomPopUp(int index) {
        RoomDescription room = loadedRoomDescription.get(index);
        dialog = new Dialog(activity.get(), R.style.Dialog);
        dialog.setContentView(R.layout.room_edit_popup);
        dialog.setTitle(room.getName());
        EditText monsters = dialog.findViewById(R.id.edit_monster);
        monsters.setText(room.getMonster().substring(9)); // Monster:
        setTextStyle(monsters);
        EditText treasures = dialog.findViewById(R.id.edit_treasure);
        treasures.setText(room.getTreasure().substring(11)); // Treasures:
        setTextStyle(treasures);
        Button saveButton = dialog.findViewById(R.id.editDialogSaveButton);
        saveButton.setOnClickListener(v -> {
            RoomDescription newRoom = new RoomDescription(room.getName(), "Treasures: " + treasures.getText().toString(), "Monster: " + monsters.getText().toString(), room.getDoors());
            loadedRoomDescription.set(index, newRoom);
            drawDungeon(null);
            dialog.dismiss();
        });
        Button cancelButton = dialog.findViewById(R.id.editDialogCancelButton);
        cancelButton.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private static void createDialog(String title, View view) {
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        dialog = new Dialog(activity.get(), R.style.Dialog);
        dialog.setContentView(R.layout.room_details_popup);
        dialog.setTitle(title);
        if (view != null) {
            dialog.addContentView(view, params);
        }
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(ResourcesCompat.getColor(activity.get().getResources(), R.color.transparentWhite, null)));
        }
        dialog.show();
    }

    private static void saveData() {
        if (filter != null) {
            try {
                updateDungeon();
                Toast.makeText(activity.get(), "Dungeon updated", Toast.LENGTH_SHORT).show();
                return;
            } catch (Exception ex) {
                Toast.makeText(activity.get(), "Something went wrong during the update", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        try {
            saveCurrentDungeon();
            Toast.makeText(activity.get(), "Dungeon saved", Toast.LENGTH_SHORT).show();
        } catch (SQLiteConstraintException ex) {
            Toast.makeText(activity.get(), "Dungeon already saved", Toast.LENGTH_SHORT).show();
        }
    }

    private static void drawDungeon(Object result) {
        RelativeLayout layout = activity.get().findViewById(R.id.dungeon_layout);
        layout.removeAllViews(); // remove the loading screen + text view
        layout.setBackgroundColor(Color.WHITE);
        if (result != null) { // not a loaded dungeon
            layout.addView((View) result);
            addButtons(layout);
            addDescription(layout, dungeonView.getRoomDescription(), dungeonView.getTrapDescription());
        } else if (loadedDungeon != null) { // its a loaded dungeon
            layout.addView(getDungeonView(true));
            addButtons(layout);
            addDescription(layout, loadedRoomDescription, loadedTrapDescription);
        }
        addTouchListener();
    }

    private static void saveCurrentDungeon() {
        Uri uri = activity.get().getContentResolver().insert(DungeonsProvider.CONTENT_URI, getDungeonContentValues());
        if (uri != null) {
            filter = DBOpenHelper.DUNGEON_ID + "=" + uri.getLastPathSegment();
        }
    }

    private static void updateDungeon() {
        activity.get().getContentResolver().update(DungeonsProvider.CONTENT_URI, getUpdateValues(), filter, null);
    }

    @NonNull
    private static ContentValues getUpdateValues() {
        ContentValues values = new ContentValues();
        values.put(DBOpenHelper.LOADED_ROOM_DESCRIPTION, gson.toJson(loadedRoomDescription));
        values.put(DBOpenHelper.LOADED_TRAP_DESCRIPTION, gson.toJson(loadedTrapDescription));
        return values;
    }

    @NonNull
    private static ContentValues getDungeonContentValues() {
        ContentValues values = new ContentValues();
        values.put(DBOpenHelper.DUNGEON_NAME, "Dungeon_" + getFormattedDate());
        values.put(DBOpenHelper.LOADED_DUNGEON, gson.toJson(dungeonView.getDungeonTiles()));
        values.put(DBOpenHelper.LOADED_ROOM_DESCRIPTION, gson.toJson(dungeonView.getRoomDescription()));
        values.put(DBOpenHelper.LOADED_TRAP_DESCRIPTION, gson.toJson(dungeonView.getTrapDescription()));
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
        return values;
    }

    private static String getFormattedDate() {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd HH:mm:ss", Locale.ENGLISH);
        return dateFormat.format(c.getTime());
    }

    private static void addButtons(RelativeLayout layout) {
        addSaveButton(layout);
        addGenerateButton(layout);
        addExportButton(layout);
    }

    private static void addExportButton(RelativeLayout layout) {
        Button button = new Button(activity.get());
        button.setText(R.string.export_button_text);
        button.setId(R.id.dungeon_activity_export_button);
        RelativeLayout.LayoutParams relativeParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        relativeParams.addRule(RelativeLayout.BELOW, R.id.dungeon_activity_generate_button);
        button.setLayoutParams(relativeParams);
        button.setOnClickListener(v -> {
            if (checkMassClick()) {
                return;
            }
            checkPermission();
        });
        buttonStyle(button);
        layout.addView(button);
    }

    private static void addGenerateButton(RelativeLayout layout) {
        Button button = new Button(activity.get());
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

    private static void addSaveButton(RelativeLayout layout) {
        Button button = new Button(activity.get());
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

    private static boolean checkMassClick() {
        if (SystemClock.elapsedRealtime() - mLastClickTime < 800) {
            return true;
        }
        mLastClickTime = SystemClock.elapsedRealtime();
        return false;
    }

    private static void buttonStyle(Button button) {
        button.setTextColor(Color.WHITE);
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.RECTANGLE);
        if (Build.VERSION.SDK_INT < 23) {
            drawable.setColor(activity.get().getResources().getColor(R.color.primaryAccent));
            drawable.setStroke(4, activity.get().getResources().getColor(R.color.primaryDivider));
        } else {
            drawable.setColor(activity.get().getResources().getColor(R.color.primaryAccent, null));
            drawable.setStroke(4, activity.get().getResources().getColor(R.color.primaryDivider, null));
        }
        button.setBackground(drawable);
    }

    private static void checkPermission() {
        if (ContextCompat.checkSelfPermission(activity.get(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity.get(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                Toast.makeText(activity.get(), "To save the dungeon, the app needs write permission", Toast.LENGTH_LONG).show();
            } else {
                ActivityCompat.requestPermissions(activity.get(),
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_WRITE_EXTERNAL_STORAGE);
            }
        } else {
            export();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_WRITE_EXTERNAL_STORAGE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            export();
        } else {
            Toast.makeText(DungeonActivity.this, "Write files permission is denied", Toast.LENGTH_LONG).show();
        }
    }

    private static void export() {
        if (exported) {
            Toast.makeText(activity.get(), "You are already exported this dungeon", Toast.LENGTH_SHORT).show();
            return;
        }
        if (isExternalStorageWritableAndHasSpace()) {
            Bitmap dungeonBitmap = getBitmapFromView(activity.get().findViewById(R.id.dungeonMap_view));
            File directory = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
                    + "/DungeonMaps/");
            String html = Export.generateHTML(dungeonBitmap, dungeonView.getRoomDescription(), dungeonView.getTrapDescription());
            if (!directory.exists()) {
                if (directory.mkdirs()) {
                    createFile(directory, getFilename(directory), html);
                }
            } else {
                createFile(directory, getFilename(directory), html);
            }
        }
    }

    private static String getFilename(File directory) {
        File[] files = directory.listFiles((dir, name) -> name.toLowerCase().endsWith(".html"));
        if (files.length > 0) {
            return "dungeon" + files.length + ".html";
        } else {
            return "dungeon.html";
        }
    }

    private static Bitmap getBitmapFromView(View view) {
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

    private static boolean isExternalStorageWritableAndHasSpace() {
        String state = Environment.getExternalStorageState();
        long space = Environment.getExternalStorageDirectory().getFreeSpace() / 1024 / 1024;
        return Environment.MEDIA_MOUNTED.equals(state) && space > 1;
    }

    private static void createFile(File directory, String filename, String html) {
        File file = new File(directory, filename);
        try (FileOutputStream outputStream = new FileOutputStream(file)) {
            outputStream.write(html.getBytes());
        } catch (IOException e) {
            Log.e("DungeonActivity", "File write failed: " + e.toString());
        }
        Toast.makeText(activity.get(), filename + " saved at: " + directory.getPath(), Toast.LENGTH_LONG).show();
        exported = true;
    }

    private static DungeonMapView getDungeonView(Boolean load) {
        if (activity.get() == null) {
            return null;
        }
        dungeonView = new DungeonMapView(activity.get());
        setDungeonParameters();
        if (load) {
            dungeonView.loadDungeon(loadedDungeon, loadedRoomDescription, loadedTrapDescription, hasCorridor);
        } else {
            dungeonView.generateDungeon();
        }
        return dungeonView;
    }

    private static void setDungeonParameters() {
        dungeonView.setDungeonHeight(area);
        dungeonView.setDungeonWidth(area);
        dungeonView.setDungeonSize(dungeonSize);
        dungeonView.setHasCorridor(hasCorridor);
        dungeonView.setJsonMonster(jsonMonster);
        dungeonView.setJsonTreasure(jsonTreasure);
        dungeonView.setRoomDensity(roomDensity);
        dungeonView.setRoomSizePercent(roomSize);
        dungeonView.setTrapPercent(traps);
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

    private static void addDescription(RelativeLayout layout, List<RoomDescription> roomDescription, List<TrapDescription> trapDescription) {
        List<TextView> rooms = getRoomTextViews(layout, roomDescription);
        for (int i = 4; i < rooms.size(); i += 4) { // 4 because the first 4 manually added
            addViewToLayout(layout, rooms.get(i), rooms.get(i - 3), true, rooms.get(i - 1));
            addViewToLayout(layout, rooms.get(i + 1), rooms.get(i - 1), false, rooms.get(i));
            addViewToLayout(layout, rooms.get(i + 2), rooms.get(i + 1), false, rooms.get(i));
            addViewToLayout(layout, rooms.get(i + 3), rooms.get(i + 2), false, rooms.get(i));
        }
        if (!trapDescription.isEmpty()) {
            List<TextView> trapsD = getTrapTextViews(trapDescription);
            addViewToLayout(layout, trapsD.get(0), null, true, rooms.get(rooms.size() - 1));
            addViewToLayout(layout, trapsD.get(1), rooms.get(rooms.size() - 1), false, trapsD.get(0));
            for (int i = 2; i < trapsD.size(); i += 2) { // 2 because the first 2 manually added
                addViewToLayout(layout, trapsD.get(i), null, true, trapsD.get(i - 1));
                addViewToLayout(layout, trapsD.get(i + 1), trapsD.get(i - 1), false, trapsD.get(i));
            }
        }
    }

    @NonNull
    private static List<TextView> getTrapTextViews(List<TrapDescription> trapDescription) {
        List<TextView> trapsD = new ArrayList<>();
        for (TrapDescription trap : trapDescription) { // generate traps TextViews
            TextView trapName = new TextView(activity.get());
            TextView trapDes = new TextView(activity.get());
            trapName.setText(trap.getName());
            trapName.setId(View.generateViewId());
            trapDes.setText(trap.getDescription());
            trapDes.setId(View.generateViewId());
            setTextStyle(trapName);
            setTextStyle(trapDes);
            trapsD.add(trapName);
            trapsD.add(trapDes);
        }
        return trapsD;
    }

    @NonNull
    private static List<TextView> getRoomTextViews(RelativeLayout layout, List<RoomDescription> roomDescription) {
        List<TextView> rooms = new ArrayList<>();
        for (RoomDescription room : roomDescription) { // generate rooms TextViews
            TextView roomName = new TextView(activity.get());
            TextView monster = new TextView(activity.get());
            TextView treasure = new TextView(activity.get());
            TextView doors = new TextView(activity.get());
            roomName.setText(room.getName());
            roomName.setId(View.generateViewId());
            monster.setText(room.getMonster());
            monster.setId(View.generateViewId());
            treasure.setText(room.getTreasure());
            treasure.setId(View.generateViewId());
            doors.setText(room.getDoors());
            doors.setId(View.generateViewId());
            setTextStyle(roomName);
            setTextStyle(monster);
            setTextStyle(treasure);
            setTextStyle(doors);
            rooms.add(roomName);
            rooms.add(monster);
            rooms.add(treasure);
            rooms.add(doors);
        }
        addViewToLayout(layout, rooms.get(0), null, true, activity.get().findViewById(R.id.dungeon_activity_export_button));
        addViewToLayout(layout, rooms.get(1), activity.get().findViewById(R.id.dungeon_activity_export_button), false, rooms.get(0));
        addViewToLayout(layout, rooms.get(2), rooms.get(1), false, rooms.get(0));
        addViewToLayout(layout, rooms.get(3), rooms.get(2), false, rooms.get(0));
        return rooms;
    }

    private static void setTextStyle(TextView textView) {
        if (Build.VERSION.SDK_INT < 23) {
            textView.setTextColor(activity.get().getResources().getColor(R.color.primaryText));
        } else {
            textView.setTextColor(activity.get().getResources().getColor(R.color.primaryText, null));
        }
    }

    private static void addViewToLayout(RelativeLayout relativeLayout, View view, View parent, boolean root, View end) {
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        if (root) {
            params.addRule(RelativeLayout.BELOW, end.getId());
            params.addRule(RelativeLayout.ALIGN_PARENT_START);
        } else {
            params.width = RelativeLayout.LayoutParams.MATCH_PARENT;
            params.addRule(RelativeLayout.END_OF, end.getId());
            params.addRule(RelativeLayout.BELOW, parent.getId());
        }
        relativeLayout.addView(view, params);
    }

}
