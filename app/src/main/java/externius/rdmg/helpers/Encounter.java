package externius.rdmg.helpers;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import externius.rdmg.models.Monster;

final class Encounter {
    private static final String base = "Monsters: ";
    private static final String none = "None";
    private static final String notSuitable = "No suitable monsters with this settings";
    private static List<Monster> filteredMonsters;
    private static int sumXP;
    private static final int[] challengeRatingXP = {
            10,
            25,
            50,
            100,
            200,
            450,
            700,
            1100,
            1800,
            2300,
            2900,
            3900,
            5000,
            5900,
            7200,
            8400,
            10000,
            11500,
            13000,
            15000,
            18000,
            20000,
            22000,
            25000,
            33000,
            41000,
            50000,
            62000,
            75000,
            90000,
            105000,
            120000,
            135000,
            155000
    };

    private static final double[][] multipliers = {
            {1, 1},
            {2, 1.5},
            {3, 2},
            {7, 2.5},
            {11, 3},
            {15, 4}
    };

    private static final List<String> challengeRating = new ArrayList<>(Arrays.asList("0", "1/8", "1/4", "1/2", "1", "2", "3", "4", "5",
            "6", "7", "8", "9", "10", "11", "12", "13", "14", "15",
            "16", "17", "18", "19", "20", "21", "22", "23", "24", "25",
            "26", "27", "28", "29", "30"));

    private static final int[][] thresholds = {
            {0, 0, 0, 0},
            {25, 50, 75, 100},
            {50, 100, 150, 200},
            {75, 150, 225, 400},
            {125, 250, 375, 500},
            {250, 500, 750, 1100},
            {300, 600, 900, 1400},
            {350, 750, 1100, 1700},
            {450, 900, 1400, 2100},
            {550, 1100, 1600, 2400},
            {600, 1200, 1900, 2800},
            {800, 1600, 2400, 3600},
            {1000, 2000, 3000, 4500},
            {1100, 2200, 3400, 5100},
            {1250, 2500, 3800, 5700},
            {1400, 2800, 4300, 6400},
            {1600, 3200, 4800, 7200},
            {2000, 3900, 5900, 8800},
            {2100, 4200, 6300, 9500},
            {2400, 4900, 7300, 10900},
            {2800, 5700, 8500, 12700}
    };

    private static final int[] difficulty = {
            0, 0, 0, 0
    };

    private Encounter() {

    }

    private static int getMonsterXP(Monster monster) {
        return challengeRatingXP[challengeRating.indexOf(monster.getChallengeRating())];
    }

    private static String addMonster(int currentXP) {
        int monsterCount = filteredMonsters.size();
        int monster = 0;
        int count;
        double allXP;
        while (monster < monsterCount) {
            Monster currentMonster = filteredMonsters.get(Utils.getRandomInt(0, filteredMonsters.size())); // get random monster
            filteredMonsters.remove(currentMonster);
            int monsterXP = getMonsterXP(currentMonster);
            for (int i = multipliers.length - 1; i > -1; i--) {
                count = (int) multipliers[i][0];
                allXP = monsterXP * count * multipliers[i][1];
                if (allXP <= currentXP && count > 1) {
                    return count + "x " + currentMonster.getName() + " (CR: " + currentMonster.getChallengeRating() + ") " + monsterXP * count + " XP";
                } else if (allXP <= currentXP) {
                    return currentMonster.getName() + " (CR: " + currentMonster.getChallengeRating() + ") " + monsterXP + " XP";
                }
            }
            monster++;
        }
        return none;
    }

    private static boolean checkPossible() {
        for (Monster monster : filteredMonsters) {
            if (sumXP > getMonsterXP(monster)) {
                return true;
            }
        }
        return false;
    }

    private static String calcEncounter() {
        StringBuilder result = new StringBuilder();
        result.append(base);
        if (Math.floor(Math.random() * 100) > 50) {
            result.append(addMonster(sumXP));
        } else {
            int x = Utils.getRandomInt(2, Utils.getDungeonDifficulty() + 3);
            for (int i = 0; i < x; i++) {
                result.append(addMonster(sumXP / x));
                result.append(", ");
            }
            result.setLength(result.length() - 2);
        }
        return result.toString().replaceAll(", " + none, "");
    }

    private static void init() {
        difficulty[0] = thresholds[Utils.getPartyLevel()][0] * Utils.getPartySize();
        difficulty[1] = thresholds[Utils.getPartyLevel()][1] * Utils.getPartySize();
        difficulty[2] = thresholds[Utils.getPartyLevel()][2] * Utils.getPartySize();
        difficulty[3] = thresholds[Utils.getPartyLevel()][3] * Utils.getPartySize();
        filteredMonsters = new ArrayList<>(Utils.getMonsterList()); // get monsters for party level
        sumXP = difficulty[Utils.getDungeonDifficulty()];
    }

    static String getMonster() {
        if (Utils.getMonsterType().equalsIgnoreCase("none")) {
            return base + none;
        }
        init();
        boolean checkResult = checkPossible();
        if (checkResult && Math.floor(Math.random() * 100) <= Utils.getMonsterPercentage()) {
            return calcEncounter();
        } else if (!checkResult) {
            return base + notSuitable;
        } else {
            return base + none;
        }
    }

    static String getRoamingName(int count) {
        return "ROAMING MONSTERS " + count + "# ";
    }

    static String getRoamingMonster() {
        init();
        if (checkPossible()) {
            return calcEncounter().substring(10); // remove "Monsters: "
        } else {
            return notSuitable;
        }
    }
}
