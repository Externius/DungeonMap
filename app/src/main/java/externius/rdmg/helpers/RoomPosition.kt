package externius.rdmg.helpers;


import externius.rdmg.models.DungeonTile;
import externius.rdmg.models.Textures;

public final class RoomPosition {
    private static boolean up;
    private static boolean down;
    private static boolean left;
    private static boolean right;

    private RoomPosition() {

    }

    public static void checkRoomPosition(DungeonTile[][] dungeon, int x, int y) {
        up = false;
        down = false;
        left = false;
        right = false;
        if (dungeon[x][y - 1].getTexture() == Textures.ROOM) { // left
            left = true;
        }
        if (dungeon[x][y + 1].getTexture() == Textures.ROOM) { // right
            right = true;
        }
        if (dungeon[x + 1][y].getTexture() == Textures.ROOM) { // bottom
            down = true;
        }
        if (dungeon[x - 1][y].getTexture() == Textures.ROOM) { // top
            up = true;
        }
    }

    public static boolean isUp() {
        return up;
    }

    public static boolean isDown() {
        return down;
    }

    public static boolean isLeft() {
        return left;
    }

    public static boolean isRight() {
        return right;
    }
}
