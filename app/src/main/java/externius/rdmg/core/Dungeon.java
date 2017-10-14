package externius.rdmg.core;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import externius.rdmg.helpers.Utils;
import externius.rdmg.models.DungeonTile;
import externius.rdmg.models.RoomDescription;
import externius.rdmg.models.Textures;
import externius.rdmg.models.TrapDescription;


public class Dungeon {
    private static final int MOVEMENT = 10;
    final List<DungeonTile> rooms = new ArrayList<>();
    DungeonTile[][] dungeonTiles;
    List<RoomDescription> roomDescription = new ArrayList<>();
    int dungeonWidth;
    int dungeonHeight;
    int dungeonSize;
    int roomSizePercent;
    int roomSize;
    List<DungeonTile> doors = new ArrayList<>();
    private List<DungeonTile> result = new ArrayList<>();
    private List<DungeonTile> corridors = new ArrayList<>();
    private List<TrapDescription> trapDescription = new ArrayList<>();
    private int roomDensity;
    private int trapPercent;
    private int trapCount;
    private int roomCount;
    private boolean hasDeadEnds;

    Dungeon() {

    }

    public Dungeon(int dungeonWidth, int dungeonHeight, int dungeonSize, int roomDensity, int roomSizePercent, int trapPercent, boolean hasDeadEnds) {
        this.dungeonWidth = dungeonWidth;
        this.dungeonHeight = dungeonHeight;
        this.dungeonSize = dungeonSize;
        this.roomDensity = roomDensity;
        this.roomSizePercent = roomSizePercent;
        this.trapPercent = trapPercent;
        this.hasDeadEnds = hasDeadEnds;
    }

    public void generate() {
        init();
        generateRoom();
        addEntryPoint();
        generateCorridors();
        if (hasDeadEnds) {
            addDeadEnds();
        }
        addRandomTrap();
    }

    public void init() {
        corridors = new ArrayList<>();
        int imgSizeX = dungeonWidth / dungeonSize;
        int imgSizeY = dungeonHeight / dungeonSize;
        roomCount = Math.round(((float) dungeonSize / 100) * (float) roomDensity);
        roomSize = Math.round((float) (dungeonSize - Math.round(dungeonSize * 0.35)) / 100 * roomSizePercent);
        roomDescription = new ArrayList<>();
        trapDescription = new ArrayList<>();
        trapCount = dungeonSize * trapPercent / 100;
        dungeonSize += 2; // because of boundaries
        dungeonTiles = new DungeonTile[dungeonSize][dungeonSize];
        for (int i = 0; i < dungeonSize; i++) {
            for (int j = 0; j < dungeonSize; j++) {
                dungeonTiles[i][j] = new DungeonTile(i, j);
            }
        }
        for (int i = 1; i < dungeonSize - 1; i++) { // set drawing area
            for (int j = 1; j < dungeonSize - 1; j++) {
                dungeonTiles[i][j] = new DungeonTile(i, j, (j - 1) * imgSizeX, (i - 1) * imgSizeY, imgSizeX, imgSizeY);
            }
        }
    }

    public void addRandomTrap() {
        int count = 0;
        while (trapCount > count) {
            int x = Utils.getRandomInt(0, corridors.size());
            int i = corridors.get(x).getI();
            int j = corridors.get(x).getJ();
            if (dungeonTiles[i][j].getTexture() == Textures.CORRIDOR) {
                addTrap(i, j);
                count++;
            }
        }
    }

    private void addTrap(int x, int y) {
        dungeonTiles[x][y].setTexture(Textures.TRAP);
        Utils.addTrapDescription(dungeonTiles, x, y, trapDescription);
    }

    public void addDeadEnds() {
        List<DungeonTile> deadEnds = generateDeadEnds();
        DungeonTile firstDoor = doors.get(0); // get  first door
        for (DungeonTile end : deadEnds) {
            doors = new ArrayList<>(); // empty doors
            doors.add(firstDoor);
            doors.add(end);
            generateCorridors();
        }
    }

