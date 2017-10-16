package externius.rdmg.helpers;


final class Trap {

    private static final String[] trapSeverity = {
            "Setback",
            "Dangerous",
            "Deadly"
    };

    private static final int[] trapSave = {
            10, 12, 16, 21
    };

    private static final int[] trapAttackBonus = {
            3, 6, 9, 13
    };

    private static final int[][] trapDmgSeverity = {
            {1, 2, 4},
            {2, 4, 10},
            {4, 10, 18},
            {10, 18, 24}
    };

    private static final String[][] trapKind = { // name, save, spot, disable, disableCheck, attackMod
            {"Collapsing Roof", "Dexterity", "10", "15", "Dexterity", "false"},
            {"Falling Net", "Strength", "10", "15", "Dexterity", "false"},
            {"Fire-Breathing Statue", "Dexterity", "15", "13", "Dispel Magic", "false"},
            {"Spiked Pit", "Constitution", "15", "15", "Intelligence", "false"}, //
            {"Locking Pit", "Strength", "10", "15", "Intelligence", "false"}, //
            {"Poison Darts", "Constitution", "15", "15", "Intelligence", "true"}, //
            {"Poison Needle", "Constitution", "15", "15", "Dexterity", "false"},
            {"Rolling Sphere", "Dexterity", "15", "15", " Intelligence", "false"}
    };

    private static final String[][] trapDoorKind = { // name, save, spot, disable, disableCheck, attackMod
            {"Fire trap", "Dexterity", "10", "15", "Intelligence", "false"},
            {"Lock Covered in Dragon Bile", "Constitution", "10", "15", "Intelligence", "false"},
            {"Hail of Needles", "Dexterity", "15", "13", "Dexterity", "false"},
            {"Stone Blocks from Ceiling", "Dexterity", "15", "15", "Intelligence", "true"}, //
            {"Doorknob Smeared with Contact Poison", "Constitution", "15", "10", "Intelligence", "false"}, //
            {"Poison Darts", "Constitution", "15", "15", "Intelligence", "true"}, //
            {"Poison Needle", "Constitution", "15", "15", "Dexterity", "false"},
            {"Energy Drain", "Constitution", "15", "15", "Dispel Magic", "false"}
    };

    private static String[] currentTrap;

    private Trap() {

    }


    private static String getTrapAttackBonus(int trapDanger) {
        if (Boolean.parseBoolean(currentTrap[5])) {
            int min = trapAttackBonus[trapDanger];
            int max = trapAttackBonus[trapDanger + 1];
            return ",\n (attack bonus +" + Utils.getRandomInt(min, max) + ").";
        } else {

            return ".";
        }
    }

    private static int getTrapSaveDC(int trapDanger) {
        int min = trapSave[trapDanger];
        int max = trapSave[trapDanger + 1];
        return Utils.getRandomInt(min, max);
    }

    private static int getTrapDamage(int trapDanger) {
        if (Utils.getPartyLevel() < 5) {
            return trapDmgSeverity[0][trapDanger];
        } else if (Utils.getPartyLevel() < 11) {
            return trapDmgSeverity[1][trapDanger];
        } else if (Utils.getPartyLevel() < 17) {
            return trapDmgSeverity[2][trapDanger];
        } else {
            return trapDmgSeverity[3][trapDanger];
        }
    }

    static String getTrapName(int count) {
        return "#TRAP" + count + "#";
    }

    private static int getTrapDanger() {
        switch (Utils.getDungeonDifficulty()) {
            case 0:
                return Utils.getRandomInt(0, 1);
            case 1:
            case 2:
                return Utils.getRandomInt(0, 2);
            case 3:
                return Utils.getRandomInt(0, 3);
            default:
                return 0;
        }
    }

    static String getCurrentTrap(boolean Door) {
        int trapDanger = getTrapDanger(); // setback, dangerous, deadly
        if (Door) { // get random currentTrap index
            currentTrap = trapDoorKind[Utils.getRandomInt(0, trapDoorKind.length)];
        } else {
            currentTrap = trapKind[Utils.getRandomInt(0, trapKind.length)];
        }
        return currentTrap[0] + " [" + trapSeverity[trapDanger] + "]: DC " + currentTrap[2] + " to spot, DC " + currentTrap[3] + " to disable (" + currentTrap[4] + "), DC " + getTrapSaveDC(trapDanger) + " " + currentTrap[1] + " save or take " + getTrapDamage(trapDanger) + "D10 damage" + getTrapAttackBonus(trapDanger);
    }
}
