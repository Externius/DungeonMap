package externius.rdmg.helpers;


import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import externius.rdmg.models.DungeonTile;
import externius.rdmg.models.Monster;
import externius.rdmg.models.RoamingMonsterDescription;
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
    private static final Type monsterListType = new TypeToken<ArrayList<Monster>>() {
    }.getType();
    private static final Type treasureListType = new TypeToken<ArrayList<Treasures>>() {
    }.getType();

    private Utils() {

    }

    static List<Monster> getMonsterList() {
        return monsterList;
    }

    static List<Treasures> getTreasureList() {
        return treasureList;
    }

    static int getTreasurePercentage() {
        return switch (dungeonDifficulty) {
            case 0 -> getRandomInt(20, 71);
            case 1 -> getRandomInt(30, 81);
            case 2 -> getRandomInt(40, 91);
            case 3 -> getRandomInt(50, 101);
            default -> 0;
        };
    }

    static int getMonsterPercentage() {
        return switch (dungeonDifficulty) {
            case 0 -> getRandomInt(40, 81);
            case 1 -> getRandomInt(50, 91);
            case 2 -> getRandomInt(60, 101);
            case 3 -> getRandomInt(70, 101);
            default -> 0;
        };
    }

    public static void addRoamingMonsterDescription(DungeonTile[][] dungeon, int x, int y, List<RoamingMonsterDescription> roamingMonsterDescription) {
        dungeon[x][y].setIndex(roamingMonsterDescription.size());
        roamingMonsterDescription.add(new RoamingMonsterDescription(Encounter.getRoamingName(roamingMonsterDescription.size() + 1), Encounter.getRoamingMonster()));
        dungeon[x][y].setDescription(String.valueOf(roamingMonsterDescription.size()));
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
                if (parse(monster.getChallengeRating()) <= partyLevel + 2
                        && parse(monster.getChallengeRating()) >= Math.floor((float) (partyLevel / 4))) {
                    result.add(monster);
                }
            }
        } else {
            for (Monster monster : monsters) {
                if (parse(monster.getChallengeRating()) <= partyLevel + 2
                        && parse(monster.getChallengeRating()) >= Math.floor((float) (partyLevel / 4))
                        && CheckType(monster.getType())) {
                    result.add(monster);
                }
            }
        }
        return result;
    }

    private static boolean CheckType(String mType) {
        String[] types = monsterType.split(",");
        for (String type : types) {
            if (mType.toLowerCase().equals(type)) {
                return true;
            }
        }
        return false;
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
        monsterList = getMonsters(gson.fromJson(jsonMonster, monsterListType));
    }

    static String getMonsterType() {
        return monsterType;
    }

    public static void setMonsterType(String monsterType) {
        Utils.monsterType = monsterType;
    }

    public static void setJsonTreasure(String jsonTreasure) {
        treasureList = gson.fromJson(jsonTreasure, treasureListType);
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
