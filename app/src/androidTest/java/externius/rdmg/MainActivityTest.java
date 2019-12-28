package externius.rdmg;


import android.app.Activity;
import android.os.Environment;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.uiautomator.UiDevice;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runner.RunWith;

import java.io.File;

import externius.rdmg.activities.MainActivity;
import externius.rdmg.helpers.MultiSelectMonster;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class MainActivityTest {

    @Rule
    public final ActivityTestRule<MainActivity> mainActivityTestRule = new ActivityTestRule<>(MainActivity.class);

    @Test
    public void testUI() {
        Activity activity = mainActivityTestRule.getActivity();

        //textViews
        TextView dungeonSizeText = activity.findViewById(R.id.dungeon_size_text);
        TextView dungeonDifficultyText = activity.findViewById(R.id.dungeon_difficulty_text);
        TextView partyLevelText = activity.findViewById(R.id.party_level_text);
        TextView partySizeText = activity.findViewById(R.id.party_size_text);
        TextView roomDensityText = activity.findViewById(R.id.room_density_text);
        TextView roomSizeText = activity.findViewById(R.id.room_size_text);
        TextView monsterTypeText = activity.findViewById(R.id.monster_type_text);
        TextView trapsText = activity.findViewById(R.id.traps_text);
        TextView roamingMonstersText = activity.findViewById(R.id.roaming_monsters_text);
        TextView deadEndsText = activity.findViewById(R.id.dead_end_text);
        TextView corridorsText = activity.findViewById(R.id.corridors_text);
        TextView themeText = activity.findViewById(R.id.theme_text);
        TextView treasureValueText = activity.findViewById(R.id.treasure_value_text);
        TextView itemsRarityText = activity.findViewById(R.id.items_rarity_text);
        assertNotNull(dungeonSizeText);
        assertNotNull(dungeonDifficultyText);
        assertNotNull(partyLevelText);
        assertNotNull(partySizeText);
        assertNotNull(roomDensityText);
        assertNotNull(roomSizeText);
        assertNotNull(monsterTypeText);
        assertNotNull(trapsText);
        assertNotNull(roamingMonstersText);
        assertNotNull(deadEndsText);
        assertNotNull(corridorsText);
        assertNotNull(themeText);
        assertNotNull(treasureValueText);
        assertNotNull(itemsRarityText);
        assertTrue(dungeonSizeText.isShown());
        assertTrue(dungeonDifficultyText.isShown());
        assertTrue(partyLevelText.isShown());
        assertTrue(partySizeText.isShown());
        assertTrue(roomDensityText.isShown());
        assertTrue(roomSizeText.isShown());
        assertTrue(monsterTypeText.isShown());
        assertTrue(trapsText.isShown());
        assertTrue(roamingMonstersText.isShown());
        assertTrue(deadEndsText.isShown());
        assertTrue(corridorsText.isShown());
        assertTrue(themeText.isShown());
        assertTrue(treasureValueText.isShown());
        assertTrue(itemsRarityText.isShown());
        assertEquals(getApplicationContext().getString(R.string.dungeon_size), dungeonSizeText.getText());
        assertEquals(getApplicationContext().getString(R.string.dungeon_difficulty), dungeonDifficultyText.getText());
        assertEquals(getApplicationContext().getString(R.string.party_level), partyLevelText.getText());
        assertEquals(getApplicationContext().getString(R.string.party_size), partySizeText.getText());
        assertEquals(getApplicationContext().getString(R.string.room_density), roomDensityText.getText());
        assertEquals(getApplicationContext().getString(R.string.room_size), roomSizeText.getText());
        assertEquals(getApplicationContext().getString(R.string.monster_type), monsterTypeText.getText());
        assertEquals(getApplicationContext().getString(R.string.traps), trapsText.getText());
        assertEquals(getApplicationContext().getString(R.string.roaming_monsters), roamingMonstersText.getText());
        assertEquals(getApplicationContext().getString(R.string.dead_ends), deadEndsText.getText());
        assertEquals(getApplicationContext().getString(R.string.corridors), corridorsText.getText());
        assertEquals(getApplicationContext().getString(R.string.theme_value), themeText.getText());
        assertEquals(getApplicationContext().getString(R.string.treasure_value), treasureValueText.getText());
        assertEquals(getApplicationContext().getString(R.string.items_rarity), itemsRarityText.getText());

        //spinners
        Spinner dungeonSize = activity.findViewById(R.id.dungeon_size);
        Spinner dungeonDifficulty = activity.findViewById(R.id.dungeon_difficulty);
        Spinner partyLevel = activity.findViewById(R.id.party_level);
        Spinner partySize = activity.findViewById(R.id.party_size);
        Spinner roomDensity = activity.findViewById(R.id.room_density);
        Spinner roomSize = activity.findViewById(R.id.room_size);
        MultiSelectMonster monsterType = activity.findViewById(R.id.monster_type);
        Spinner traps = activity.findViewById(R.id.traps);
        Spinner roamingMonsters = activity.findViewById(R.id.roaming_monsters);
        Spinner deadEnds = activity.findViewById(R.id.dead_end);
        Spinner corridors = activity.findViewById(R.id.corridors);
        Spinner theme = activity.findViewById(R.id.theme);
        Spinner treasureValue = activity.findViewById(R.id.treasure_value);
        Spinner itemsRarity = activity.findViewById(R.id.items_rarity);
        assertNotNull(dungeonSize);
        assertNotNull(dungeonDifficulty);
        assertNotNull(partyLevel);
        assertNotNull(partySize);
        assertNotNull(roomDensity);
        assertNotNull(roomSize);
        assertNotNull(monsterType);
        assertNotNull(traps);
        assertNotNull(roamingMonsters);
        assertNotNull(deadEnds);
        assertNotNull(corridors);
        assertNotNull(theme);
        assertNotNull(treasureValue);
        assertNotNull(itemsRarity);
        assertTrue(dungeonSize.isShown());
        assertTrue(dungeonDifficulty.isShown());
        assertTrue(partyLevel.isShown());
        assertTrue(partySize.isShown());
        assertTrue(roomDensity.isShown());
        assertTrue(roomSize.isShown());
        assertTrue(monsterType.isShown());
        assertTrue(traps.isShown());
        assertTrue(roamingMonsters.isShown());
        assertTrue(deadEnds.isShown());
        assertTrue(corridors.isShown());
        assertTrue(theme.isShown());
        assertTrue(treasureValue.isShown());
        assertTrue(itemsRarity.isShown());

        //buttons
        Button generateButton = activity.findViewById(R.id.generate_button);
        Button loadButton = activity.findViewById(R.id.load_button);
        assertNotNull(generateButton);
        assertNotNull(loadButton);
        assertTrue(generateButton.isShown());
        assertTrue(loadButton.isShown());
    }

    public static void generateDungeon() {
        onView(withId(R.id.generate_button)).perform(click());
        onView(withId(R.id.dungeon_activity_generate_button)).perform(scrollTo(), click());
    }

    public static void checkDungeonUI(String className, String name) {
        onView(withId(R.id.dungeonMap_view)).check(matches(isDisplayed()));
        onView(withId(R.id.dungeon_activity_save_button)).check(matches(isDisplayed()));
        onView(withId(R.id.dungeon_activity_generate_button)).check(matches(isDisplayed()));
        onView(withId(R.id.dungeon_activity_export_button)).perform(scrollTo());
        onView(withId(R.id.dungeon_activity_export_button)).check(matches(isDisplayed()));
        onView(withText("#ROOM1#")).perform(scrollTo());
        onView(withText("#ROOM1#")).check(matches(isDisplayed()));
        Description description = Description.createTestDescription(className, name);
        takeScreenshot(description);
    }

    public static void takeScreenshot(Description description) {
        // Save to external storage (usually /sdcard/screenshots)
        File path = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
                + "/screenshots/" + getApplicationContext().getPackageName());
        if (!path.exists()) {
            if (path.mkdirs()) {
                takeIt(path, description);
            }
        } else {
            takeIt(path, description);
        }
    }

    private static void takeIt(File path, Description description) {
        UiDevice device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        String filename = description.getClassName() + "_" + description.getMethodName() + ".png";
        device.takeScreenshot(new File(path, filename));
    }
}
