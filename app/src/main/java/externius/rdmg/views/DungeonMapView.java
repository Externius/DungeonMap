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
import externius.rdmg.core.*;
import externius.rdmg.helpers.Utils;
import externius.rdmg.models.DungeonTile;
import externius.rdmg.models.RoomDescription;
import externius.rdmg.models.Textures;
import externius.rdmg.models.TrapDescription;

public class DungeonMapView extends View {
    private final Bitmap marble = BitmapFactory.decodeResource(getResources(), R.drawable.marble);
    private final Bitmap corridor = BitmapFactory.decodeResource(getResources(), R.drawable.corridor);
    private final Bitmap door = BitmapFactory.decodeResource(getResources(), R.drawable.door);
    private final Bitmap room = BitmapFactory.decodeResource(getResources(), R.drawable.room);
    private final Bitmap entry = BitmapFactory.decodeResource(getResources(), R.drawable.entry);
    private final Bitmap trap = BitmapFactory.decodeResource(getResources(), R.drawable.trap);
    private final Bitmap ncDoor = BitmapFactory.decodeResource(getResources(), R.drawable.nc_door);
    private Bitmap roomEdge;

    private DungeonTile[][] dungeonTiles;
    private final Paint paint = new Paint();
    private List<RoomDescription> roomDescription = new ArrayList<>();
    private List<TrapDescription> trapDescription = new ArrayList<>();
    private String json;
    private int dungeonWidth;
    private int dungeonHeight;
    private int dungeonSize;
    private int roomDensity;
    private int roomSizePercent;
    private int trapPercent;
    private int partyLevel;
    private boolean hasCorridor;
    private boolean hasDeadEnds;
    private int dungeonDifficulty;
    private int partySize;
    private String monsterType;

    public DungeonMapView(Context context) {
        super(context);
    }

    public void GenerateDungeon() {
        Utils.setPartySize(partySize);
        Utils.setPartyLevel(partyLevel);
        Utils.setDungeonDifficulty(dungeonDifficulty);
        Utils.setMonsterType(monsterType);
        Utils.setJson(json);
        paint.setTextSize(getFontSize(dungeonSize));
        if (hasCorridor) {
            roomEdge = BitmapFactory.decodeResource(getResources(), R.drawable.marble);
            Dungeon dungeon = new Dungeon(dungeonWidth, dungeonHeight, dungeonSize, roomDensity, roomSizePercent, trapPercent, hasDeadEnds);
            dungeon.generate();
            roomDescription = dungeon.getRoomDescription();
            trapDescription = dungeon.getTrapDescription();
            dungeonTiles = dungeon.getDungeonTiles();
        } else {
            roomEdge = BitmapFactory.decodeResource(getResources(), R.drawable.room_edge);
            DungeonNoCorridor dungeonNoCorridor = new DungeonNoCorridor(dungeonWidth, dungeonHeight, dungeonSize, roomSizePercent);
            dungeonNoCorridor.generate();
            roomDescription = dungeonNoCorridor.getRoomDescription();
            dungeonTiles = dungeonNoCorridor.getDungeonTiles();
        }
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
                    case ROOM:
                        canvas.drawBitmap(scaleBitmap(room, i, j), dungeonTiles[i][j].getX(), dungeonTiles[i][j].getY(), null);
                        canvas.drawText(dungeonTiles[i][j].getRoomCount(), Math.round(dungeonTiles[i][j].getX() + dungeonTiles[i][j].getWidth() * 0.1), Math.round(dungeonTiles[i][j].getY() + dungeonTiles[i][j].getHeight() * 0.65), paint);
                        break;
                    case ENTRY:
                        canvas.drawBitmap(scaleBitmap(entry, i, j), dungeonTiles[i][j].getX(), dungeonTiles[i][j].getY(), null);
                        break;
                    case TRAP:
                        canvas.drawBitmap(scaleBitmap(trap, i, j), dungeonTiles[i][j].getX(), dungeonTiles[i][j].getY(), null);
                        canvas.drawText(dungeonTiles[i][j].getRoomCount(), Math.round(dungeonTiles[i][j].getX() + dungeonTiles[i][j].getWidth() * 0.1), Math.round(dungeonTiles[i][j].getY() + dungeonTiles[i][j].getHeight() * 0.50), paint);
                        break;
                    case NO_CORRIDOR_DOOR:
                        canvas.drawBitmap(scaleBitmap(rotateBitmap(ncDoor, getDegree(i, j)), i, j), dungeonTiles[i][j].getX(), dungeonTiles[i][j].getY(), null);
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

    public List<RoomDescription> getRoomDescription() {
        return roomDescription;
    }

    public void setJson(String json) {
        this.json = json;
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
}