    private List<DungeonTile> generateDeadEnds() {
        int count = roomCount / 2;
        int deadEndsCount = 0;
        List<DungeonTile> deadEnds = new ArrayList<>();
        DungeonTile[][] croppedDungeonTiles = new DungeonTile[dungeonTiles.length - 4][dungeonTiles.length - 4];
        for (int i = 2; i < dungeonTiles.length - 2; i++) {
            System.arraycopy(dungeonTiles[i], 2, croppedDungeonTiles[i - 2], 0, dungeonTiles[i].length - 4);
        }
        List<DungeonTile> dungeonList = twoDArrayToList(croppedDungeonTiles);
        dungeonList.removeAll(rooms);
        dungeonList.removeAll(doors);
        dungeonList.removeAll(corridors);
        int maxAttempt = dungeonList.size() * 2;
        do {
            DungeonTile tile = dungeonList.get(Utils.getRandomInt(0, dungeonList.size()));
            if (checkTileForDeadEnd(tile.getI(), tile.getJ())) {
                dungeonTiles[tile.getI()][tile.getJ()].setTexture(Textures.CORRIDOR);
                deadEnds.add(dungeonTiles[tile.getI()][tile.getJ()]);
                deadEndsCount++;
            }
            maxAttempt--;
        }
        while (count != deadEndsCount && maxAttempt > 0);
        return deadEnds;
    }

    private <T> List<T> twoDArrayToList(T[][] twoDArray) {
        List<T> list = new ArrayList<>();
        for (T[] array : twoDArray) {
            list.addAll(Arrays.asList(array));
        }
        return list;
    }

    private boolean checkTileForDeadEnd(int x, int y) {
        for (int i = x - 1; i < x + 2; i++) {
            for (int j = y - 1; j < y + 2; j++) {
                if (dungeonTiles[i][j].getTexture() != Textures.MARBLE) { // check if any other tile is there
                    return false;
                }
            }
        }
        return true;
    }

    public void addEntryPoint() {
        boolean entryIsOk;
        int x;
        int y;
        do {
            x = Utils.getRandomInt(1, dungeonTiles.length - 1);
            y = Utils.getRandomInt(1, dungeonTiles.length - 1);
            entryIsOk = dungeonTiles[x][y].getTexture() == Textures.MARBLE;
        }
        while (!entryIsOk);
        dungeonTiles[x][y].setTexture(Textures.ENTRY);
        doors.add(dungeonTiles[x][y]);
    }

    public void generateCorridors() {
        for (int d = 0; d < doors.size() - 1; d++) { // -1 because the end point
            result = new ArrayList<>();
            List<DungeonTile> openList = new ArrayList<>();
            List<DungeonTile> closedList = new ArrayList<>();
            DungeonTile start = doors.get(d); // set door as the starting point
            DungeonTile end = doors.get(d + 1); // set the next door as the end point
            for (int i = 1; i < dungeonTiles.length - 1; i++) { // preconfig H value + restore default values
                for (int j = 1; j < dungeonTiles.length - 1; j++) {
                    dungeonTiles[i][j].setH(Utils.manhattan(Math.abs(i - end.getI()), Math.abs(j - end.getJ())));
                    dungeonTiles[i][j].setG(0);
                    dungeonTiles[i][j].setParent(null);
                    dungeonTiles[i][j].setF(9999);
                }
            }
            addToClosedList(closedList, start); // add start point to closed list
            addToOpen(start, openList, closedList, end); // add the nearby nodes to openList
            while (result.isEmpty() && !openList.isEmpty()) {
                start = openList.get(0); // get lowest F to repeat things (openList sorted)
                addToClosedList(closedList, start); // add to closed list this node
                removeFromOpen(openList, start); // remove from open list this node
                addToOpen(start, openList, closedList, end); // add open list the nearby nodes
            }
            setPath(); // modify tiles Texture with the path
        }
    }

    private void setPath() {
        for (DungeonTile tile : result) {
            if (tile.getTexture() == Textures.MARBLE) { // only change the marble texture
                dungeonTiles[tile.getI()][tile.getJ()].setTexture(Textures.CORRIDOR);
                corridors.add(tile);
            }
        }
    }

    void removeFromOpen(List<DungeonTile> openList, DungeonTile node) {
        openList.remove(node);
    }

