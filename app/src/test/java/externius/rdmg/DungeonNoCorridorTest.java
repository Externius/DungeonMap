package externius.rdmg;


import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import java.util.List;

import externius.rdmg.core.DungeonNoCorridor;
import externius.rdmg.helpers.Utils;
import externius.rdmg.models.DungeonTile;
import externius.rdmg.models.Textures;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.assertTrue;

public class DungeonNoCorridorTest {
    private final DungeonNoCorridor dungeonNoCorridor = new DungeonNoCorridor(800, 800, 15, 15);

    @Rule
    public final TestName name = new TestName();

    @Before
    public void setup() {
        Utils.setDungeonDifficulty(1);
        Utils.setPartySize(4);
        Utils.setPartyLevel(1);
        Utils.setMonsterType("any");
        Utils.setTreasureValue(1);
        Utils.setItemsRarity(3);
        Utils.setJsonMonster(UtilsTest.readJSON("monsters.json"));
        Utils.setJsonTreasure(UtilsTest.readJSON("treasures.json"));
        dungeonNoCorridor.init();
    }

    @Test
    public void testInit() {
        System.out.println(name.getMethodName());
        DrawTestDungeon.draw(dungeonNoCorridor.getDungeonTiles());
        assertTrue(dungeonNoCorridor.getRoomDescription().isEmpty());
    }

    @Test
    public void testAddFirstRoom() {
        dungeonNoCorridor.addFirstRoom();
        System.out.println(name.getMethodName());
        DrawTestDungeon.draw(dungeonNoCorridor.getDungeonTiles());
        assertTrue(dungeonNoCorridor.getRoomDescription().isEmpty());
        assertTrue(dungeonNoCorridor.getOpenDoorList().size() == 2);
    }

    @Test
    public void testFillRoomToDoor() {
        dungeonNoCorridor.addFirstRoom();
        dungeonNoCorridor.fillRoomToDoor();
        System.out.println(name.getMethodName());
        DrawTestDungeon.draw(dungeonNoCorridor.getDungeonTiles());
        assertTrue(dungeonNoCorridor.getOpenDoorList().size() == 0);
    }

    @Test
    public void testAddDescription() {
        dungeonNoCorridor.addFirstRoom();
        dungeonNoCorridor.fillRoomToDoor();
        dungeonNoCorridor.addDescription();
        System.out.println(name.getMethodName());
        DrawTestDungeon.draw(dungeonNoCorridor.getDungeonTiles());
        assertThat(dungeonNoCorridor.getRoomDescription(), allOf(hasSize(greaterThan(1)), hasSize(lessThan(30))));
    }

    @Test
    public void testAddEntryPoint() {
        dungeonNoCorridor.addFirstRoom();
        dungeonNoCorridor.fillRoomToDoor();
        dungeonNoCorridor.addDescription();
        dungeonNoCorridor.addEntryPoint();
        System.out.println(name.getMethodName());
        DrawTestDungeon.draw(dungeonNoCorridor.getDungeonTiles());
        List<DungeonTile> dungeonList = UtilsTest.twoDArrayToList(dungeonNoCorridor.getDungeonTiles());
        assertThat(dungeonList, hasItem(Matchers.<DungeonTile>hasProperty("texture", equalTo(Textures.ENTRY))));
    }

}
