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
                case EDGE:
                    System.out.print("X");
                    break;
                case ROOM_EDGE:
                    System.out.print("#");
                    break;
                case MARBLE:
                    System.out.print(" ");
                    break;
                case ROOM:
                    System.out.print(".");
                    break;
                case NO_CORRIDOR_DOOR:
                case NO_CORRIDOR_DOOR_LOCKED:
                case NO_CORRIDOR_DOOR_TRAPPED:
                case DOOR:
                case DOOR_LOCKED:
                case DOOR_TRAPPED:
                    System.out.print("D");
                    break;
                case CORRIDOR:
                    System.out.print("-");
                    break;
                case ENTRY:
                    System.out.print("E");
                    break;
                case TRAP:
                    System.out.print("T");
                    break;
                default:
                    break;
            }
        }
        System.out.println();
    }
}
