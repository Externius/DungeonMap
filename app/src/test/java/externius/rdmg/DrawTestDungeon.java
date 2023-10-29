package externius.rdmg;


import externius.rdmg.models.DungeonTile;

final class DrawTestDungeon {

    private DrawTestDungeon(){

    }

    static void draw(DungeonTile[][] dungeonTiles) {
        for (DungeonTile[] row : dungeonTiles) {
            printRow(row);
        }
        System.out.println();
    }

    private static void printRow(DungeonTile[] row) {
        for (DungeonTile i : row) {
            switch (i.getTexture()) {
                case EDGE -> System.out.print("X");
                case ROOM_EDGE -> System.out.print("#");
                case MARBLE -> System.out.print(" ");
                case ROOM -> System.out.print(".");
                case NO_CORRIDOR_DOOR, NO_CORRIDOR_DOOR_LOCKED, NO_CORRIDOR_DOOR_TRAPPED, DOOR, DOOR_LOCKED, DOOR_TRAPPED ->
                        System.out.print("D");
                case CORRIDOR -> System.out.print("-");
                case ENTRY -> System.out.print("E");
                case TRAP -> System.out.print("T");
                case ROAMING_MONSTER -> System.out.print("M");
                default -> {
                }
            }
        }
        System.out.println();
    }
}
