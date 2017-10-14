package externius.rdmg.helpers;


import java.util.List;

import externius.rdmg.models.DungeonTile;
import externius.rdmg.models.Textures;

public final class Door {

    private static int wCount;
    private static int sCount;
    private static int eCount;
    private static int nCount;

    private static final String[] doorTypes = {
            "Crystal",
            "Wooden",
            "Stone",
            "Iron",
            "Steel",
            "Mithral",
            "Adamantine"
    };

    private static final int[] doorAC = {
            13, 15, 17, 19, 19, 21, 23
    };

    private static final int[] doorHP = {
            10, 10, 15, 15, 18, 18, 27
    };

    private Door() {

    }

    private static int getDoorDC() {
        switch (Utils.getDungeonDifficulty()) {
            case 0:
                return Utils.getRandomInt(0, 2);
            case 1:
                return Utils.getRandomInt(1, 4);
            case 2:
                return Utils.getRandomInt(1, 6);
            case 3:
                return Utils.getRandomInt(2, 7);
            default:
                return 0;
        }
    }

    private static String getState(Textures texture) {
        switch (texture) {
            case NO_CORRIDOR_DOOR_LOCKED:
            case DOOR_LOCKED:
                return " Locked";
            case NO_CORRIDOR_DOOR_TRAPPED:
            case DOOR_TRAPPED:
                return " Trapped";
            default:
                return " Open";
        }
    }

    private static String getDoorText(Textures texture, int x) {
        if (RoomPosition.isUp()) {
            return "South Entry #" + sCount++ + ": " + doorTypes[x] + getState(texture) + " Door (AC: " + doorAC[x] + ", HP: " + doorHP[x] + ")\n";
        } else if (RoomPosition.isDown()) {
            return "North Entry #" + nCount++ + ": " + doorTypes[x] + getState(texture) + " Door (AC: " + doorAC[x] + ", HP: " + doorHP[x] + ")\n";
        } else if (RoomPosition.isRight()) {
            return "West Entry #" + wCount++ + ": " + doorTypes[x] + getState(texture) + " Door (AC: " + doorAC[x] + ", HP: " + doorHP[x] + ")\n";
        } else {
            return "East Entry #" + eCount++ + ": " + doorTypes[x] + getState(texture) + " Door (AC: " + doorAC[x] + ", HP: " + doorHP[x] + ")\n";
        }
    }

    static String getDoorDescription(DungeonTile[][] dungeon, List<DungeonTile> doorList) {
        StringBuilder sb = new StringBuilder();
        wCount = 1;
        sCount = 1;
        eCount = 1;
        nCount = 1;
        for (DungeonTile door : doorList) {
            RoomPosition.checkRoomPosition(dungeon, door.getI(), door.getJ());
            sb.append(getDoorText(door.getTexture(), getDoorDC()));
        }
        sb.setLength(sb.length() - 1);
        return sb.toString();
    }

    public static String getNCDoor(DungeonTile door) {
        int x = getDoorDC();
        return ": " + doorTypes[x] + getState(door.getTexture()) + " Door (AC: " + doorAC[x] + ", HP: " + doorHP[x] + ")\n";
    }

    public static String getNCDoorDescription(DungeonTile[][] dungeon, List<DungeonTile> closedList) {
        wCount = 1;
        sCount = 1;
        eCount = 1;
        nCount = 1;
        StringBuilder sb = new StringBuilder();
        for (DungeonTile tile : closedList) {
            if (checkNCDoor(dungeon, tile.getI(), tile.getJ() - 1)) {
                sb.append("West Entry #");
                sb.append(wCount++);
                sb.append(dungeon[tile.getI()][tile.getJ() - 1].getRoomCount());
            } else if (checkNCDoor(dungeon, tile.getI(), tile.getJ() + 1)) {
                sb.append("East Entry #");
                sb.append(eCount++);
                sb.append(dungeon[tile.getI()][tile.getJ() + 1].getRoomCount());
            } else if (checkNCDoor(dungeon, tile.getI() + 1, tile.getJ())) {
                sb.append("South Entry #");
                sb.append(sCount++);
                sb.append(dungeon[tile.getI() + 1][tile.getJ()].getRoomCount());
            } else if (checkNCDoor(dungeon, tile.getI() - 1, tile.getJ())) {
                sb.append("North Entry #");
                sb.append(nCount++);
                sb.append(dungeon[tile.getI() - 1][tile.getJ()].getRoomCount());
            }
        }
        sb.setLength(sb.length() - 1);
        return sb.toString();
    }

    public static boolean checkNCDoor(DungeonTile[][] dungeon, int x, int y) {
        return dungeon[x][y].getTexture() == Textures.NO_CORRIDOR_DOOR || dungeon[x][y].getTexture() == Textures.NO_CORRIDOR_DOOR_LOCKED || dungeon[x][y].getTexture() == Textures.NO_CORRIDOR_DOOR_TRAPPED;
    }

}