    private void addToOpen(DungeonTile node, List<DungeonTile> openList, List<DungeonTile> closedList, DungeonTile end) {
        addToOpenList(node, node.getI(), node.getJ() - 1, openList, closedList, end); // left
        addToOpenList(node, node.getI() - 1, node.getJ(), openList, closedList, end); // top
        addToOpenList(node, node.getI() + 1, node.getJ(), openList, closedList, end); // bottom
        addToOpenList(node, node.getI(), node.getJ() + 1, openList, closedList, end); // right
        calcGValue(openList); // calc G value Parent G + Movement
        calcFValue(openList); // calc F value (G + H)
    }

    private void calcFValue(List<DungeonTile> openList) {
        for (DungeonTile tile : openList) {
            tile.setF(tile.getG() + tile.getH());
        }
        Collections.sort(openList, new Comparator<DungeonTile>() { //sort it
            @Override
            public int compare(DungeonTile t1, DungeonTile t2) {
                return t1.getF() - t2.getF();
            }
        });
    }

    private void calcGValue(List<DungeonTile> openList) {
        for (DungeonTile tile : openList) {
            tile.setG(tile.getParent().getG() + MOVEMENT);
        }
    }

    private void addToOpenList(DungeonTile node, int x, int y, List<DungeonTile> openList, List<DungeonTile> closedList, DungeonTile end) {
        if (!checkEnd(node, x, y, end)) {
            checkG(node, x, y, openList); // check if it needs reparenting
            if (checkTileForOpenList(x, y) && !closedList.contains(dungeonTiles[x][y]) && !openList.contains(dungeonTiles[x][y])) { // not in openlist/closedlist
                setParent(node, x, y);
                openList.add(dungeonTiles[x][y]);
            }
        }
    }

    boolean checkTileForOpenList(int x, int y) {
        return dungeonTiles[x][y].getH() != 0 && dungeonTiles[x][y].getTexture() != Textures.ROOM && dungeonTiles[x][y].getTexture() != Textures.ROOM_EDGE; // check its not edge/room/room_edge
    }


    private void setParent(DungeonTile node, int x, int y) {
        dungeonTiles[x][y].setParent(node);
    }

    private void checkG(DungeonTile node, int x, int y, List<DungeonTile> openList) {
        if (openList.contains(dungeonTiles[x][y]) && dungeonTiles[x][y].getG() > (node.getG() + MOVEMENT)) {
            setParent(node, x, y);
        }
    }

    private boolean checkEnd(DungeonTile node, int x, int y, DungeonTile end) {
        if (end.getI() == x && end.getJ() == y) {
            setParent(node, x, y);
            getParents(node);
            return true;
        }
        return false;
    }

    private void getParents(DungeonTile node) {
        result.add(node);
        DungeonTile parent = node;
        while (parent.getParent() != null) {
            parent = parent.getParent();
            result.add(parent);
        }

    }

    void addToClosedList(List<DungeonTile> closedList, DungeonTile node) {
        closedList.add(dungeonTiles[node.getI()][node.getJ()]);
    }


    public void generateRoom() {
        int[] coordinates;
        for (int i = 0; i < roomCount; i++) {
            coordinates = setTilesForRoom();
            if (coordinates[0] != 0) {
                fillRoom(coordinates[0], coordinates[1], coordinates[2], coordinates[3]);
            }
        }
    }

    int getDoorCount(int down, int right) {
        if (down < 4 || right < 4) {
            return Utils.getRandomInt(1, 3);
        } else {
            return Utils.getRandomInt(2, 5);
        }
    }

    void fillRoom(int x, int y, int right, int down) {
        int doorCount = getDoorCount(down, right);
        for (int i = 0; i < down + 2; i++) { // fill with room_edge texture the bigger boundaries
            for (int j = 0; j < right + 2; j++) {
                dungeonTiles[x + i - 1][y + j - 1].setTexture(Textures.ROOM_EDGE);
            }
        }
        for (int i = 0; i < down; i++) { // fill room texture
            for (int j = 0; j < right; j++) {
                dungeonTiles[x + i][y + j].setTexture(Textures.ROOM);
                rooms.add(dungeonTiles[x + i][y + j]);
                dungeonTiles[x + i][y + j].setRoomCount(" ");
            }
        }
        int currentSize = doors.size();
        for (int d = 0; d < doorCount; d++) {
            addDoor(x, y, down, right);
        }
        int newSize = doors.size();
        List<DungeonTile> currentDoors = new ArrayList<>(doors.subList(currentSize, newSize));
        Utils.addRoomDescription(dungeonTiles, x, y, roomDescription, currentDoors);
    }

