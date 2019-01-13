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

    private static final String[][] trapKind = { // name, save, spot, disable, disableCheck, attackMod, dmg type, special
            {"Collapsing Roof", "Dexterity", "10", "15", "Dexterity", "false", "bludgeoning", ""},
            {"Falling Net", "Strength", "10", "15", "Dexterity", "false", "", "restrained."},
            {"Fire-Breathing Statue", "Dexterity", "15", "13", "Dispel Magic", "false", "fire", ""},
            {"Spiked Pit", "Constitution", "15", "15", "Intelligence", "false", "piercing", ""},
            {"Locking Pit", "Strength", "10", "15", "Intelligence", "false", "", "locked."},
            {"Poison Darts", "Constitution", "15", "15", "Intelligence", "true", "poison", ""},
            {"Poison Needle", "Constitution", "15", "15", "Dexterity", "false", "poison", ""},
            {"Rolling Sphere", "Dexterity", "15", "15", "Intelligence", "false", "bludgeoning", ""}
    };

    private static final String[][] trapDoorKind = { // name, save, spot, disable, disableCheck, attackMod. dmg type, special
            {"Fire trap", "Dexterity", "10", "15", "Intelligence", "false", "fire", ""},
            {"Lock Covered in Dragon Bile", "Constitution", "10", "15", "Intelligence", "false", "poison", ""},
            {"Hail of Needles", "Dexterity", "15", "13", "Dexterity", "false", "piercing", ""},
            {"Stone Blocks from Ceiling", "Dexterity", "15", "15", "Intelligence", "true", "bludgeoning", ""},
            {"Doorknob Smeared with Contact Poison", "Constitution", "15", "10", "Intelligence", "false", "poison", ""},
            {"Poison Darts", "Constitution", "15", "15", "Intelligence", "true", "poison", ""},
            {"Poison Needle", "Constitution", "15", "15", "Dexterity", "false", "poison", ""},
            {"Energy Drain", "Constitution", "15", "15", "Dispel Magic", "false", "necrotic", ""}
    };

    private static String[] currentTrap;

    private Trap() {

    }

    private static String getTrapAttackBonus(int trapDanger) {
        if (Boolean.parseBoolean(currentTrap[5])) {
            int min = trapAttackBonus[trapDanger];
            int max = trapAttackBonus[trapDanger + 1];
            return ", (attack bonus +" + Utils.getRandomInt(min, max) + ").";
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

    static String getCurrentTrap(boolean door) {
        int trapDanger = getTrapDanger(); // setback, dangerous, deadly
        if (door) { // get random currentTrap index
            currentTrap = trapDoorKind[Utils.getRandomInt(0, trapDoorKind.length)];
        } else {
            currentTrap = trapKind[Utils.getRandomInt(0, trapKind.length)];
        }
        if (currentTrap[6] != null && !currentTrap[6].isEmpty()) { // check dmg type
            return currentTrap[0] + " [" + trapSeverity[trapDanger] + "]: DC " + currentTrap[2] + " to spot, DC " + currentTrap[3] + " to disable (" + currentTrap[4] + "), DC " + getTrapSaveDC(trapDanger) + " " + currentTrap[1] + " save or take " + getTrapDamage(trapDanger) + "D10 (" + currentTrap[6] + ") damage" + getTrapAttackBonus(trapDanger);
        } else {
            return currentTrap[0] + " [" + trapSeverity[trapDanger] + "]: DC " + currentTrap[2] + " to spot, DC " + currentTrap[3] + " to disable (" + currentTrap[4] + "), DC " + getTrapSaveDC(trapDanger) + " " + currentTrap[1] + " save or " + currentTrap[7];
        }
    }
}
