package externius.rdmg

import androidx.test.espresso.Espresso.onData
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import externius.rdmg.activities.MainActivity
import org.hamcrest.Matchers.allOf
import org.hamcrest.core.IsAnything.anything
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DungeonTest {

    @get:Rule
    val mainActivityTestRule = ActivityScenarioRule(MainActivity::class.java)

    @get:Rule
    val watcher: TestRule = object : TestWatcher() {
        override fun failed(e: Throwable?, description: Description) {
            MainActivityTest.takeScreenshot(description)
        }
    }

    @Test
    fun testDefault() {
        MainActivityTest.generateDungeon()
        MainActivityTest.checkDungeonUI(this.javaClass.name, "testDefault")
    }

    @Test
    fun testMediumSize() {
        onView(withId(R.id.dungeon_size)).perform(scrollTo(), click())
        onData(anything()).atPosition(1).perform(click())
        MainActivityTest.generateDungeon()
        MainActivityTest.checkDungeonUI(this.javaClass.name, "testMediumSize")
    }

    @Test
    fun testLargeSize() {
        onView(withId(R.id.dungeon_size)).perform(scrollTo(), click())
        onData(anything()).atPosition(2).perform(click())
        MainActivityTest.generateDungeon()
        MainActivityTest.checkDungeonUI(this.javaClass.name, "testLargeSize")
    }

    @Test
    fun testEasyDifficulty() {
        onView(withId(R.id.dungeon_difficulty)).perform(scrollTo(), click())
        onData(anything()).atPosition(0).perform(click())
        MainActivityTest.generateDungeon()
        MainActivityTest.checkDungeonUI(this.javaClass.name, "testEasyDifficulty")
    }

    @Test
    fun testHardDifficulty() {
        onView(withId(R.id.dungeon_difficulty)).perform(scrollTo(), click())
        onData(anything()).atPosition(2).perform(click())
        MainActivityTest.generateDungeon()
        MainActivityTest.checkDungeonUI(this.javaClass.name, "testHardDifficulty")
    }

    @Test
    fun testDeadlyDifficulty() {
        onView(withId(R.id.dungeon_difficulty)).perform(scrollTo(), click())
        onData(anything()).atPosition(3).perform(click())
        MainActivityTest.generateDungeon()
        MainActivityTest.checkDungeonUI(this.javaClass.name, "testDeadlyDifficulty")
    }

    @Test
    fun testPartyLevel() {
        onView(withId(R.id.party_level)).perform(scrollTo(), click())
        onData(anything()).atPosition(6).perform(click())
        MainActivityTest.generateDungeon()
        MainActivityTest.checkDungeonUI(this.javaClass.name, "testPartyLevel")
    }

    @Test
    fun testPartySize() {
        onView(withId(R.id.party_size)).perform(scrollTo(), click())
        onData(anything()).atPosition(0).perform(click())
        MainActivityTest.generateDungeon()
        MainActivityTest.checkDungeonUI(this.javaClass.name, "testPartySize")
    }

    @Test
    fun testTreasureValue() {
        onView(withId(R.id.treasure_value)).perform(scrollTo(), click())
        onData(anything()).atPosition(0).perform(click())
        MainActivityTest.generateDungeon()
        MainActivityTest.checkDungeonUI(this.javaClass.name, "testTreasureValue")
    }

    @Test
    fun testItemsMaxRarity() {
        onView(withId(R.id.items_rarity)).perform(scrollTo(), click())
        onData(anything()).atPosition(0).perform(click())
        MainActivityTest.generateDungeon()
        MainActivityTest.checkDungeonUI(this.javaClass.name, "testItemsMaxRarity")
    }

    @Test
    fun testRoomDensity() {
        onView(withId(R.id.room_density)).perform(scrollTo(), click())
        onData(anything()).atPosition(1).perform(click())
        MainActivityTest.generateDungeon()
        MainActivityTest.checkDungeonUI(this.javaClass.name, "testRoomDensity")
    }

    @Test
    fun testRoomSizeMedium() {
        onView(withId(R.id.room_size)).perform(scrollTo(), click())
        onData(anything()).atPosition(1).perform(click())
        MainActivityTest.generateDungeon()
        MainActivityTest.checkDungeonUI(this.javaClass.name, "testRoomSizeMedium")
    }

    @Test
    fun testRoomSizeLarge() {
        onView(withId(R.id.room_size)).perform(scrollTo(), click())
        onData(anything()).atPosition(2).perform(click())
        MainActivityTest.generateDungeon()
        MainActivityTest.checkDungeonUI(this.javaClass.name, "testRoomSizeLarge")
    }

    @Test
    fun testMonsterType() {
        onView(withId(R.id.monster_type)).perform(scrollTo(), click())
        onData(anything()).atPosition(3).perform(click())
        onView(allOf(withId(R.id.monster_ok), withText("OK"))).perform(click())
        MainActivityTest.generateDungeon()
        MainActivityTest.checkDungeonUI(this.javaClass.name, "testMonsterType")
    }

    @Test
    fun testWithOutTraps() {
        onView(withId(R.id.traps)).perform(scrollTo(), click())
        onData(anything()).atPosition(0).perform(click())
        MainActivityTest.generateDungeon()
        MainActivityTest.checkDungeonUI(this.javaClass.name, "testWithOutTraps")
    }

    @Test
    fun testWithRoamingMonsters() {
        onView(withId(R.id.roaming_monsters)).perform(scrollTo(), click())
        onData(anything()).atPosition(1).perform(click())
        MainActivityTest.generateDungeon()
        MainActivityTest.checkDungeonUI(this.javaClass.name, "testWithRoamingMonsters")
    }

    @Test
    fun testWithoutDeadEnds() {
        onView(withId(R.id.dead_end)).perform(scrollTo(), click())
        onData(anything()).atPosition(1).perform(click())
        MainActivityTest.generateDungeon()
        MainActivityTest.checkDungeonUI(this.javaClass.name, "testWithoutDeadEnds")
    }

    @Test
    fun testTheme() {
        onView(withId(R.id.theme)).perform(scrollTo(), click())
        onData(anything()).atPosition(1).perform(click())
        MainActivityTest.generateDungeon()
        MainActivityTest.checkDungeonUI(this.javaClass.name, "testTheme")
    }
}