    void addDoor(int x, int y, int down, int right) {
        boolean doorIsOK;
        int doorX;
        int doorY;
        do {
            doorX = Utils.getRandomInt(x, x + down);
            doorY = Utils.getRandomInt(y, y + right);
            doorIsOK = checkDoor(doorX, doorY);
        }
        while (!doorIsOK);
    }

    boolean checkDoor(int x, int y) {
        for (int i = x - 1; i < x + 2; i++) {
            for (int j = y - 1; j < y + 2; j++) {
                if (dungeonTiles[i][j].getTexture() == Textures.DOOR || dungeonTiles[i][j].getTexture() == Textures.DOOR_LOCKED || dungeonTiles[i][j].getTexture() == Textures.DOOR_TRAPPED) { //check nearby doors
                    return false;
                }
            }
        }
        return checkEnvironment(x, y);
    }

    boolean checkEnvironment(int x, int y) {
        if (dungeonTiles[x][y - 1].getTexture() == Textures.ROOM_EDGE) { // left
            setDoor(x, y - 1);
            return true;
        } else if (dungeonTiles[x][y + 1].getTexture() == Textures.ROOM_EDGE) { // right
            setDoor(x, y + 1);
            return true;
        } else if (dungeonTiles[x + 1][y].getTexture() == Textures.ROOM_EDGE) { // bottom
            setDoor(x + 1, y);
            return true;
        } else if (dungeonTiles[x - 1][y].getTexture() == Textures.ROOM_EDGE) { // top
            setDoor(x - 1, y);
            return true;
        }
        return false;
    }

    void setDoor(int x, int y) {
        if (Utils.getRandomInt(0, 101) < 40) {
            dungeonTiles[x][y].setTexture(Textures.DOOR_TRAPPED);
        } else if (Utils.getRandomInt(0, 101) < 50) {
            dungeonTiles[x][y].setTexture(Textures.DOOR_LOCKED);
        } else {
            dungeonTiles[x][y].setTexture(Textures.DOOR);
        }
        doors.add(dungeonTiles[x][y]);
    }

    private int[] setTilesForRoom() {
        boolean roomIsOk;
        int x;
        int y;
        int max = dungeonTiles.length - (roomSize + 2); // because of edge + room_edge
        int failSafeCount = dungeonTiles.length * dungeonTiles.length / 2;
        int right;
        int down;
        do {
            x = Utils.getRandomInt(3, max); // 3 because of edge + room_edge
            y = Utils.getRandomInt(3, max);
            right = Utils.getRandomInt(2, roomSize + 1);
            down = Utils.getRandomInt(2, roomSize + 1);
            roomIsOk = checkTileGoodForRoom(x - 2, y - 2, right + 2, down + 2); // x&y-1 && roomSize +1 because i want min 2 tiles between rooms
            failSafeCount--;
        }
        while (!roomIsOk && failSafeCount > 0);
        if (failSafeCount > 0) {
            return new int[]{x, y, right, down};
        } else {
            return new int[]{0, 0, 0, 0}; // it can never be 0 if its a good coordinate
        }
    }


    private boolean checkTileGoodForRoom(int x, int y, int right, int down) {
        int maxX = x + down + 2; // +2 because of edges
        int maxY = y + right + 2;
        for (int i = x; i < maxX; i++) { // check the room area + boundaries
            for (int j = y; j < maxY; j++) {
                if (checkIsRoom(i, j)) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean checkIsRoom(int x, int y) {
        return dungeonTiles[x][y].getTexture() == Textures.ROOM || dungeonTiles[x][y].getTexture() == Textures.ROOM_EDGE;
    }


    public List<RoomDescription> getRoomDescription() {
        return roomDescription;
    }

    public List<TrapDescription> getTrapDescription() {
        return trapDescription;
    }

    public DungeonTile[][] getDungeonTiles() {
        return dungeonTiles;
    }
}
