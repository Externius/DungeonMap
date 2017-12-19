package externius.rdmg;


import android.util.Log;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import externius.rdmg.activities.MainActivity;
import externius.rdmg.helpers.Treasure;
import externius.rdmg.helpers.Utils;
import externius.rdmg.models.DungeonTile;
import externius.rdmg.models.RoomDescription;
import externius.rdmg.models.Textures;
import externius.rdmg.models.TrapDescription;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class UtilsTest {
    @Rule
    public final TestName name = new TestName();

    @Before
    public void setup() {
        Utils.setDungeonDifficulty(1);
        Utils.setPartySize(4);
        Utils.setPartyLevel(1);
        Utils.setJsonMonster(readJSON("monsters.json"));
        Utils.setJsonTreasure(UtilsTest.readJSON("treasures.json"));
        Utils.setMonsterType("any");
        Treasure.setTreasureValue(1);
        Treasure.setItemsRarity(3);
    }

    @Test
    public void testManhattan() {
        int x = Utils.manhattan(2, 5);
        assertEquals(name.getMethodName() + " not adding correctly", 7, x);
    }

    @Test
    public void testGetRandomInt() {
        for (int i = 0; i < 100; i++) {
            int x = Utils.getRandomInt(2, 9);
            assertNotEquals(name.getMethodName() + " returns lower value then expected", 1, x);
            assertNotEquals(name.getMethodName() + " returns the max value", 9, x);
        }
        int y = Utils.getRandomInt(2, 2);
        assertEquals(name.getMethodName() + " not working correctly if the 2 parameters equals", 2, y);
    }

    @Test
    public void testAddTrapDescription() {
        DungeonTile[][] dungeon = getTiles();
        List<TrapDescription> trapDescription = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            Utils.addTrapDescription(dungeon, i + 1, i + 1, trapDescription);
        }
        assertEquals(name.getMethodName() + " size isn't right", 3, trapDescription.size());
    }

    @Test
    public void testAddRoomDescription() {
        DungeonTile[][] dungeon = getTiles();
        List<RoomDescription> roomDescription = new ArrayList<>();
        List<DungeonTile> doors = new ArrayList<>();
        doors.add(new DungeonTile(3, 3, 50, 50, 50, 50));
        dungeon[3][3].setTexture(Textures.DOOR);
        dungeon[3][4].setTexture(Textures.ROOM);
        for (int i = 0; i < 3; i++) {
            Utils.addRoomDescription(dungeon, i + 1, i + 1, roomDescription, doors);
        }
        assertEquals(name.getMethodName() + " size isn't right", 3, roomDescription.size());
    }

    @Test
    public void testAddNCRoomDescription() {
        DungeonTile[][] dungeon = getTiles();
        List<RoomDescription> roomDescription = new ArrayList<>();
        Utils.addNCRoomDescription(dungeon, 5, 5, roomDescription, "Example door text");
        assertEquals(name.getMethodName() + " size isn't right", 1, roomDescription.size());
    }

    private static DungeonTile[][] getTiles() {
        DungeonTile[][] dungeon;
        int max = 22;
        dungeon = new DungeonTile[max][max];
        int imgSizeX = 20;
        int imgSizeY = 20;
        for (int i = 0; i < max; i++) {
            for (int j = 0; j < max; j++) {
                dungeon[i][j] = new DungeonTile(i, j);
            }
        }
        for (int i = 1; i < max - 1; i++) { // set drawing area
            for (int j = 1; j < max - 1; j++) {
                dungeon[i][j] = new DungeonTile(i, j, (j - 1) * imgSizeX, (i - 1) * imgSizeY, imgSizeX, imgSizeY);
            }
        }
        return dungeon;
    }

    static String readJSON(String resource) {
        String result;
        try (InputStream is = MainActivity.class.getClassLoader().getResourceAsStream(resource)) {
            Scanner scanner = new Scanner(is);
            StringBuilder sb = new StringBuilder();
            while (scanner.hasNextLine()) {
                sb.append(scanner.nextLine());
            }
            result = sb.toString();
        } catch (Exception ex) {
            Log.e("readJSON", ex.getMessage());
            return null;
        }
        return result;
    }

    static <T> List<T> twoDArrayToList(T[][] twoDArray) {
        List<T> list = new ArrayList<>();
        for (T[] array : twoDArray) {
            list.addAll(Arrays.asList(array));
        }
        return list;
    }
}
