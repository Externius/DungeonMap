package externius.rdmg.models;

public class DungeonTile {
    private Textures texture;
    private int i;
    private int j;
    private final int x;
    private final int y;
    private int width;
    private int height;
    private int h;
    private int g;
    private int f;
    private DungeonTile parent;
    private String roomCount;

    public DungeonTile(int x, int y) {
        this.x = x;
        this.y = y;
        this.texture = Textures.EDGE;
        setH(0);
    }

    public DungeonTile(int i, int j, int x, int y, int width, int height) {
        this.i = i;
        this.j = j;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.texture = Textures.MARBLE;
        setH(0);
    }

    public int getH() {
        return h;
    }

    public void setH(int h) {
        this.h = h;
    }

    public int getG() {
        return g;
    }

    public void setG(int g) {
        this.g = g;
    }

    public int getF() {
        return f;
    }

    public void setF(int f) {
        this.f = f;
    }

    public DungeonTile getParent() {
        return parent;
    }

    public void setParent(DungeonTile parent) {
        this.parent = parent;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getI() {
        return i;
    }

    public int getJ() {
        return j;
    }

    public Textures getTexture() {
        return texture;
    }

    public void setTexture(Textures texture) {
        this.texture = texture;
    }

    public String getRoomCount() {
        return roomCount;
    }

    public void setRoomCount(String roomCount) {
        this.roomCount = roomCount;
    }
}
