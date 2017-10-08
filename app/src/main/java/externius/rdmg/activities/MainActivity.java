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
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //get spinners
        spinnerDifficulty = (Spinner) findViewById(R.id.dungeon_difficulty);
        spinnerPartyLevel = (Spinner) findViewById(R.id.party_level);
        spinnerPartySize = (Spinner) findViewById(R.id.party_size);
        spinnerSize = (Spinner) findViewById(R.id.dungeon_size);
        spinnerRoomDens = (Spinner) findViewById(R.id.room_density);
        spinnerRoomSize = (Spinner) findViewById(R.id.room_size);
        spinnerTraps = (Spinner) findViewById(R.id.traps);
        spinnerCorridors = (Spinner) findViewById(R.id.corridors);
        spinnerMonsterType = (Spinner) findViewById(R.id.monster_type);
        spinnerDeadEnds = (Spinner) findViewById(R.id.dead_end);
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

        final Button button = (Button) findViewById(R.id.generate_button);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                generateDungeon();
            }
        });

    }

    private void generateDungeon() {
        //get values
        String dungeonDifficulty = spinnerDifficulty.getSelectedItem().toString();
        String partyLevel = spinnerPartyLevel.getSelectedItem().toString();
        String partySize = spinnerPartySize.getSelectedItem().toString();
        String size = spinnerSize.getSelectedItem().toString();
        String roomDens = spinnerRoomDens.getSelectedItem().toString();
        String roomSize = spinnerRoomSize.getSelectedItem().toString();
        String traps = spinnerTraps.getSelectedItem().toString();
        String corridors = spinnerCorridors.getSelectedItem().toString();
        String monsterType = spinnerMonsterType.getSelectedItem().toString();
        String deadEnds = spinnerDeadEnds.getSelectedItem().toString();
        Intent dungeon = new Intent(this, DungeonActivity.class);
        Bundle extras = new Bundle();
        extras.putString("DUNGEON_DIFFICULTY", dungeonDifficulty);
        extras.putString("PARTY_LEVEL", partyLevel);
        extras.putString("PARTY_SIZE", partySize);
        extras.putString("DUNGEON_SIZE", size);
        extras.putString("ROOM_DENSITY", roomDens);
        extras.putString("ROOM_SIZE", roomSize);
        extras.putString("TRAPS", traps);
        extras.putString("CORRIDORS", corridors);
        extras.putString("MONSTER_TYPE", monsterType);
        extras.putString("DEAD_ENDS", deadEnds);
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
