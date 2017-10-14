package externius.rdmg.helpers;


import java.util.List;
import java.util.Random;

import externius.rdmg.models.DungeonTile;
import externius.rdmg.models.RoomDescription;
import externius.rdmg.models.TrapDescription;

public final class Utils {
    private static int partyLevel;
    private static int partySize;
    private static int dungeonDifficulty;
    private static String json;
    private static String monsterType;

    private Utils() {

    }

    static int getPercentage() {
        switch (dungeonDifficulty) {
            case 0:
                return Utils.getRandomInt(20, 71);
            case 1:
                return Utils.getRandomInt(30, 81);
            case 2:
                return Utils.getRandomInt(40, 91);
            case 3:
                return Utils.getRandomInt(50, 101);
            default:
                return 0;
        }
    }

    public static void addTrapDescription(DungeonTile[][] dungeon, int x, int y, List<TrapDescription> trapDescription) {
        trapDescription.add(new TrapDescription(Trap.getTrapName(trapDescription.size() + 1), Trap.getCurrentTrap()));
        dungeon[x][y].setRoomCount(String.valueOf(trapDescription.size()));
    }

    public static void addRoomDescription(DungeonTile[][] dungeon, int x, int y, List<RoomDescription> roomDescription, List<DungeonTile> doorList) {
        roomDescription.add(new RoomDescription(getRoomName(roomDescription.size() + 1), Treasure.getTreasure(), Encounter.getMonster(), Door.getDoorDescription(dungeon, doorList)));
        dungeon[x][y].setRoomCount(String.valueOf(roomDescription.size()));
    }

    public static void addNCRoomDescription(DungeonTile[][] dungeon, int x, int y, List<RoomDescription> roomDescription, String doors) {
        roomDescription.add(new RoomDescription(getRoomName(roomDescription.size() + 1), Treasure.getTreasure(), Encounter.getMonster(), doors));
        dungeon[x][y].setRoomCount(String.valueOf(roomDescription.size()));
    }

    public static int manhattan(int dx, int dy) {
        return dx + dy;
    }

    public static int getRandomInt(int min, int max) {
        if (max != min) {
            Random r = new Random();
            return r.nextInt(max - min) + min;
        }
        return max;
    }

    private static String getRoomName(int x) {
        return "#ROOM" + x + "#";
    }

    static int getPartyLevel() {
        return partyLevel;
    }

    public static void setPartyLevel(int partyLevel) {
        Utils.partyLevel = partyLevel;
    }

    static int getPartySize() {
        return partySize;
    }

    public static void setPartySize(int partySize) {
        Utils.partySize = partySize;
    }

    static int getDungeonDifficulty() {
        return dungeonDifficulty;
    }

    public static void setDungeonDifficulty(int dungeonDifficulty) {
        Utils.dungeonDifficulty = dungeonDifficulty;
    }

    static String getJson() {
        return json;
    }

    public static void setJson(String json) {
        Utils.json = json;
    }

    static String getMonsterType() {
        return monsterType;
    }

    public static void setMonsterType(String monsterType) {
        Utils.monsterType = monsterType;
    }
}
