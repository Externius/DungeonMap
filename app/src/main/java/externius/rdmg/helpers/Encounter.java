package externius.rdmg.helpers;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import externius.rdmg.models.Monster;

final class Encounter {
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

    private static List<Monster> getMonsters(List<Monster> monsters) {
        List<Monster> result = new ArrayList<>();
        if (Objects.equals(Utils.getMonsterType(), "any")) {
            for (Monster monster : monsters) {
                if (parse(monster.getChallengeRating()) <= Utils.getPartyLevel() + 2 && parse(monster.getChallengeRating()) >= Math.floor(Utils.getPartyLevel() / 4)) {
                    result.add(monster);
                }
            }
        } else {
            for (Monster monster : monsters) {
                if (parse(monster.getChallengeRating()) <= Utils.getPartyLevel() + 2 && parse(monster.getChallengeRating()) >= Math.floor(Utils.getPartyLevel() / 4)
                        && monster.getType().equals(Utils.getMonsterType())) {
                    result.add(monster);
                }
            }
        }
        return result;
    }

    private static double parse(String ratio) {
        if (ratio.contains("/")) {
            String[] rat = ratio.split("/");
            return Double.parseDouble(rat[0]) / Double.parseDouble(rat[1]);
        } else {
            return Double.parseDouble(ratio);
        }
    }

    private static String calcEncounter() {
        List<Monster> filteredMonsters = getMonsters(Utils.getMonsterList()); //get monsters for party level
        int monsterCount = filteredMonsters.size();
        int monster = 0;
        double allXP;
        double count;
        while (monster < monsterCount) {
            int currentMonster = Utils.getRandomInt(0, filteredMonsters.size()); // get random monster
            int monsterXP = challengeRatingXP[challengeRating.indexOf(filteredMonsters.get(currentMonster).getChallengeRating())]; //get monster xp
            for (int i = multipliers.length - 1; i > -1; i--) {
                count = multipliers[i][0];
                allXP = monsterXP * count * multipliers[i][1];
                if (allXP <= difficulty[Utils.getDungeonDifficulty()] && count > 1) {
                    return "Monster: " + (int) count + "x " + filteredMonsters.get(currentMonster).getName() + " (CR: " + filteredMonsters.get(currentMonster).getChallengeRating() + ") " + (int) allXP + " XP";
                } else if (allXP <= difficulty[Utils.getDungeonDifficulty()]) {
                    return "Monster: " + filteredMonsters.get(currentMonster).getName() + " (CR: " + filteredMonsters.get(currentMonster).getChallengeRating() + ") " + (int) allXP + " XP";
                }
            }
            monster++;
        }
        return "Monster: None";
    }

    static String getMonster() {
        if (Math.floor(Math.random() * 100) > Utils.getPercentage()) {
            return "Monster: None";
        }
        //set difficulty
        difficulty[0] = thresholds[Utils.getPartyLevel()][0] * Utils.getPartySize();
        difficulty[1] = thresholds[Utils.getPartyLevel()][1] * Utils.getPartySize();
        difficulty[2] = thresholds[Utils.getPartyLevel()][2] * Utils.getPartySize();
        difficulty[3] = thresholds[Utils.getPartyLevel()][3] * Utils.getPartySize();
        return calcEncounter();
    }

}
