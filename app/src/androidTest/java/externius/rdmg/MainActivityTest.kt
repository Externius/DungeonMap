package externius.rdmg

import android.os.Environment
import android.os.SystemClock
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import externius.rdmg.activities.MainActivity
import externius.rdmg.helpers.MultiSelectMonster
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.Description
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class MainActivityTest {

    @get:Rule
    val mainActivityTestRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun testUI() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                // textViews
                val dungeonSizeText: TextView? = activity.findViewById(R.id.dungeon_size_text)
                val dungeonDifficultyText: TextView? = activity.findViewById(R.id.dungeon_difficulty_text)
                val partyLevelText: TextView? = activity.findViewById(R.id.party_level_text)
                val partySizeText: TextView? = activity.findViewById(R.id.party_size_text)
                val roomDensityText: TextView? = activity.findViewById(R.id.room_density_text)
                val roomSizeText: TextView? = activity.findViewById(R.id.room_size_text)
                val monsterTypeText: TextView? = activity.findViewById(R.id.monster_type_text)
                val trapsText: TextView? = activity.findViewById(R.id.traps_text)
                val roamingMonstersText: TextView? = activity.findViewById(R.id.roaming_monsters_text)
                val deadEndsText: TextView? = activity.findViewById(R.id.dead_end_text)
                val corridorsText: TextView? = activity.findViewById(R.id.corridors_text)
                val themeText: TextView? = activity.findViewById(R.id.theme_text)
                val treasureValueText: TextView? = activity.findViewById(R.id.treasure_value_text)
                val itemsRarityText: TextView? = activity.findViewById(R.id.items_rarity_text)

                assertNotNull(dungeonSizeText)
                assertNotNull(dungeonDifficultyText)
                assertNotNull(partyLevelText)
                assertNotNull(partySizeText)
                assertNotNull(roomDensityText)
                assertNotNull(roomSizeText)
                assertNotNull(monsterTypeText)
                assertNotNull(trapsText)
                assertNotNull(roamingMonstersText)
                assertNotNull(deadEndsText)
                assertNotNull(corridorsText)
                assertNotNull(themeText)
                assertNotNull(treasureValueText)
                assertNotNull(itemsRarityText)

                assertTrue(dungeonSizeText!!.isShown)
                assertTrue(dungeonDifficultyText!!.isShown)
                assertTrue(partyLevelText!!.isShown)
                assertTrue(partySizeText!!.isShown)
                assertTrue(roomDensityText!!.isShown)
                assertTrue(roomSizeText!!.isShown)
                assertTrue(monsterTypeText!!.isShown)
                assertTrue(trapsText!!.isShown)
                assertTrue(roamingMonstersText!!.isShown)
                assertTrue(deadEndsText!!.isShown)
                assertTrue(corridorsText!!.isShown)
                assertTrue(themeText!!.isShown)
                assertTrue(treasureValueText!!.isShown)
                assertTrue(itemsRarityText!!.isShown)

                assertEquals(getApplicationContext<android.content.Context>().getString(R.string.dungeon_size), dungeonSizeText.text)
                assertEquals(getApplicationContext<android.content.Context>().getString(R.string.dungeon_difficulty), dungeonDifficultyText.text)
                assertEquals(getApplicationContext<android.content.Context>().getString(R.string.party_level), partyLevelText.text)
                assertEquals(getApplicationContext<android.content.Context>().getString(R.string.party_size), partySizeText.text)
                assertEquals(getApplicationContext<android.content.Context>().getString(R.string.room_density), roomDensityText.text)
                assertEquals(getApplicationContext<android.content.Context>().getString(R.string.room_size), roomSizeText.text)
                assertEquals(getApplicationContext<android.content.Context>().getString(R.string.monster_type), monsterTypeText.text)
                assertEquals(getApplicationContext<android.content.Context>().getString(R.string.traps), trapsText.text)
                assertEquals(getApplicationContext<android.content.Context>().getString(R.string.roaming_monsters), roamingMonstersText.text)
                assertEquals(getApplicationContext<android.content.Context>().getString(R.string.dead_ends), deadEndsText.text)
                assertEquals(getApplicationContext<android.content.Context>().getString(R.string.corridors), corridorsText.text)
                assertEquals(getApplicationContext<android.content.Context>().getString(R.string.theme_value), themeText.text)
                assertEquals(getApplicationContext<android.content.Context>().getString(R.string.treasure_value), treasureValueText.text)
                assertEquals(getApplicationContext<android.content.Context>().getString(R.string.items_rarity), itemsRarityText.text)

                // spinners
                val dungeonSize: Spinner? = activity.findViewById(R.id.dungeon_size)
                val dungeonDifficulty: Spinner? = activity.findViewById(R.id.dungeon_difficulty)
                val partyLevel: Spinner? = activity.findViewById(R.id.party_level)
                val partySize: Spinner? = activity.findViewById(R.id.party_size)
                val roomDensity: Spinner? = activity.findViewById(R.id.room_density)
                val roomSize: Spinner? = activity.findViewById(R.id.room_size)
                val monsterType: MultiSelectMonster? = activity.findViewById(R.id.monster_type)
                val traps: Spinner? = activity.findViewById(R.id.traps)
                val roamingMonsters: Spinner? = activity.findViewById(R.id.roaming_monsters)
                val deadEnds: Spinner? = activity.findViewById(R.id.dead_end)
                val corridors: Spinner? = activity.findViewById(R.id.corridors)
                val theme: Spinner? = activity.findViewById(R.id.theme)
                val treasureValue: Spinner? = activity.findViewById(R.id.treasure_value)
                val itemsRarity: Spinner? = activity.findViewById(R.id.items_rarity)

                assertNotNull(dungeonSize)
                assertNotNull(dungeonDifficulty)
                assertNotNull(partyLevel)
                assertNotNull(partySize)
                assertNotNull(roomDensity)
                assertNotNull(roomSize)
                assertNotNull(monsterType)
                assertNotNull(traps)
                assertNotNull(roamingMonsters)
                assertNotNull(deadEnds)
                assertNotNull(corridors)
                assertNotNull(theme)
                assertNotNull(treasureValue)
                assertNotNull(itemsRarity)

                assertTrue(dungeonSize!!.isShown)
                assertTrue(dungeonDifficulty!!.isShown)
                assertTrue(partyLevel!!.isShown)
                assertTrue(partySize!!.isShown)
                assertTrue(roomDensity!!.isShown)
                assertTrue(roomSize!!.isShown)
                assertTrue(monsterType!!.isShown)
                assertTrue(traps!!.isShown)
                assertTrue(roamingMonsters!!.isShown)
                assertTrue(deadEnds!!.isShown)
                assertTrue(corridors!!.isShown)
                assertTrue(theme!!.isShown)
                assertTrue(treasureValue!!.isShown)
                assertTrue(itemsRarity!!.isShown)

                // buttons
                val generateButton: Button? = activity.findViewById(R.id.generate_button)
                val loadButton: Button? = activity.findViewById(R.id.load_button)
                assertNotNull(generateButton)
                assertNotNull(loadButton)
                assertTrue(generateButton!!.isShown)
                assertTrue(loadButton!!.isShown)
            }
        }
    }

    companion object {
        @JvmStatic
        fun generateDungeon() {
            onView(withId(R.id.generate_button)).perform(click())
            SystemClock.sleep(2000)
            onView(withId(R.id.dungeon_activity_generate_button)).perform(scrollTo(), click())
        }

        @JvmStatic
        fun checkDungeonUI(className: String, name: String) {
            onView(withId(R.id.dungeonMap_view)).check(matches(isDisplayed()))
            onView(withId(R.id.dungeon_activity_save_button)).check(matches(isDisplayed()))
            onView(withId(R.id.dungeon_activity_generate_button)).check(matches(isDisplayed()))
            onView(withId(R.id.dungeon_activity_export_button)).perform(scrollTo())
            onView(withId(R.id.dungeon_activity_export_button)).check(matches(isDisplayed()))
            onView(withText("#ROOM1#")).perform(scrollTo())
            onView(withText("#ROOM1#")).check(matches(isDisplayed()))
            val description = Description.createTestDescription(className, name)
            takeScreenshot(description)
        }

        @JvmStatic
        fun takeScreenshot(description: Description) {
            val path = File(
                Environment.getExternalStorageDirectory().absolutePath +
                        "/screenshots/" + getApplicationContext<android.content.Context>().packageName
            )
            if (!path.exists()) {
                if (path.mkdirs()) {
                    takeIt(path, description)
                }
            } else {
                takeIt(path, description)
            }
        }

        private fun takeIt(path: File, description: Description) {
            val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
            val filename = "${description.className}_${description.methodName}.png"
            device.takeScreenshot(File(path, filename))
        }
    }
}
