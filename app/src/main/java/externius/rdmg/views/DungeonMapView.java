package externius.rdmg.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import externius.rdmg.R;
import externius.rdmg.core.Dungeon;
import externius.rdmg.core.DungeonNoCorridor;
import externius.rdmg.helpers.Utils;
import externius.rdmg.models.DungeonTile;
import externius.rdmg.models.RoamingMonsterDescription;
import externius.rdmg.models.RoomDescription;
import externius.rdmg.models.Textures;
import externius.rdmg.models.TrapDescription;

public class DungeonMapView extends View {
    private Bitmap marble;
    private Bitmap corridor;
    private Bitmap door;
    private Bitmap doorLocked;
    private Bitmap doorTrapped;
    private Bitmap room;
    private Bitmap entry;
    private Bitmap trap;
    private Bitmap roamingMonster;
    private Bitmap ncDoor;
    private Bitmap ncDoorLocked;
    private Bitmap ncDoorTrapped;
    private Bitmap roomEdge;
    private double treasureValue;
    private int itemsRarity;
    private DungeonTile[][] dungeonTiles;
    private final Paint paint = new Paint();
    private List<RoomDescription> roomDescription = new ArrayList<>();
    private List<TrapDescription> trapDescription = new ArrayList<>();
    private List<RoamingMonsterDescription> roamingMonsterDescription = new ArrayList<>();
    private String jsonMonster;
    private String jsonTreasure;
    private int dungeonWidth;
    private int dungeonHeight;
    private int dungeonSize;
    private int roomDensity;
    private int roomSizePercent;
    private int trapPercent;
    private int roamingPercent;
    private int partyLevel;
    private boolean hasCorridor;
    private boolean hasDeadEnds;
    private int dungeonDifficulty;
    private int partySize;
    private String monsterType;
    private String theme;

    public DungeonMapView(Context context) {
        super(context);
    }

    public void loadDungeon(DungeonTile[][] dungeonTiles, List<RoomDescription> roomDescription, List<TrapDescription> trapDescription, boolean hasCorridor, List<RoamingMonsterDescription> roamingMonsterDescription) {
        this.dungeonTiles = dungeonTiles;
        this.roomDescription = roomDescription;
        this.trapDescription = trapDescription;
        this.roamingMonsterDescription = roamingMonsterDescription;
        setBitmaps(hasCorridor);
        setPaint();
    }

    public void generateDungeon() {
        init();
        setPaint();
        setBitmaps(hasCorridor);
        if (hasCorridor) {
            Dungeon dungeon = new Dungeon(dungeonWidth, dungeonHeight, dungeonSize, roomDensity, roomSizePercent, trapPercent, hasDeadEnds, roamingPercent);
            dungeon.generate();
            roomDescription = dungeon.getRoomDescription();
            trapDescription = dungeon.getTrapDescription();
            roamingMonsterDescription = dungeon.getRoamingMonsterDescriptions();
            dungeonTiles = dungeon.getDungeonTiles();
        } else {
            DungeonNoCorridor dungeonNoCorridor = new DungeonNoCorridor(dungeonWidth, dungeonHeight, dungeonSize, roomSizePercent);
            dungeonNoCorridor.generate();
            roomDescription = dungeonNoCorridor.getRoomDescription();
            dungeonTiles = dungeonNoCorridor.getDungeonTiles();
        }
    }

    private void setPaint() {
        paint.setTextSize(getFontSize(dungeonSize));
    }

    private void init() {
        Utils.setPartySize(partySize);
        Utils.setPartyLevel(partyLevel);
        Utils.setDungeonDifficulty(dungeonDifficulty);
        Utils.setMonsterType(monsterType);
        Utils.setJsonMonster(jsonMonster);
        Utils.setJsonTreasure(jsonTreasure);
        Utils.setTreasureValue(treasureValue);
        Utils.setItemsRarity(itemsRarity);
    }

    private void setBitmaps(Boolean hasCorridor) {
        corridor = BitmapFactory.decodeResource(getResources(), R.drawable.corridor_dark);
        door = BitmapFactory.decodeResource(getResources(), R.drawable.door_dark);
        doorLocked = BitmapFactory.decodeResource(getResources(), R.drawable.door_locked_dark);
        doorTrapped = BitmapFactory.decodeResource(getResources(), R.drawable.door_trapped_dark);
        trap = BitmapFactory.decodeResource(getResources(), R.drawable.trap_dark);
        roamingMonster = BitmapFactory.decodeResource(getResources(), R.drawable.monster_dark);
        entry = BitmapFactory.decodeResource(getResources(), R.drawable.entry_dark);
        switch (theme) {
            case "dark":
                setTheme(hasCorridor, R.drawable.marble_dark, R.drawable.room_edge_dark, R.drawable.room_dark, R.drawable.nc_door_dark, R.drawable.nc_door_locked_dark, R.drawable.nc_door_trapped_dark);
                break;
            case "light":
                setTheme(hasCorridor, R.drawable.marble_light, R.drawable.room_edge_light, R.drawable.room_light, R.drawable.nc_door_light, R.drawable.nc_door_locked_light, R.drawable.nc_door_trapped_light);
                break;
            case "minimal":
                setTheme(hasCorridor, R.drawable.marble_white, R.drawable.marble_white, R.drawable.room_white, R.drawable.nc_door_white, R.drawable.nc_door_locked_white, R.drawable.nc_door_trapped_white);
                break;
            default:
                break;
        }
    }

