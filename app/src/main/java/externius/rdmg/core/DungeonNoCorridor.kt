package externius.rdmg.core;

import java.util.ArrayList;
import java.util.List;

import externius.rdmg.helpers.Door;
import externius.rdmg.helpers.RoomPosition;
import externius.rdmg.helpers.Utils;
import externius.rdmg.models.DungeonTile;
import externius.rdmg.models.Textures;

public class DungeonNoCorridor extends Dungeon {
    private List<DungeonTile> openDoorList;
    private List<DungeonTile> edgeTileList;
    private List<DungeonTile> roomStart;

    public DungeonNoCorridor(int dungeonWidth, int dungeonHeight, int dungeonSize, int roomSizePercent) {
        this.dungeonWidth = dungeonWidth;
        this.dungeonHeight = dungeonHeight;
        this.dungeonSize = dungeonSize;
        this.roomSizePercent = roomSizePercent;
    }

    @Override
    public void init() {
        int imgSizeX = dungeonWidth / dungeonSize;
        int imgSizeY = dungeonHeight / dungeonSize;
        roomSize = Math.round((float) dungeonSize / 100 * roomSizePercent);
        roomDescription = new ArrayList<>();
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

    @Override
    public void generate() {
        init();
        addFirstRoom();
        fillRoomToDoor();
        addEntryPoint();
        addDescription();
    }

    public void addFirstRoom() {
        roomStart = new ArrayList<>();
        openDoorList = new ArrayList<>();
        int x = Utils.getRandomInt(5, dungeonTiles.length - (roomSize + 4));
        int y = Utils.getRandomInt(5, dungeonTiles.length - (roomSize + 4));
        int right = Utils.getRandomInt(2, roomSize + 1);
        int down = Utils.getRandomInt(2, roomSize + 1);
        fillRoom(x, y, down, right);
        roomStart.add(dungeonTiles[x][y]);
    }

    private void setRoomTiles(int x, int y) {
        dungeonTiles[x][y].setTexture(Textures.ROOM);
        dungeonTiles[x][y].setDescription(" ");
        dungeonTiles[x][y].setIndex(roomStart.size());
    }

    @Override
    void fillRoom(int x, int y, int right, int down) {
        int doorCount = getDoorCount(down, right);
        for (int i = 0; i < down + 2; i++) { // fill with room_edge texture the bigger boundaries
            for (int j = 0; j < right + 2; j++) {
                dungeonTiles[x + i - 1][y + j - 1].setTexture(Textures.ROOM_EDGE);
            }
        }
        for (int i = 0; i < down; i++) { // fill room texture
            for (int j = 0; j < right; j++) {
                setRoomTiles(x + i, y + j);
                rooms.add(dungeonTiles[x + i][y + j]);
            }
        }
        for (int d = 0; d < doorCount; d++) {
            addDoor(x, y, down, right);
        }
    }

    @Override
    public void addEntryPoint() {
        boolean entryIsOk;
        int x;
        int y;
        do {
            x = Utils.getRandomInt(1, dungeonTiles.length - 1);
            y = Utils.getRandomInt(1, dungeonTiles.length - 1);
            RoomPosition.checkRoomPosition(dungeonTiles, x, y);
            entryIsOk = dungeonTiles[x][y].getTexture() == Textures.ROOM_EDGE && checkPos() && checkNearbyDoor(dungeonTiles[x][y]) && checkEdges(dungeonTiles, x, y);
        }
        while (!entryIsOk);
        dungeonTiles[x][y].setTexture(Textures.ENTRY);
    }

    private boolean checkEdges(DungeonTile[][] dungeonTiles, int x, int y) {
        return dungeonTiles[x][y - 1].getTexture() == Textures.ROOM || dungeonTiles[x][y + 1].getTexture() == Textures.ROOM || dungeonTiles[x + 1][y].getTexture() == Textures.ROOM || dungeonTiles[x - 1][y].getTexture() == Textures.ROOM;
    }

    public void fillRoomToDoor() {
        while (!openDoorList.isEmpty()) {
            int i = openDoorList.size() - 1;
            RoomPosition.checkRoomPosition(dungeonTiles, openDoorList.get(i).getI(), openDoorList.get(i).getJ());
            if (checkPos()) {
                if (RoomPosition.isUp()) {
                    randomFillUpDown(openDoorList.get(i).getI() + 1, openDoorList.get(i).getJ(), openDoorList.get(i));
                } else if (RoomPosition.isDown()) {
                    randomFillUpDown(openDoorList.get(i).getI() - 1, openDoorList.get(i).getJ(), openDoorList.get(i));
                } else if (RoomPosition.isRight()) {
                    randomFillLeftRight(openDoorList.get(i).getI(), openDoorList.get(i).getJ() - 1, openDoorList.get(i));
                } else if (RoomPosition.isLeft()) {
                    randomFillLeftRight(openDoorList.get(i).getI(), openDoorList.get(i).getJ() + 1, openDoorList.get(i));
                }
            }
            openDoorList.remove(openDoorList.get(i));
        }
    }

    private void randomFillLeftRight(int x, int y, DungeonTile door) {
        int[] result = checkArea(x, y, door);
        if (result[0] != 0 && result[1] != 0) {
            fillLeftRight(x, y, result[0], result[1]);
        }
    }

    private void fillLeftRight(int x, int y, int down, int right) {
        edgeTileList = new ArrayList<>();
        if (right < 0) {
            for (int i = right + 1; i < 1; i++) {
                fillVertical(x, y + i, down);
            }
        } else {
            for (int i = 0; i < right; i++) {
                fillVertical(x, y + i, down);
            }
        }
        setVerticalEdge(x, y, right, down);
        setVerticalEdge(x, y, right < 0 ? 1 : -1, down);
        fillDoor(down, right);
        roomStart.add(dungeonTiles[x][y]);
    }

    public void addDescription() {
        cleanDoorList();
        setDoorsDescriptions();
        for (DungeonTile room : roomStart) {
            List<DungeonTile> openList = new ArrayList<>();
            List<DungeonTile> closedList = new ArrayList<>();
            DungeonTile start = room;
            addToClosedList(closedList, start); // add start point to closed list
            addToOpen(start, openList, closedList); // add the nearby nodes to openList
            while (!openList.isEmpty()) {
                start = openList.get(0); // get next room
                addToClosedList(closedList, start); // add to closed list this node
                removeFromOpen(openList, start); // remove from open list this node
                addToOpen(start, openList, closedList); // add open list the nearby nodes
            }
            Utils.addNCRoomDescription(dungeonTiles, room.getI(), room.getJ(), roomDescription, Door.getNCDoorDescription(dungeonTiles, closedList));
        }
    }

    private void setDoorsDescriptions() {
        for (DungeonTile door : doors) {
            door.setDescription(Door.getNCDoor(door));
        }
    }

    @Override
    boolean checkTileForOpenList(int x, int y) {
        return dungeonTiles[x][y].getTexture() == Textures.ROOM;
    }

    private void addToOpenList(int x, int y, List<DungeonTile> openList, List<DungeonTile> closedList) {
        if (checkTileForOpenList(x, y) && !closedList.contains(dungeonTiles[x][y]) && !openList.contains(dungeonTiles[x][y])) { // not in openlist/closedlist
            openList.add(dungeonTiles[x][y]);
        }
    }

    private void addToOpen(DungeonTile node, List<DungeonTile> openList, List<DungeonTile> closedList) {
        addToOpenList(node.getI(), node.getJ() - 1, openList, closedList); // left
        addToOpenList(node.getI() - 1, node.getJ(), openList, closedList); // top
        addToOpenList(node.getI() + 1, node.getJ(), openList, closedList); // bottom
        addToOpenList(node.getI(), node.getJ() + 1, openList, closedList); // right
    }

    private void cleanDoorList() {
        List<DungeonTile> toDelete = new ArrayList<>();
        for (DungeonTile door : doors) {
            if (door.getTexture() == Textures.ROOM_EDGE) {
                toDelete.add(door);
            }
        }
        doors.removeAll(toDelete);
    }

    private void setVerticalEdge(int x, int y, int right, int down) {
        boolean addToEdgeList = !(right == 1 || right == -1);
        if (down < 0) { // up
            for (int i = down; i < 2; i++) { //right edge
                setRoomEdge(x + i, y + right, addToEdgeList);
            }
        } else { // bottom
            for (int i = -1; i < down + 1; i++) { //left edge
                setRoomEdge(x + i, y + right, addToEdgeList);
            }
        }
    }

    private void fillVertical(int x, int y, int down) {
        if (down < 0) { // up
            for (int i = down + 1; i < 1; i++) { // set room
                setRoomTiles(x + i, y);
            }
            setTextureToRoomEdge(x + 1, y); // bottom edge
            addEdgeTileList(x + 1, y);
        } else { // down
            for (int i = 0; i < down; i++) { // set room
                setRoomTiles(x + i, y);
            }
            setTextureToRoomEdge(x - 1, y); // top edge
            addEdgeTileList(x - 1, y);
        }
        setTextureToRoomEdge(x + down, y);
        addEdgeTileList(x + down, y);
    }

    private int[] checkArea(int x, int y, DungeonTile door) {
        int vertical = checkVertical(x, y);
        int horizontal = checkHorizontal(x, y);
        if (checkPossible(vertical, horizontal, door)) {
            int[] result = getDownRight(vertical, horizontal);
            int down = result[0];
            int right = result[1];
            vertical = checkVerticalOneWay(x, y + right, down); // check horizontal end vertically
            horizontal = checkHorizontalOneWay(x + down, y, right); // check vertical end horizontally
            if (checkPossibleEnd(vertical, horizontal, door, down, right)) {
                return new int[]{down, right};
            }
        }
        return new int[]{0, 0};
    }

    private boolean checkPossibleEnd(int vertical, int horizontal, DungeonTile door, int down, int right) {
        if (vertical < 0 != down < 0 || horizontal < 0 != right < 0 || Math.abs(vertical) < Math.abs(down) || Math.abs(horizontal) < Math.abs(right)) { // it would overlap with another room
            dungeonTiles[door.getI()][door.getJ()].setTexture(Textures.ROOM_EDGE); // change the door to a room_edge
            return false;
        }
        return true;
    }

    private void randomFillUpDown(int x, int y, DungeonTile door) {
        int[] result = checkArea(x, y, door);
        if (result[0] != 0 && result[1] != 0) {
            fillUpDown(x, y, result[0], result[1]);
        }
    }

    private void fillUpDown(int x, int y, int down, int right) {
        edgeTileList = new ArrayList<>();
        if (down < 0) {
            for (int i = down + 1; i < 1; i++) {
                fillHorizontal(x + i, y, right);
            }
        } else {
            for (int i = 0; i < down; i++) {
                fillHorizontal(x + i, y, right);
            }
        }
        setHorizontalEdge(x, y, right, down);
        setHorizontalEdge(x, y, right, down < 0 ? 1 : -1);
        fillDoor(down, right);
        roomStart.add(dungeonTiles[x][y]);
    }

    @Override
    int getDoorCount(int down, int right) {
        if (Math.abs(down) < 4 || Math.abs(right) < 4) {
            return 2;
        } else {
            return Utils.getRandomInt(3, 6);
        }
    }

    private void fillDoor(int down, int right) {
        int doorCount = getDoorCount(down, right);
        cleanEdgeTileList();
        int maxTryNumber = edgeTileList.size();
        do {
            int random = Utils.getRandomInt(0, edgeTileList.size());
            if (checkNearbyDoor(edgeTileList.get(random))) {
                setDoor(edgeTileList.get(random).getI(), edgeTileList.get(random).getJ());
                doorCount--;
            }
            maxTryNumber--;
        }
        while (doorCount > 0 && maxTryNumber > 0);
    }

    private boolean checkNearbyDoor(DungeonTile node) {
        for (int i = node.getI() - 1; i < node.getI() + 2; i++) {
            for (int j = node.getJ() - 1; j < node.getJ() + 2; j++) {
                if (Door.checkNCDoor(dungeonTiles, i, j)) { // check nearby doors
                    return false;
                }
            }
        }
        return true;
    }

    private void cleanEdgeTileList() {
        List<DungeonTile> toDelete = new ArrayList<>();
        for (DungeonTile tile : edgeTileList) {
            if (checkRooms(tile.getI(), tile.getJ())) { // if its on the edge
                toDelete.add(tile);
            }
        }
        edgeTileList.removeAll(toDelete);
    }

    private boolean checkRooms(int x, int y) {
        return dungeonTiles[x][y - 1].getTexture() != Textures.ROOM && dungeonTiles[x][y + 1].getTexture() != Textures.ROOM && dungeonTiles[x + 1][y].getTexture() != Textures.ROOM && dungeonTiles[x - 1][y].getTexture() != Textures.ROOM;
    }

    private void setHorizontalEdge(int x, int y, int right, int down) {
        boolean addToEdgeList = !(down == 1 || down == -1);
        if (right < 0) { // left
            for (int i = right; i < 2; i++) {
                setRoomEdge(x + down, y + i, addToEdgeList);
            }
        } else { // right
            for (int i = -1; i < right + 1; i++) {
                setRoomEdge(x + down, y + i, addToEdgeList);
            }
        }
    }

    private void setRoomEdge(int x, int y, boolean addToEdgeList) {
        if (!Door.checkNCDoor(dungeonTiles, x, y) && addToEdgeList) { // if its not a corridor_door
            setTextureToRoomEdge(x, y);
            addEdgeTileList(x, y);
        } else if (!Door.checkNCDoor(dungeonTiles, x, y)) {
            setTextureToRoomEdge(x, y);
        }
    }

    private void setTextureToRoomEdge(int x, int y) {
        dungeonTiles[x][y].setTexture(Textures.ROOM_EDGE);
    }

    private void addEdgeTileList(int x, int y) {
        edgeTileList.add(dungeonTiles[x][y]);
    }

    private void fillHorizontal(int x, int y, int right) {
        if (right < 0) { // left
            for (int i = right + 1; i < 1; i++) { // set room
                setRoomTiles(x, y + i);
            }
            setTextureToRoomEdge(x, y + 1); // right edge
            addEdgeTileList(x, y + 1);
        } else { // right
            for (int i = 0; i < right; i++) { // set room
                setRoomTiles(x, y + i);
            }
            setTextureToRoomEdge(x, y - 1); // left edge
            addEdgeTileList(x, y - 1);
        }
        setTextureToRoomEdge(x, y + right);
        addEdgeTileList(x, y + right);
    }

    private int[] getDownRight(int vertical, int horizontal) {
        int down = Utils.getRandomInt(2, Math.min((Math.abs(vertical)), roomSize));
        int right = Utils.getRandomInt(2, Math.min((Math.abs(horizontal)), roomSize));
        if (vertical < 0) {
            down = -down;
        }
        if (horizontal < 0) {
            right = -right;
        }
        return new int[]{down, right};
    }

    private boolean checkPossible(int vertical, int horizontal, DungeonTile door) {
        if (vertical == 0 || horizontal == 0) { // its impossible to add room
            dungeonTiles[door.getI()][door.getJ()].setTexture(Textures.ROOM_EDGE); // change the door to a room_edge
            return false;
        }
        return true;
    }

    private int checkUp(int x, int y) {
        boolean tile;
        boolean edge;
        int temp = x;
        int count = 0;
        do {
            tile = checkTile(temp, y);
            edge = checkDungeonTilesEdge(temp, y);
            temp--;
            count--;
        }
        while (!tile && !edge);
        if (edge) {
            count++;
        }
        return count;
    }

    private int checkDown(int x, int y) {
        boolean tile;
        boolean edge;
        int temp = x;
        int count = 0;
        do {
            tile = checkTile(temp, y);
            edge = checkDungeonTilesEdge(temp, y);
            temp++;
            count++;
        }
        while (!tile && !edge);
        if (edge) {
            count--;
        }
        return count;
    }

    private int checkVerticalOneWay(int x, int y, int down) {
        int count;
        if (down < 0) { // up
            count = checkUp(x, y);
        } else {
            count = checkDown(x, y);
        }
        if (Math.abs(count) > 2) {
            return count;
        }
        return 0;
    }

    private int checkLeft(int x, int y) {
        boolean tile;
        boolean edge;
        int temp = y;
        int count = 0;
        do {
            tile = checkTile(x, temp);
            edge = checkDungeonTilesEdge(x, temp);
            temp--;
            count--;
        }
        while (!tile && !edge);
        if (edge) {
            count++;
        }
        return count;
    }

    private int checkRight(int x, int y) {
        boolean tile;
        boolean edge;
        int temp = y;
        int count = 0;
        do {
            tile = checkTile(x, temp);
            edge = checkDungeonTilesEdge(x, temp);
            temp++;
            count++;
        }
        while (!tile && !edge);
        if (edge) {
            count--;
        }
        return count;
    }

    private int checkHorizontalOneWay(int x, int y, int right) {
        int count;
        if (right < 0) { // left
            count = checkLeft(x, y);
        } else {
            count = checkRight(x, y);
        }
        if (Math.abs(count) > 2) {
            return count;
        }
        return 0;
    }

    private int checkHorizontal(int x, int y) {
        return getCheckResult(checkLeft(x, y), checkRight(x, y));
    }

    private boolean checkDungeonTilesEdge(int x, int y) {
        return dungeonTiles[x][y].getTexture() == Textures.EDGE;
    }

    private boolean checkTile(int x, int y) {
        return dungeonTiles[x][y].getTexture() == Textures.ROOM_EDGE || Door.checkNCDoor(dungeonTiles, x, y);
    }

    private int checkVertical(int x, int y) {
        return getCheckResult(checkUp(x, y), checkDown(x, y));
    }

    private int getCheckResult(int x, int y) {
        if (Math.abs(x) >= Math.abs(y) && Math.abs(x) > 2) {
            return x;
        }
        if (Math.abs(y) > Math.abs(x) && Math.abs(y) > 2) {
            return y;
        }
        return 0;
    }

    private boolean checkPos() {
        return !(RoomPosition.isUp() && RoomPosition.isDown() || RoomPosition.isLeft() && RoomPosition.isRight());
    }

    @Override
    public boolean checkDoor(int x, int y) {
        for (int i = x - 1; i < x + 2; i++) {
            for (int j = y - 1; j < y + 2; j++) {
                if (Door.checkNCDoor(dungeonTiles, i, j)) { // check nearby doors
                    return false;
                }
            }
        }
        return checkEnvironment(x, y);
    }

    @Override
    void setDoor(int x, int y) {
        if (Utils.getRandomInt(0, 101) < 40) {
            dungeonTiles[x][y].setTexture(Textures.NO_CORRIDOR_DOOR_TRAPPED);
        } else if (Utils.getRandomInt(0, 101) < 50) {
            dungeonTiles[x][y].setTexture(Textures.NO_CORRIDOR_DOOR_LOCKED);
        } else {
            dungeonTiles[x][y].setTexture(Textures.NO_CORRIDOR_DOOR);
        }
        openDoorList.add(dungeonTiles[x][y]);
        doors.add(dungeonTiles[x][y]);
    }

    public List<DungeonTile> getOpenDoorList() {
        return openDoorList;
    }
}