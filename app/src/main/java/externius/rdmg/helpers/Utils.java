package externius.rdmg.helpers;


import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import externius.rdmg.models.DungeonTile;
import externius.rdmg.models.Monster;
import externius.rdmg.models.RoomDescription;
import externius.rdmg.models.TrapDescription;
import externius.rdmg.models.Treasures;

public final class Utils {
    private static int partyLevel;
    private static int partySize;
    private static int dungeonDifficulty;
    private static double treasureValue;
    private static int itemsRarity;
    private static String monsterType;
    private static List<Monster> monsterList;
    private static List<Treasures> treasureList;
    private static final Gson gson = new Gson();
    private static final Random random = new Random();

    private Utils() {

    }

    static List<Monster> getMonsterList() {
        return monsterList;
    }

    static List<Treasures> getTreasureList() {
        return treasureList;
    }

    static int getTreasurePercentage() {
        switch (dungeonDifficulty) {
            case 0:
                return getRandomInt(20, 71);
            case 1:
                return getRandomInt(30, 81);
            case 2:
                return getRandomInt(40, 91);
            case 3:
                return getRandomInt(50, 101);
            default:
                return 0;
        }
    }

    static int getMonsterPercentage() {
        switch (dungeonDifficulty) {
            case 0:
                return getRandomInt(40, 81);
            case 1:
                return getRandomInt(50, 91);
            case 2:
                return getRandomInt(60, 101);
            case 3:
                return getRandomInt(70, 101);
            default:
                return 0;
        }
    }

    public static void addTrapDescription(DungeonTile[][] dungeon, int x, int y, List<TrapDescription> trapDescription) {
        dungeon[x][y].setIndex(trapDescription.size());
        trapDescription.add(new TrapDescription(Trap.getTrapName(trapDescription.size() + 1), Trap.getCurrentTrap(false)));
        dungeon[x][y].setDescription(String.valueOf(trapDescription.size()));
    }

    public static void addRoomDescription(DungeonTile[][] dungeon, int x, int y, List<RoomDescription> roomDescription, List<DungeonTile> doorList) {
        dungeon[x][y].setIndex(roomDescription.size());
        roomDescription.add(new RoomDescription(getRoomName(roomDescription.size() + 1), Treasure.getTreasure(), Encounter.getMonster(), Door.getDoorDescription(dungeon, doorList)));
        dungeon[x][y].setDescription(String.valueOf(roomDescription.size()));
    }

    public static void addNCRoomDescription(DungeonTile[][] dungeon, int x, int y, List<RoomDescription> roomDescription, String doors) {
        dungeon[x][y].setIndex(roomDescription.size());
        roomDescription.add(new RoomDescription(getRoomName(roomDescription.size() + 1), Treasure.getTreasure(), Encounter.getMonster(), doors));
        dungeon[x][y].setDescription(String.valueOf(roomDescription.size()));
    }

    public static int manhattan(int dx, int dy) {
        return dx + dy;
    }

    public static int getRandomInt(int min, int max) {
        if (max != min) {
            return random.nextInt(max - min) + min;
        }
        return max;
    }

    private static String getRoomName(int x) {
        return "#ROOM" + x + "#";
    }

    private static List<Monster> getMonsters(List<Monster> monsters) {
        List<Monster> result = new ArrayList<>();
        if (monsterType.equalsIgnoreCase("any")) {
            for (Monster monster : monsters) {
                if (parse(monster.getChallengeRating()) <= partyLevel + 2 && parse(monster.getChallengeRating()) >= Math.floor(partyLevel / 4)) {
                    result.add(monster);
                }
            }
        } else {
            for (Monster monster : monsters) {
                if (parse(monster.getChallengeRating()) <= partyLevel + 2 && parse(monster.getChallengeRating()) >= Math.floor(partyLevel / 4)
                        && monsterType.contains(monster.getType())) {
                    result.add(monster);
                }
            }
        }
        return result;
    }

    private static double parse(String ratio) {
        if (ratio.contains("/")) {
            String[] rat = ratio.split("/");
            return Double.parseDouble(rat[0]) / Double.parseDouble(rat[1]);
        } else {
            return Double.parseDouble(ratio);
        }
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

    public static void setJsonMonster(String jsonMonster) {
        monsterList = getMonsters(Arrays.asList(gson.fromJson(jsonMonster, Monster[].class)));
    }

    static String getMonsterType() {
        return monsterType;
    }

    public static void setMonsterType(String monsterType) {
        Utils.monsterType = monsterType;
    }

    public static void setJsonTreasure(String jsonTreasure) {
        treasureList = Arrays.asList(gson.fromJson(jsonTreasure, Treasures[].class));
    }

    public static void setTreasureValue(double treasureValue) {
        Utils.treasureValue = treasureValue;
    }

    public static void setItemsRarity(int itemsRarity) {
        Utils.itemsRarity = itemsRarity;
    }

    static double getTreasureValue() {
        return treasureValue;
    }

    static int getItemsRarity() {
        return itemsRarity;
    }
}