    private void setTheme(Boolean hasCorridor, int marbleID, int roomEdgeID, int roomID, int ncDoorID, int ncDoorLockedID, int ncDoorTrappedID) {
        if (hasCorridor) {
            roomEdge = BitmapFactory.decodeResource(getResources(), marbleID);
        } else {
            roomEdge = BitmapFactory.decodeResource(getResources(), roomEdgeID);
        }
        marble = BitmapFactory.decodeResource(getResources(), marbleID);
        room = BitmapFactory.decodeResource(getResources(), roomID);
        ncDoor = BitmapFactory.decodeResource(getResources(), ncDoorID);
        ncDoorLocked = BitmapFactory.decodeResource(getResources(), ncDoorLockedID);
        ncDoorTrapped = BitmapFactory.decodeResource(getResources(), ncDoorTrappedID);
    }

    private int getFontSize(int dungeonSize) {
        if (dungeonSize <= 15) {
            return getResources().getDimensionPixelSize(R.dimen.drawTextSmallFontSize);
        } else if (dungeonSize <= 20) {
            return getResources().getDimensionPixelSize(R.dimen.drawTextMediumFontSize);
        } else {
            return getResources().getDimensionPixelSize(R.dimen.drawTextLargeFontSize);
        }
    }

    protected void onDraw(Canvas canvas) {
        for (int i = 1; i < dungeonTiles.length - 1; i++) {
            for (int j = 1; j < dungeonTiles.length - 1; j++) {
                switch (dungeonTiles[i][j].getTexture()) {
                    case ROOM_EDGE:
                        canvas.drawBitmap(scaleBitmap(roomEdge, i, j), dungeonTiles[i][j].getX(), dungeonTiles[i][j].getY(), null);
                        break;
                    case MARBLE:
                        canvas.drawBitmap(scaleBitmap(marble, i, j), dungeonTiles[i][j].getX(), dungeonTiles[i][j].getY(), null);
                        break;
                    case CORRIDOR:
                        canvas.drawBitmap(scaleBitmap(corridor, i, j), dungeonTiles[i][j].getX(), dungeonTiles[i][j].getY(), null);
                        break;
                    case DOOR:
                        canvas.drawBitmap(scaleBitmap(rotateBitmap(door, getDegree(i, j)), i, j), dungeonTiles[i][j].getX(), dungeonTiles[i][j].getY(), null);
                        break;
                    case DOOR_LOCKED:
                        canvas.drawBitmap(scaleBitmap(rotateBitmap(doorLocked, getDegree(i, j)), i, j), dungeonTiles[i][j].getX(), dungeonTiles[i][j].getY(), null);
                        break;
                    case DOOR_TRAPPED:
                        canvas.drawBitmap(scaleBitmap(rotateBitmap(doorTrapped, getDegree(i, j)), i, j), dungeonTiles[i][j].getX(), dungeonTiles[i][j].getY(), null);
                        break;
                    case ROOM:
                        canvas.drawBitmap(scaleBitmap(room, i, j), dungeonTiles[i][j].getX(), dungeonTiles[i][j].getY(), null);
                        canvas.drawText(dungeonTiles[i][j].getDescription(), Math.round(dungeonTiles[i][j].getX() + dungeonTiles[i][j].getWidth() * 0.1), Math.round(dungeonTiles[i][j].getY() + dungeonTiles[i][j].getHeight() * 0.65), paint);
                        break;
                    case ENTRY:
                        canvas.drawBitmap(scaleBitmap(entry, i, j), dungeonTiles[i][j].getX(), dungeonTiles[i][j].getY(), null);
                        break;
                    case TRAP:
                        canvas.drawBitmap(scaleBitmap(trap, i, j), dungeonTiles[i][j].getX(), dungeonTiles[i][j].getY(), null);
                        canvas.drawText(dungeonTiles[i][j].getDescription(), Math.round(dungeonTiles[i][j].getX() + dungeonTiles[i][j].getWidth() * 0.1), Math.round(dungeonTiles[i][j].getY() + dungeonTiles[i][j].getHeight() * 0.5), paint);
                        break;
                    case ROAMING_MONSTER:
                        canvas.drawBitmap(scaleBitmap(roamingMonster, i, j), dungeonTiles[i][j].getX(), dungeonTiles[i][j].getY(), null);
                        canvas.drawText(dungeonTiles[i][j].getDescription(), Math.round(dungeonTiles[i][j].getX() + dungeonTiles[i][j].getWidth() * 0.1), Math.round(dungeonTiles[i][j].getY() + dungeonTiles[i][j].getHeight() * 0.4), paint);
                        break;
                    case NO_CORRIDOR_DOOR:
                        canvas.drawBitmap(scaleBitmap(rotateBitmap(ncDoor, getDegree(i, j)), i, j), dungeonTiles[i][j].getX(), dungeonTiles[i][j].getY(), null);
                        break;
                    case NO_CORRIDOR_DOOR_LOCKED:
                        canvas.drawBitmap(scaleBitmap(rotateBitmap(ncDoorLocked, getDegree(i, j)), i, j), dungeonTiles[i][j].getX(), dungeonTiles[i][j].getY(), null);
                        break;
                    case NO_CORRIDOR_DOOR_TRAPPED:
                        canvas.drawBitmap(scaleBitmap(rotateBitmap(ncDoorTrapped, getDegree(i, j)), i, j), dungeonTiles[i][j].getX(), dungeonTiles[i][j].getY(), null);
                        break;
                    default:
                        break;
                }
            }
        }
    }

