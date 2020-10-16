package externius.rdmg;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.junit.runner.RunWith;

import externius.rdmg.activities.MainActivity;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.core.IsAnything.anything;

@RunWith(AndroidJUnit4.class)
public class DungeonTest {

    @Rule
    public final ActivityScenarioRule<MainActivity> mainActivityTestRule = new ActivityScenarioRule<>(MainActivity.class);

    @Rule
    public TestRule watcher = new TestWatcher() {
        @Override
        protected void failed(Throwable e, Description description) {
            MainActivityTest.takeScreenshot(description);
        }
    };

    @Test
    public void testDefault() {
        MainActivityTest.generateDungeon();
        MainActivityTest.checkDungeonUI(this.getClass().getName(), "testDefault");
    }

    @Test
    public void testMediumSize() {
        onView(withId(R.id.dungeon_size)).perform(scrollTo(), click());
        onData(anything()).atPosition(1).perform(click());
        MainActivityTest.generateDungeon();
        MainActivityTest.checkDungeonUI(this.getClass().getName(), "testMediumSize");
    }

    @Test
    public void testLargeSize() {
        onView(withId(R.id.dungeon_size)).perform(scrollTo(), click());
        onData(anything()).atPosition(2).perform(click());
        MainActivityTest.generateDungeon();
        MainActivityTest.checkDungeonUI(this.getClass().getName(), "testLargeSize");
    }

    @Test
    public void testEasyDifficulty() {
        onView(withId(R.id.dungeon_difficulty)).perform(scrollTo(), click());
        onData(anything()).atPosition(0).perform(click());
        MainActivityTest.generateDungeon();
        MainActivityTest.checkDungeonUI(this.getClass().getName(), "testEasyDifficulty");
    }

    @Test
    public void testHardDifficulty() {
        onView(withId(R.id.dungeon_difficulty)).perform(scrollTo(), click());
        onData(anything()).atPosition(2).perform(click());
        MainActivityTest.generateDungeon();
        MainActivityTest.checkDungeonUI(this.getClass().getName(), "testHardDifficulty");
    }

    @Test
    public void testDeadlyDifficulty() {
        onView(withId(R.id.dungeon_difficulty)).perform(scrollTo(), click());
        onData(anything()).atPosition(3).perform(click());
        MainActivityTest.generateDungeon();
        MainActivityTest.checkDungeonUI(this.getClass().getName(), "testDeadlyDifficulty");
    }

    @Test
    public void testPartyLevel() {
        onView(withId(R.id.party_level)).perform(scrollTo(), click());
        onData(anything()).atPosition(2).perform(click());
        MainActivityTest.generateDungeon();
        MainActivityTest.checkDungeonUI(this.getClass().getName(), "testPartyLevel");
    }

    @Test
    public void testPartySize() {
        onView(withId(R.id.party_size)).perform(scrollTo(), click());
        onData(anything()).atPosition(0).perform(click());
        MainActivityTest.generateDungeon();
        MainActivityTest.checkDungeonUI(this.getClass().getName(), "testPartySize");
    }

    @Test
    public void testTreasureValue() {
        onView(withId(R.id.treasure_value)).perform(scrollTo(), click());
        onData(anything()).atPosition(0).perform(click());
        MainActivityTest.generateDungeon();
        MainActivityTest.checkDungeonUI(this.getClass().getName(), "testTreasureValue");
    }

    @Test
    public void testItemsMaxRarity() {
        onView(withId(R.id.items_rarity)).perform(scrollTo(), click());
        onData(anything()).atPosition(0).perform(click());
        MainActivityTest.generateDungeon();
        MainActivityTest.checkDungeonUI(this.getClass().getName(), "testItemsMaxRarity");
    }

    @Test
    public void testRoomDensity() {
        onView(withId(R.id.room_density)).perform(scrollTo(), click());
        onData(anything()).atPosition(1).perform(click());
        MainActivityTest.generateDungeon();
        MainActivityTest.checkDungeonUI(this.getClass().getName(), "testRoomDensity");
    }

    @Test
    public void testRoomSizeMedium() {
        onView(withId(R.id.room_size)).perform(scrollTo(), click());
        onData(anything()).atPosition(1).perform(click());
        MainActivityTest.generateDungeon();
        MainActivityTest.checkDungeonUI(this.getClass().getName(), "testRoomSizeMedium");
    }

    @Test
    public void testRoomSizeLarge() {
        onView(withId(R.id.room_size)).perform(scrollTo(), click());
        onData(anything()).atPosition(2).perform(click());
        MainActivityTest.generateDungeon();
        MainActivityTest.checkDungeonUI(this.getClass().getName(), "testRoomSizeLarge");
    }

    @Test
    public void testMonsterType() {
        onView(withId(R.id.monster_type)).perform(scrollTo(), click());
        onData(anything()).atPosition(3).perform(click());
        onView(Matchers.allOf(withId(R.id.monster_ok), withText("OK"))).perform(click());
        MainActivityTest.generateDungeon();
        MainActivityTest.checkDungeonUI(this.getClass().getName(), "testMonsterType");
    }

    @Test
    public void testWithOutTraps() {
        onView(withId(R.id.traps)).perform(scrollTo(), click());
        onData(anything()).atPosition(0).perform(click());
        MainActivityTest.generateDungeon();
        MainActivityTest.checkDungeonUI(this.getClass().getName(), "testWithOutTraps");
    }

    @Test
    public void testWithRoamingMonsters() {
        onView(withId(R.id.roaming_monsters)).perform(scrollTo(), click());
        onData(anything()).atPosition(1).perform(click());
        MainActivityTest.generateDungeon();
        MainActivityTest.checkDungeonUI(this.getClass().getName(), "testWithRoamingMonsters");
    }

    @Test
    public void testWithoutDeadEnds() {
        onView(withId(R.id.dead_end)).perform(scrollTo(), click());
        onData(anything()).atPosition(1).perform(click());
        MainActivityTest.generateDungeon();
        MainActivityTest.checkDungeonUI(this.getClass().getName(), "testWithoutDeadEnds");
    }

    @Test
    public void testTheme() {
        onView(withId(R.id.theme)).perform(scrollTo(), click());
        onData(anything()).atPosition(1).perform(click());
        MainActivityTest.generateDungeon();
        MainActivityTest.checkDungeonUI(this.getClass().getName(), "testTheme");
    }
}
