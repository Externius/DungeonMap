package externius.rdmg;


import android.app.Activity;
import android.os.Environment;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.UiDevice;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runner.RunWith;

import java.io.File;

import externius.rdmg.activities.MainActivity;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class MainActivityTest {

    @Rule
    public final ActivityTestRule<MainActivity> mainActivityActivityTestRule = new ActivityTestRule<>(MainActivity.class);

    @Test
    public void testUI() {
        Activity activity = mainActivityActivityTestRule.getActivity();
        //textViews
        assertNotNull(activity.findViewById(R.id.dungeon_size_text));
        assertNotNull(activity.findViewById(R.id.dungeon_difficulty_text));
        assertNotNull(activity.findViewById(R.id.party_level_text));
        assertNotNull(activity.findViewById(R.id.party_size_text));
        assertNotNull(activity.findViewById(R.id.room_density_text));
        assertNotNull(activity.findViewById(R.id.room_size_text));
        assertNotNull(activity.findViewById(R.id.monster_type_text));
        assertNotNull(activity.findViewById(R.id.traps_text));
        assertNotNull(activity.findViewById(R.id.dead_end_text));
        assertNotNull(activity.findViewById(R.id.corridors_text));
        TextView dungeonText = activity.findViewById(R.id.dungeon_size_text);
        TextView dungeonDifficultyText = activity.findViewById(R.id.dungeon_difficulty_text);
        TextView partyLevelText = activity.findViewById(R.id.party_level_text);
        TextView partySizeText = activity.findViewById(R.id.party_size_text);
        TextView roomDensityText = activity.findViewById(R.id.room_density_text);
        TextView roomSizeText = activity.findViewById(R.id.room_size_text);
        TextView monsterTypeText = activity.findViewById(R.id.monster_type_text);
        TextView trapsText = activity.findViewById(R.id.traps_text);
        TextView deadEndsText = activity.findViewById(R.id.dead_end_text);
        TextView corridorsText = activity.findViewById(R.id.corridors_text);
        assertTrue(dungeonText.isShown());
        assertTrue(dungeonDifficultyText.isShown());
        assertTrue(partyLevelText.isShown());
        assertTrue(partySizeText.isShown());
        assertTrue(roomDensityText.isShown());
        assertTrue(roomSizeText.isShown());
        assertTrue(monsterTypeText.isShown());
        assertTrue(trapsText.isShown());
        assertTrue(deadEndsText.isShown());
        assertTrue(corridorsText.isShown());
        assertEquals(getTargetContext().getString(R.string.dungeon_size), dungeonText.getText());
        assertEquals(getTargetContext().getString(R.string.dungeon_difficulty), dungeonDifficultyText.getText());
        assertEquals(getTargetContext().getString(R.string.party_level), partyLevelText.getText());
        assertEquals(getTargetContext().getString(R.string.party_size), partySizeText.getText());
        assertEquals(getTargetContext().getString(R.string.room_density), roomDensityText.getText());
        assertEquals(getTargetContext().getString(R.string.room_size), roomSizeText.getText());
        assertEquals(getTargetContext().getString(R.string.monster_type), monsterTypeText.getText());
        assertEquals(getTargetContext().getString(R.string.traps), trapsText.getText());
        assertEquals(getTargetContext().getString(R.string.dead_ends), deadEndsText.getText());
        assertEquals(getTargetContext().getString(R.string.corridors), corridorsText.getText());

        //spinners
        assertNotNull(activity.findViewById(R.id.dungeon_size));
        assertNotNull(activity.findViewById(R.id.dungeon_difficulty));
        assertNotNull(activity.findViewById(R.id.party_level));
        assertNotNull(activity.findViewById(R.id.party_size));
        assertNotNull(activity.findViewById(R.id.room_density));
        assertNotNull(activity.findViewById(R.id.room_size));
        assertNotNull(activity.findViewById(R.id.monster_type));
        assertNotNull(activity.findViewById(R.id.traps));
        assertNotNull(activity.findViewById(R.id.dead_end));
        assertNotNull(activity.findViewById(R.id.corridors));
        Spinner dungeonSize = activity.findViewById(R.id.dungeon_size);
        Spinner dungeonDifficulty = activity.findViewById(R.id.dungeon_difficulty);
        Spinner partyLevel = activity.findViewById(R.id.party_level);
        Spinner partySize = activity.findViewById(R.id.party_size);
        Spinner roomDensity = activity.findViewById(R.id.room_density);
        Spinner roomSize = activity.findViewById(R.id.room_size);
        Spinner monsterType = activity.findViewById(R.id.monster_type);
        Spinner traps = activity.findViewById(R.id.traps);
        Spinner deadEnds = activity.findViewById(R.id.dead_end);
        Spinner corridors = activity.findViewById(R.id.corridors);
        assertTrue(dungeonSize.isShown());
        assertTrue(dungeonDifficulty.isShown());
        assertTrue(partyLevel.isShown());
        assertTrue(partySize.isShown());
        assertTrue(roomDensity.isShown());
        assertTrue(roomSize.isShown());
        assertTrue(monsterType.isShown());
        assertTrue(traps.isShown());
        assertTrue(deadEnds.isShown());
        assertTrue(corridors.isShown());

        //button
        assertNotNull(activity.findViewById(R.id.generate_button));
        Button button = activity.findViewById(R.id.generate_button);
        assertTrue(button.isShown());
    }


    public static void checkDungeonUI(String className, String name) {
        onView(withId(R.id.dungeonMap_view)).check(matches(isDisplayed()));
        onView(withId(R.id.dungeon_activity_generate_button)).check(matches(isDisplayed()));
        onView(withId(R.id.dungeon_activity_export_button)).check(matches(isDisplayed()));
        onView(withText("#ROOM1#")).check(matches(isDisplayed()));
        Description description = Description.createTestDescription(className, name);
        takeScreenshot(description);
    }

    public static void takeScreenshot(Description description) {
        // Save to external storage (usually /sdcard/screenshots)
        File path = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
                + "/screenshots/" + getTargetContext().getPackageName());
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