    private Bitmap scaleBitmap(Bitmap bitmap, int i, int j) {
        return Bitmap.createScaledBitmap(bitmap, dungeonTiles[i][j].getWidth(), dungeonTiles[i][j].getHeight(), false);
    }

    private int getDegree(int i, int j) {
        if (dungeonTiles[i][j - 1].getTexture() == Textures.ROOM) {
            return -90;
        }
        if (dungeonTiles[i][j + 1].getTexture() == Textures.ROOM) {
            return 90;
        }
        if (dungeonTiles[i + 1][j].getTexture() == Textures.ROOM) {
            return 180;
        }
        return 0;
    }

    private static Bitmap rotateBitmap(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int w = MeasureSpec.getSize(widthMeasureSpec);
        setMeasuredDimension(w, w); // because its always in portrait mode
    }

    @Override
    public boolean performClick() {
        super.performClick();
        return true;
    }

    public List<RoomDescription> getRoomDescription() {
        return roomDescription;
    }

    public void setJsonMonster(String jsonMonster) {
        this.jsonMonster = jsonMonster;
    }

    public void setDungeonWidth(int dungeonWidth) {
        this.dungeonWidth = dungeonWidth;
    }

    public void setDungeonHeight(int dungeonHeight) {
        this.dungeonHeight = dungeonHeight;
    }

    public void setDungeonSize(int dungeonSize) {
        this.dungeonSize = dungeonSize;
    }

    public void setRoomDensity(int roomDensity) {
        this.roomDensity = roomDensity;
    }

    public void setRoomSizePercent(int roomSizePercent) {
        this.roomSizePercent = roomSizePercent;
    }

    public void setTrapPercent(int trapPercent) {
        this.trapPercent = trapPercent;
    }

    public void setPartyLevel(int partyLevel) {
        this.partyLevel = partyLevel;
    }

    public void setHasCorridor(boolean hasCorridor) {
        this.hasCorridor = hasCorridor;
    }

    public void setDungeonDifficulty(int dungeonDifficulty) {
        this.dungeonDifficulty = dungeonDifficulty;
    }

    public void setPartySize(int partySize) {
        this.partySize = partySize;
    }

    public List<TrapDescription> getTrapDescription() {
        return trapDescription;
    }

    public void setMonsterType(String monsterType) {
        this.monsterType = monsterType;
    }

    public void setHasDeadEnds(boolean hasDeadEnds) {
        this.hasDeadEnds = hasDeadEnds;
    }

    public void setTreasureValue(double treasureValue) {
        this.treasureValue = treasureValue;
    }

    public void setItemsRarity(int itemsRarity) {
        this.itemsRarity = itemsRarity;
    }

    public void setJsonTreasure(String jsonTreasure) {
        this.jsonTreasure = jsonTreasure;
    }

    public DungeonTile[][] getDungeonTiles() {
        return dungeonTiles;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    public void setRoamingPercent(int roamingPercent) {
        this.roamingPercent = roamingPercent;
    }

    public List<RoamingMonsterDescription> getRoamingMonsterDescription() {
        return roamingMonsterDescription;
    }
}
