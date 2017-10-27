package externius.rdmg.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;

import java.util.Objects;

import externius.rdmg.R;

public class MainActivity extends AppCompatActivity {

    private Spinner spinnerDifficulty;
    private Spinner spinnerPartyLevel;
    private Spinner spinnerPartySize;
    private Spinner spinnerTreasureValue;
    private Spinner spinnerItemsRarity;
    private Spinner spinnerSize;
    private Spinner spinnerRoomDens;
    private Spinner spinnerRoomSize;
    private Spinner spinnerTraps;
    private Spinner spinnerCorridors;
    private Spinner spinnerMonsterType;
    private Spinner spinnerDeadEnds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //get spinners
        spinnerDifficulty = findViewById(R.id.dungeon_difficulty);
        spinnerPartyLevel = findViewById(R.id.party_level);
        spinnerPartySize = findViewById(R.id.party_size);
        spinnerTreasureValue = findViewById(R.id.treasure_value);
        spinnerItemsRarity = findViewById(R.id.items_rarity);
        spinnerSize = findViewById(R.id.dungeon_size);
        spinnerRoomDens = findViewById(R.id.room_density);
        spinnerRoomSize = findViewById(R.id.room_size);
        spinnerTraps = findViewById(R.id.traps);
        spinnerCorridors = findViewById(R.id.corridors);
        spinnerMonsterType = findViewById(R.id.monster_type);
        spinnerDeadEnds = findViewById(R.id.dead_end);
        spinnerCorridors.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (Objects.equals(spinnerCorridors.getSelectedItem().toString(), "No")) {
                    spinnerTraps.setEnabled(false);
                    spinnerRoomDens.setEnabled(false);
                    spinnerDeadEnds.setEnabled(false);
                } else {
                    spinnerTraps.setEnabled(true);
                    spinnerRoomDens.setEnabled(true);
                    spinnerDeadEnds.setEnabled(true);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        //set some default value
        spinnerDifficulty.setSelection(1);
        spinnerPartySize.setSelection(3);
        spinnerTraps.setSelection(1);
        spinnerTreasureValue.setSelection(1);
        spinnerItemsRarity.setSelection(1);
        final Button button = findViewById(R.id.generate_button);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                generateDungeon();
            }
        });

    }

    private void generateDungeon() {
        Intent dungeon = new Intent(this, DungeonActivity.class);
        Bundle extras = new Bundle();
        extras.putString("DUNGEON_DIFFICULTY", spinnerDifficulty.getSelectedItem().toString());
        extras.putString("PARTY_LEVEL", spinnerPartyLevel.getSelectedItem().toString());
        extras.putString("PARTY_SIZE", spinnerPartySize.getSelectedItem().toString());
        extras.putString("TREASURE_VALUE", spinnerTreasureValue.getSelectedItem().toString());
        extras.putString("ITEMS_RARITY", spinnerItemsRarity.getSelectedItem().toString());
        extras.putString("DUNGEON_SIZE", spinnerSize.getSelectedItem().toString());
        extras.putString("ROOM_DENSITY", spinnerRoomDens.getSelectedItem().toString());
        extras.putString("ROOM_SIZE", spinnerRoomSize.getSelectedItem().toString());
        extras.putString("TRAPS", spinnerTraps.getSelectedItem().toString());
        extras.putString("CORRIDORS", spinnerCorridors.getSelectedItem().toString());
        extras.putString("MONSTER_TYPE", spinnerMonsterType.getSelectedItem().toString());
        extras.putString("DEAD_ENDS", spinnerDeadEnds.getSelectedItem().toString());
        dungeon.putExtras(extras);
        startActivity(dungeon);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_about) {
            Intent about = new Intent(this, AboutActivity.class);
            startActivity(about);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
